from pydantic import BaseModel, Field


class RagQueryRequest(BaseModel):
    query: str = Field(min_length=1)


class RagDocument(BaseModel):
    index: int
    content: str


class RagQueryResponse(BaseModel):
    answer: str
    documents: list[RagDocument]


class RagReloadResponse(BaseModel):
    ok: bool
