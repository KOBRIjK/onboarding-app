from __future__ import annotations

from fastapi import APIRouter, HTTPException, status

from ....schemas.rag import (
    RagDocument,
    RagQueryRequest,
    RagQueryResponse,
    RagReloadResponse,
)
from ....services.rag import RAGUnavailableError, rag_service

router = APIRouter()


@router.post("/ask", response_model=RagQueryResponse)
def ask_rag(req: RagQueryRequest) -> RagQueryResponse:
    try:
        result = rag_service.answer(req.query)
    except RAGUnavailableError as exc:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=str(exc),
        ) from exc

    return RagQueryResponse(
        answer=result.answer,
        documents=[
            RagDocument(index=index + 1, content=content)
            for index, content in enumerate(result.documents)
        ],
    )


@router.post("/reload", response_model=RagReloadResponse)
def reload_rag() -> RagReloadResponse:
    try:
        rag_service.reload_retriever()
    except RAGUnavailableError as exc:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=str(exc),
        ) from exc

    return RagReloadResponse(ok=True)
