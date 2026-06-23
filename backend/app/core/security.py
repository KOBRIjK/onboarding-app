from __future__ import annotations

from datetime import datetime, timedelta, timezone

import jwt
from passlib.context import CryptContext

from .config import settings

ALGORITHM = "HS512"

try:
    from argon2 import PasswordHasher
    from argon2.exceptions import VerifyMismatchError

    _argon2 = PasswordHasher(
        time_cost=3,
        memory_cost=64 * 1024,
        parallelism=2,
        hash_len=32,
        salt_len=16,
    )
    USE_ARGON2 = True
except Exception:
    USE_ARGON2 = False

_pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def utcnow() -> datetime:
    return datetime.now(timezone.utc)


def hash_password(password: str) -> str:
    if USE_ARGON2:
        return "argon2id$" + _argon2.hash(password)
    return "bcrypt$" + _pwd_context.hash(password)


def verify_password(password: str, password_hash: str) -> bool:
    if password_hash.startswith("argon2id$"):
        if not USE_ARGON2:
            return False
        try:
            _argon2.verify(password_hash.removeprefix("argon2id$"), password)
            return True
        except VerifyMismatchError:
            return False
        except Exception:
            return False

    if password_hash.startswith("bcrypt$"):
        return _pwd_context.verify(password, password_hash.removeprefix("bcrypt$"))

    return False


def create_access_token(user_id: str) -> tuple[str, int]:
    now = utcnow()
    exp = now + timedelta(minutes=settings.access_ttl_minutes)

    payload = {
        "sub": user_id,
        "iss": settings.jwt_issuer,
        "iat": int(now.timestamp()),
        "exp": int(exp.timestamp()),
    }
    if settings.jwt_audience:
        payload["aud"] = settings.jwt_audience

    token = jwt.encode(payload, settings.jwt_secret, algorithm=ALGORITHM)
    return token, int((exp - now).total_seconds())


def create_refresh_token(user_id: str, jti: str) -> str:
    now = utcnow()
    exp = now + timedelta(days=settings.refresh_ttl_days)
    payload = {
        "sub": user_id,
        "iss": settings.jwt_issuer,
        "iat": int(now.timestamp()),
        "exp": int(exp.timestamp()),
        "jti": jti,
        "type": "refresh",
    }
    if settings.jwt_audience:
        payload["aud"] = settings.jwt_audience

    return jwt.encode(payload, settings.jwt_secret, algorithm=ALGORITHM)


def decode_jwt(token: str) -> dict:
    kwargs = {
        "key": settings.jwt_secret,
        "algorithms": [ALGORITHM],
        "issuer": settings.jwt_issuer,
        "options": {"require": ["exp", "iss", "sub"]},
    }
    if settings.jwt_audience:
        kwargs["audience"] = settings.jwt_audience

    return jwt.decode(token, **kwargs)
