from __future__ import annotations

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from ....api.deps import require_access_user
from ....db.session import db_session
from ....schemas.onboarding import (
    OnboardingFlowResponse,
    OnboardingProgressResponse,
    OnboardingReportResponse,
    OnboardingStepResponse,
    OnboardingTaskStatusRequest,
)
from ....schemas.user import AuthenticatedUser
from ....services.onboarding import (
    get_user_flow,
    get_user_progress,
    get_user_report,
    get_user_tasks,
    update_user_step_status,
)

router = APIRouter()


@router.get("/flow", response_model=OnboardingFlowResponse)
def flow(
    user: AuthenticatedUser = Depends(require_access_user),
    db: Session = Depends(db_session),
) -> OnboardingFlowResponse:
    return get_user_flow(db, user.user_id)


@router.get("/tasks", response_model=list[OnboardingStepResponse])
def tasks(
    user: AuthenticatedUser = Depends(require_access_user),
    db: Session = Depends(db_session),
) -> list[OnboardingStepResponse]:
    return get_user_tasks(db, user.user_id)


@router.post("/tasks/{step_id}/status", response_model=OnboardingStepResponse)
def update_task_status(
    step_id: str,
    req: OnboardingTaskStatusRequest,
    user: AuthenticatedUser = Depends(require_access_user),
    db: Session = Depends(db_session),
) -> OnboardingStepResponse:
    return update_user_step_status(
        db,
        user_id=user.user_id,
        step_id=step_id,
        new_status=req.status,
        notes=req.notes,
    )


@router.get("/progress", response_model=OnboardingProgressResponse)
def progress(
    user: AuthenticatedUser = Depends(require_access_user),
    db: Session = Depends(db_session),
) -> OnboardingProgressResponse:
    return get_user_progress(db, user.user_id)


@router.get("/report", response_model=OnboardingReportResponse)
def report(
    user: AuthenticatedUser = Depends(require_access_user),
    db: Session = Depends(db_session),
) -> OnboardingReportResponse:
    return get_user_report(db, user.user_id)
