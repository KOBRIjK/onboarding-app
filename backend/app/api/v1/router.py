from fastapi import APIRouter

from .endpoints import auth, onboarding, rag, users

api_router = APIRouter()
api_router.include_router(auth.router, prefix="/auth", tags=["auth"])
api_router.include_router(onboarding.router, prefix="/onboarding", tags=["onboarding"])
api_router.include_router(rag.router, prefix="/rag", tags=["rag"])
api_router.include_router(users.router, tags=["users"])
