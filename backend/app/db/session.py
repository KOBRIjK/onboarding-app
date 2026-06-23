from __future__ import annotations

from collections.abc import Generator

from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker

from ..core.config import settings
from .base import Base

engine = create_engine(settings.database_url, pool_pre_ping=True)
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)


def init_db() -> None:
    from .. import models  # noqa: F401
    from ..services.onboarding import seed_demo_onboarding

    Base.metadata.create_all(engine)
    db = SessionLocal()
    try:
        seed_demo_onboarding(db)
    finally:
        db.close()


def db_session() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
