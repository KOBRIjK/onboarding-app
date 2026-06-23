from __future__ import annotations

from datetime import timedelta
from functools import lru_cache
from pathlib import Path
from typing import Optional

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict

BACKEND_DIR = Path(__file__).resolve().parents[2]


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=str(BACKEND_DIR / ".env"),
        env_file_encoding="utf-8",
        extra="ignore",
    )

    jwt_secret: str = Field(..., alias="JWT_SECRET")
    jwt_issuer: str = Field("onboarding-app", alias="JWT_ISSUER")
    jwt_audience: Optional[str] = Field(None, alias="JWT_AUDIENCE")

    access_ttl_minutes: int = Field(15, alias="ACCESS_TTL_MINUTES")
    refresh_ttl_days: int = Field(14, alias="REFRESH_TTL_DAYS")

    database_url: str = Field(
        "postgresql+psycopg://onboarding:onboarding_password@127.0.0.1:5433/onboarding",
        alias="DATABASE_URL",
    )

    seed_demo_data: bool = Field(True, alias="SEED_DEMO_DATA")
    seed_user_email: str = Field("backend.dev@example.com", alias="SEED_USER_EMAIL")
    seed_user_password: str = Field("password123", alias="SEED_USER_PASSWORD")
    seed_user_name: str = Field("Backend Developer", alias="SEED_USER_NAME")
    seed_user_started_days_ago: int = Field(6, alias="SEED_USER_STARTED_DAYS_AGO")

    rag_model_name: str = Field("microsoft/Phi-3.5-mini-instruct", alias="RAG_MODEL_NAME")
    rag_embedding_model_name: str = Field(
        "sentence-transformers/all-MiniLM-L6-v2",
        alias="RAG_EMBEDDING_MODEL_NAME",
    )
    rag_index_cache_dir: str = Field("storage/faiss", alias="RAG_INDEX_CACHE_DIR")
    rag_chunk_size: int = Field(500, alias="RAG_CHUNK_SIZE")
    rag_chunk_overlap: int = Field(100, alias="RAG_CHUNK_OVERLAP")
    rag_retriever_k: int = Field(10, alias="RAG_RETRIEVER_K")
    rag_context_docs: int = Field(5, alias="RAG_CONTEXT_DOCS")
    rag_max_new_tokens: int = Field(256, alias="RAG_MAX_NEW_TOKENS")
    rag_temperature: float = Field(0.1, alias="RAG_TEMPERATURE")
    rag_do_sample: bool = Field(False, alias="RAG_DO_SAMPLE")
    rag_device_map: str = Field("auto", alias="RAG_DEVICE_MAP")
    rag_torch_dtype: str = Field("auto", alias="RAG_TORCH_DTYPE")

    confluence_base_url: str = Field("http://127.0.0.1:8090", alias="CONFLUENCE_BASE_URL")
    confluence_username: str = Field("", alias="CONFLUENCE_USERNAME")
    confluence_password: str = Field("", alias="CONFLUENCE_PASSWORD")
    confluence_space_key: Optional[str] = Field(None, alias="CONFLUENCE_SPACE_KEY")
    confluence_page_limit: int = Field(100, alias="CONFLUENCE_PAGE_LIMIT")
    confluence_request_timeout_seconds: int = Field(
        20,
        alias="CONFLUENCE_REQUEST_TIMEOUT_SECONDS",
    )

    @property
    def refresh_ttl_delta(self) -> timedelta:
        return timedelta(days=self.refresh_ttl_days)

    @property
    def rag_index_cache_path(self) -> Path:
        path = Path(self.rag_index_cache_dir)
        if not path.is_absolute():
            path = BACKEND_DIR / path
        return path


@lru_cache
def get_settings() -> Settings:
    settings = Settings()
    if len(settings.jwt_secret.encode("utf-8")) < 32:
        raise RuntimeError(
            "JWT_SECRET is too short. Must be at least 32 bytes (256 bits)."
        )
    return settings


settings = get_settings()
