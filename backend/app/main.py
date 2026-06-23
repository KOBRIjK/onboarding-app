from __future__ import annotations

from contextlib import asynccontextmanager
from typing import AsyncIterator

from fastapi import FastAPI

from .api.v1.router import api_router
from .db.session import init_db


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    init_db()
    yield


def create_app() -> FastAPI:
    app = FastAPI(
        title="Onboarding Auth API",
        version="1.0.0",
        lifespan=lifespan,
    )
    app.include_router(api_router, prefix="/api/v1")
    return app


app = create_app()
