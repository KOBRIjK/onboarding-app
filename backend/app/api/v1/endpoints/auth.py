from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from ....db.session import db_session
from ....schemas.auth import LoginRequest, LogoutRequest, RefreshRequest, SignupRequest, TokenResponse
from ....services.auth import (
    authenticate_user,
    create_user,
    get_user_by_email,
    issue_token_pair,
    logout_refresh_token,
    refresh_rotate,
)

router = APIRouter()


@router.post("/signup", response_model=TokenResponse)
def signup(req: SignupRequest, db: Session = Depends(db_session)) -> TokenResponse:
    existing = get_user_by_email(db, req.email)
    if existing is not None:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Email already exists",
        )

    user = create_user(db, email=req.email, name=req.name, password=req.password)
    return issue_token_pair(db, user)


@router.post("/login", response_model=TokenResponse)
def login(req: LoginRequest, db: Session = Depends(db_session)) -> TokenResponse:
    user = authenticate_user(db, req.email, req.password)
    if user is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid credentials",
        )

    return issue_token_pair(db, user)


@router.post("/refresh", response_model=TokenResponse)
def refresh(req: RefreshRequest, db: Session = Depends(db_session)) -> TokenResponse:
    return refresh_rotate(db, req.refreshToken)


@router.post("/logout")
def logout(req: LogoutRequest, db: Session = Depends(db_session)) -> dict[str, bool]:
    logout_refresh_token(db, req.refreshToken)
    return {"ok": True}
