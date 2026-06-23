from __future__ import annotations

import hashlib
import json
import re
from dataclasses import dataclass
from pathlib import Path
from threading import Lock
from typing import Any

from ..core.config import settings
from .confluence import ConfluenceError, build_confluence_corpus

CACHE_SCHEMA_VERSION = 1
CACHE_METADATA_FILE = "metadata.json"


class RAGUnavailableError(RuntimeError):
    pass


@dataclass(frozen=True)
class RAGResult:
    answer: str
    documents: list[str]


@dataclass(frozen=True)
class LoadedLLM:
    tokenizer: Any
    model: Any


class RAGService:
    def __init__(self) -> None:
        self._retriever_lock = Lock()
        self._llm_lock = Lock()
        self._retriever_loaded = False
        self._llm_loaded = False
        self._retriever: Any | None = None
        self._llm: LoadedLLM | None = None

    def answer(self, query: str) -> RAGResult:
        self._ensure_retriever_loaded()
        assert self._retriever is not None

        docs = self._retriever.invoke(query)
        docs = self._rerank_documents(query, docs)
        context_documents = [doc.page_content for doc in docs]
        context = self._build_context(context_documents)
        prompt = self._build_prompt(context=context, query=query)

        self._ensure_llm_loaded()
        assert self._llm is not None

        answer = self._generate_answer(self._llm, prompt)

        return RAGResult(answer=answer, documents=context_documents)

    def reload_retriever(self) -> None:
        with self._retriever_lock:
            self._retriever = self._load_retriever()
            self._retriever_loaded = True

    def _ensure_retriever_loaded(self) -> None:
        if self._retriever_loaded:
            return

        with self._retriever_lock:
            if self._retriever_loaded:
                return
            self._retriever = self._load_retriever()
            self._retriever_loaded = True

    def _ensure_llm_loaded(self) -> None:
        if self._llm_loaded:
            return

        with self._llm_lock:
            if self._llm_loaded:
                return
            self._llm = self._load_llm()
            self._llm_loaded = True

    def _load_retriever(self) -> Any:
        try:
            from langchain_community.embeddings import HuggingFaceEmbeddings
            from langchain_community.vectorstores import FAISS
            from langchain_text_splitters import RecursiveCharacterTextSplitter
        except ImportError as exc:
            raise RAGUnavailableError(
                "RAG dependencies are not installed. Run `pip install -r requirements.txt`."
            ) from exc

        text = self._read_knowledge_text()
        data_hash = self._hash_text(text)
        cache_path = settings.rag_index_cache_path
        cache_metadata = self._build_cache_metadata(data_hash=data_hash)
        embeddings = HuggingFaceEmbeddings(model_name=settings.rag_embedding_model_name)

        if self._is_cache_valid(cache_path, cache_metadata):
            vectorstore = self._load_cached_vectorstore(FAISS, cache_path, embeddings)
            if vectorstore is not None:
                return vectorstore.as_retriever(
                    search_kwargs={"k": settings.rag_retriever_k}
                )

        vectorstore = self._build_vectorstore(
            FAISS=FAISS,
            splitter_cls=RecursiveCharacterTextSplitter,
            text=text,
            embeddings=embeddings,
        )

        cache_path.mkdir(parents=True, exist_ok=True)
        vectorstore.save_local(str(cache_path))
        self._write_cache_metadata(cache_path, cache_metadata)

        return vectorstore.as_retriever(
            search_kwargs={"k": settings.rag_retriever_k}
        )

    @staticmethod
    def _load_cached_vectorstore(
        FAISS: Any,
        cache_path: Path,
        embeddings: Any,
    ) -> Any | None:
        if not (cache_path / "index.faiss").exists() or not (
            cache_path / "index.pkl"
        ).exists():
            return None

        try:
            return FAISS.load_local(
                str(cache_path),
                embeddings,
                allow_dangerous_deserialization=True,
            )
        except Exception:
            return None

    @staticmethod
    def _build_vectorstore(
        *,
        FAISS: Any,
        splitter_cls: Any,
        text: str,
        embeddings: Any,
    ) -> Any:
        splitter = splitter_cls(
            chunk_size=settings.rag_chunk_size,
            chunk_overlap=settings.rag_chunk_overlap,
        )
        docs = splitter.split_text(text)
        return FAISS.from_texts(docs, embedding=embeddings)

    def _load_llm(self) -> LoadedLLM:
        try:
            from transformers import AutoModelForCausalLM, AutoTokenizer
        except ImportError as exc:
            raise RAGUnavailableError(
                "RAG dependencies are not installed. Run `pip install -r requirements.txt`."
            ) from exc

        tokenizer = AutoTokenizer.from_pretrained(settings.rag_model_name)
        model = AutoModelForCausalLM.from_pretrained(
            settings.rag_model_name,
            device_map=settings.rag_device_map,
            torch_dtype=settings.rag_torch_dtype,
        )

        return LoadedLLM(tokenizer=tokenizer, model=model)

    @staticmethod
    def _generate_answer(llm: LoadedLLM, prompt: str) -> str:
        messages = [
            {
                "role": "system",
                "content": (
                    "Ты русскоязычный RAG-ассистент. Всегда отвечай строго "
                    "на русском языке. Верни только итоговый ответ на вопрос. "
                    "Используй только предоставленный контекст. "
                    "Не пересказывай инструкции и не обсуждай правила. "
                    "Не расшифровывай аббревиатуры, если расшифровки нет в контексте. "
                    "Не показывай рассуждения и шаги. Отвечай одним коротким абзацем. "
                    "Если в контексте нет ответа, скажи: Нет информации."
                ),
            },
            {"role": "user", "content": prompt},
        ]

        tokenizer = llm.tokenizer
        model = llm.model

        if getattr(tokenizer, "chat_template", None):
            inputs = tokenizer.apply_chat_template(
                messages,
                tokenize=True,
                add_generation_prompt=True,
                return_tensors="pt",
                return_dict=True,
            ).to(model.device)
        else:
            inputs = tokenizer(prompt, return_tensors="pt").to(model.device)

        generation_kwargs: dict[str, Any] = {
            "max_new_tokens": settings.rag_max_new_tokens,
            "do_sample": settings.rag_do_sample,
            "pad_token_id": tokenizer.eos_token_id,
        }
        if settings.rag_do_sample:
            generation_kwargs["temperature"] = settings.rag_temperature

        outputs = model.generate(**inputs, **generation_kwargs)
        answer = tokenizer.decode(
            outputs[0][inputs["input_ids"].shape[-1] :],
            skip_special_tokens=True,
        )
        return answer.strip()

    @staticmethod
    def _rerank_documents(query: str, docs: list[Any]) -> list[Any]:
        query_terms = RAGService._search_terms(query)
        if not query_terms:
            return docs[: settings.rag_context_docs]

        def score_doc(indexed_doc: tuple[int, Any]) -> tuple[int, int]:
            index, doc = indexed_doc
            doc_terms = RAGService._search_terms(doc.page_content)
            return (len(query_terms & doc_terms), -index)

        ranked = sorted(enumerate(docs), key=score_doc, reverse=True)
        return [doc for _, doc in ranked[: settings.rag_context_docs]]

    @staticmethod
    def _search_terms(text: str) -> set[str]:
        words = {
            word
            for word in re.findall(r"[A-Za-zА-Яа-яЁё0-9]+", text.lower())
            if len(word) >= 3
        }
        prefixes = {word[:6] for word in words if len(word) > 6}
        return words | prefixes

    @staticmethod
    def _read_knowledge_text() -> str:
        try:
            return build_confluence_corpus()
        except ConfluenceError as exc:
            raise RAGUnavailableError(str(exc)) from exc

    @staticmethod
    def _hash_text(text: str) -> str:
        return hashlib.sha256(text.encode("utf-8")).hexdigest()

    @staticmethod
    def _build_cache_metadata(*, data_hash: str) -> dict[str, Any]:
        return {
            "schema_version": CACHE_SCHEMA_VERSION,
            "source": "confluence",
            "data_sha256": data_hash,
            "confluence_base_url": settings.confluence_base_url,
            "confluence_space_key": settings.confluence_space_key,
            "confluence_page_limit": settings.confluence_page_limit,
            "embedding_model_name": settings.rag_embedding_model_name,
            "chunk_size": settings.rag_chunk_size,
            "chunk_overlap": settings.rag_chunk_overlap,
        }

    @staticmethod
    def _is_cache_valid(cache_path: Path, expected_metadata: dict[str, Any]) -> bool:
        metadata_path = cache_path / CACHE_METADATA_FILE
        if not metadata_path.exists():
            return False

        try:
            cached_metadata = json.loads(metadata_path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError):
            return False

        return cached_metadata == expected_metadata

    @staticmethod
    def _write_cache_metadata(cache_path: Path, metadata: dict[str, Any]) -> None:
        metadata_path = cache_path / CACHE_METADATA_FILE
        metadata_path.write_text(
            json.dumps(metadata, ensure_ascii=False, indent=2),
            encoding="utf-8",
        )

    @staticmethod
    def _build_context(documents: list[str]) -> str:
        if not documents:
            return "Нет доступных документов для контекста."

        return "\n\n".join(
            f"Документ {index + 1}:\n{content}"
            for index, content in enumerate(documents)
        )

    @staticmethod
    def _build_prompt(*, context: str, query: str) -> str:
        return f"""
КОНТЕКСТ:
---
{context}
---

ВОПРОС:
{query}

ОТВЕТ:
"""


rag_service = RAGService()
