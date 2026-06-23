from __future__ import annotations

import uuid

import jwt
from fastapi import HTTPException, status
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from ..core.config import settings
from ..core.security import (
    create_access_token,
    create_refresh_token,
    decode_jwt,
    hash_password,
    utcnow,
    verify_password,
)
from ..models import RefreshSession, User
from ..schemas.auth import TokenResponse
from .onboarding import assign_default_flow_to_user


def get_user_by_email(db: Session, email: str) -> User | None:
    stmt = select(User).where(func.lower(User.email) == email.lower()).limit(1)
    return db.execute(stmt).scalar_one_or_none()


def create_user(db: Session, *, email: str, name: str, password: str) -> User:
    user = User(
        email=email.lower(),
        name=name,
        password_hash=hash_password(password),
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    assign_default_flow_to_user(db, user)
    return user


def authenticate_user(db: Session, email: str, password: str) -> User | None:
    user = get_user_by_email(db, email)
    if user is None or not verify_password(password, user.password_hash):
        return None
    return user


def issue_token_pair(db: Session, user: User) -> TokenResponse:
    access, expires = create_access_token(user.id)

    refresh_jti = uuid.uuid4().hex
    refresh = create_refresh_token(user.id, refresh_jti)

    now = utcnow()
    refresh_exp = now + settings.refresh_ttl_delta

    db.add(
        RefreshSession(
            user_id=user.id,
            jti=refresh_jti,
            issued_at=now,
            expires_at=refresh_exp,
            revoked_at=None,
        )
    )
    db.commit()

    return TokenResponse(
        accessToken=access,
        refreshToken=refresh,
        expiresInSeconds=expires,
    )


def revoke_refresh_jti(db: Session, jti: str) -> None:
    stmt = select(RefreshSession).where(RefreshSession.jti == jti).limit(1)
    refresh_session = db.execute(stmt).scalar_one_or_none()
    if refresh_session and refresh_session.revoked_at is None:
        refresh_session.revoked_at = utcnow()
        db.commit()


def refresh_rotate(db: Session, refresh_token: str) -> TokenResponse:
    try:
        payload = decode_jwt(refresh_token)
    except jwt.ExpiredSignatureError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Refresh token expired",
        )
    except jwt.InvalidTokenError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid refresh token",
        )

    if payload.get("type") != "refresh":
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid refresh token type",
        )

    user_id = str(payload.get("sub"))
    jti = str(payload.get("jti") or "")
    if not jti:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Missing jti in refresh token",
        )

    stmt = select(RefreshSession).where(RefreshSession.jti == jti).limit(1)
    refresh_session = db.execute(stmt).scalar_one_or_none()
    if refresh_session is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Refresh token revoked or unknown",
        )

    if refresh_session.revoked_at is not None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Refresh token revoked",
        )

    if refresh_session.expires_at <= utcnow():
        refresh_session.revoked_at = utcnow()
        db.commit()
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Refresh token expired",
        )

    user = db.get(User, user_id)
    if user is None:
        refresh_session.revoked_at = utcnow()
        db.commit()
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User not found",
        )

    refresh_session.revoked_at = utcnow()
    db.commit()
    return issue_token_pair(db, user)


def logout_refresh_token(db: Session, refresh_token: str) -> None:
    try:
        payload = decode_jwt(refresh_token)
        if payload.get("type") == "refresh" and payload.get("jti"):
            revoke_refresh_jti(db, str(payload["jti"]))
    except Exception:
        pass
