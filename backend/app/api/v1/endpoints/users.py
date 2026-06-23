from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from ....api.deps import require_access_user
from ....db.session import db_session
from ....models import User
from ....schemas.user import AuthenticatedUser, UserResponse

router = APIRouter()


@router.get("/me", response_model=UserResponse)
def me(
    user: AuthenticatedUser = Depends(require_access_user),
    db: Session = Depends(db_session),
) -> UserResponse:
    db_user = db.get(User, user.user_id)
    if db_user is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid user",
        )

    return UserResponse(id=db_user.id, email=db_user.email, name=db_user.name)
