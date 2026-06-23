from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timedelta, timezone

from fastapi import HTTPException, status
from sqlalchemy import func, select
from sqlalchemy.orm import Session, selectinload

from ..core.config import settings
from ..core.security import hash_password, utcnow
from ..models import (
    OnboardingFlow,
    OnboardingStep,
    User,
    UserFlowAssignment,
    UserStepProgress,
)
from ..schemas.onboarding import (
    OnboardingFlowResponse,
    OnboardingProgressResponse,
    OnboardingReportResponse,
    OnboardingStepResponse,
)

DEFAULT_FLOW_KEY = "backend-developer"
TASK_NOT_STARTED = "not_started"
TASK_IN_PROGRESS = "in_progress"
TASK_DONE = "done"
ASSIGNMENT_IN_PROGRESS = "in_progress"
ASSIGNMENT_COMPLETED = "completed"


@dataclass(frozen=True)
class DefaultStep:
    title: str
    body: str
    cta_label: str
    cta_action: str
    due_days: int


DEFAULT_STEPS = [
    DefaultStep(
        title="Получить базовые доступы",
        body="Проверьте доступ к корпоративной почте, Slack, Jira, Confluence, GitHub и VPN.",
        cta_label="Открыть инструкцию",
        cta_action="docs:access",
        due_days=1,
    ),
    DefaultStep(
        title="Настроить рабочее окружение",
        body="Установите Python 3.12, Git, Docker Desktop, IDE и зависимости проекта.",
        cta_label="Проверить окружение",
        cta_action="docs:environment",
        due_days=2,
    ),
    DefaultStep(
        title="Изучить архитектуру проекта",
        body="Ознакомьтесь с backend API, RAG-модулем, Confluence и Android-клиентом.",
        cta_label="Открыть документацию",
        cta_action="docs:architecture",
        due_days=4,
    ),
    DefaultStep(
        title="Запустить backend локально",
        body="Создайте .env, установите requirements.txt и запустите python -m uvicorn main:app --reload.",
        cta_label="Запустить API",
        cta_action="backend:run",
        due_days=5,
    ),
    DefaultStep(
        title="Проверить AI-ассистента",
        body="Задайте вопрос на экране Ответ и убедитесь, что RAG отвечает по базе знаний Confluence.",
        cta_label="Задать вопрос",
        cta_action="rag:ask",
        due_days=7,
    ),
    DefaultStep(
        title="Пройти первое code review",
        body="Откройте pull request, получите комментарии наставника и внесите исправления.",
        cta_label="Открыть Git workflow",
        cta_action="docs:git",
        due_days=10,
    ),
]

DEMO_PROGRESS_BY_ORDER = {
    1: TASK_DONE,
    2: TASK_DONE,
    3: TASK_IN_PROGRESS,
}


def seed_default_onboarding(db: Session) -> OnboardingFlow:
    flow = _get_default_flow(db)
    if flow is None:
        flow = OnboardingFlow(
            key=DEFAULT_FLOW_KEY,
            name="Адаптация backend-разработчика",
            description="Маршрут адаптации нового сотрудника отдела разработки.",
            is_active=True,
        )
        db.add(flow)
        db.flush()
    else:
        flow.name = "Адаптация backend-разработчика"
        flow.description = "Маршрут адаптации нового сотрудника отдела разработки."
        flow.is_active = True

    existing_steps = {
        step.step_order: step
        for step in db.execute(
            select(OnboardingStep).where(OnboardingStep.flow_id == flow.id)
        ).scalars()
    }
    for index, step in enumerate(DEFAULT_STEPS, start=1):
        existing_step = existing_steps.get(index)
        metadata = {"due_days": step.due_days}
        if existing_step is None:
            db.add(
                OnboardingStep(
                    flow_id=flow.id,
                    step_order=index,
                    title=step.title,
                    body=step.body,
                    cta_label=step.cta_label,
                    cta_action=step.cta_action,
                    metadata_json=metadata,
                )
            )
        else:
            existing_step.title = step.title
            existing_step.body = step.body
            existing_step.cta_label = step.cta_label
            existing_step.cta_action = step.cta_action
            existing_step.metadata_json = {
                **(existing_step.metadata_json or {}),
                **metadata,
            }

    db.commit()
    db.refresh(flow)
    return flow


def seed_demo_onboarding(db: Session) -> User | None:
    if not settings.seed_demo_data:
        seed_default_onboarding(db)
        return None

    flow = seed_default_onboarding(db)
    email = settings.seed_user_email.lower()
    user = db.execute(
        select(User).where(func.lower(User.email) == email).limit(1)
    ).scalar_one_or_none()

    if user is None:
        user = User(
            email=email,
            name=settings.seed_user_name,
            password_hash=hash_password(settings.seed_user_password),
        )
        db.add(user)
        db.flush()
    else:
        user.name = settings.seed_user_name
        user.password_hash = hash_password(settings.seed_user_password)

    assignment = _get_assignment(db, user.id, flow.id)
    created_assignment = assignment is None
    demo_started_at = utcnow() - timedelta(days=settings.seed_user_started_days_ago)

    if assignment is None:
        assignment = UserFlowAssignment(
            user_id=user.id,
            flow_id=flow.id,
            status=ASSIGNMENT_IN_PROGRESS,
            started_at=demo_started_at,
        )
        db.add(assignment)
        db.flush()

    _sync_progress_items(db, assignment, flow)
    db.flush()

    assignment = _get_assignment(db, user.id, flow.id)
    if assignment is None:
        raise RuntimeError("Failed to seed demo onboarding assignment")

    if created_assignment or _progress_is_unmodified(assignment):
        _apply_demo_progress(assignment, demo_started_at)

    db.commit()
    db.refresh(user)
    return user


def assign_default_flow_to_user(db: Session, user: User) -> UserFlowAssignment:
    flow = seed_default_onboarding(db)
    assignment = _get_assignment(db, user.id, flow.id)
    if assignment is None:
        assignment = UserFlowAssignment(
            user_id=user.id,
            flow_id=flow.id,
            status=ASSIGNMENT_IN_PROGRESS,
            started_at=utcnow(),
        )
        db.add(assignment)
        db.flush()

    _sync_progress_items(db, assignment, flow)
    db.commit()
    db.refresh(assignment)
    return assignment


def get_user_flow(db: Session, user_id: str) -> OnboardingFlowResponse:
    assignment = _get_or_create_user_assignment(db, user_id)
    return _build_flow_response(assignment)


def get_user_tasks(db: Session, user_id: str) -> list[OnboardingStepResponse]:
    assignment = _get_or_create_user_assignment(db, user_id)
    progress_by_step_id = {item.step_id: item for item in assignment.progress_items}
    now = utcnow()

    return [
        _build_step_response(
            step,
            progress_by_step_id[step.id],
            assignment_started_at=assignment.started_at,
            now=now,
        )
        for step in sorted(assignment.flow.steps, key=lambda item: item.step_order)
    ]


def get_user_progress(db: Session, user_id: str) -> OnboardingProgressResponse:
    assignment = _get_or_create_user_assignment(db, user_id)
    report = _calculate_progress_report(assignment)

    return OnboardingProgressResponse(
        total=report["total"],
        done=report["done"],
        inProgress=report["in_progress"],
        notStarted=report["not_started"],
        overdue=report["overdue"],
        percent=report["percent"],
        assignmentStatus=assignment.status,
    )


def get_user_report(db: Session, user_id: str) -> OnboardingReportResponse:
    assignment = _get_or_create_user_assignment(db, user_id)
    report = _calculate_progress_report(assignment)
    now = utcnow()
    progress_by_step_id = {item.step_id: item for item in assignment.progress_items}
    overdue_tasks = [
        _build_step_response(
            step,
            progress_by_step_id[step.id],
            assignment_started_at=assignment.started_at,
            now=now,
        )
        for step in sorted(assignment.flow.steps, key=lambda item: item.step_order)
        if _is_step_overdue(step, progress_by_step_id[step.id], assignment.started_at, now)
    ]

    return OnboardingReportResponse(
        flowKey=assignment.flow.key,
        flowName=assignment.flow.name,
        assignmentStatus=assignment.status,
        startedAt=assignment.started_at,
        completedAt=assignment.completed_at,
        totalTasks=report["total"],
        completedTasks=report["done"],
        inProgressTasks=report["in_progress"],
        notStartedTasks=report["not_started"],
        overdueTasks=report["overdue"],
        percent=report["percent"],
        overdueTaskItems=overdue_tasks,
    )


def update_user_step_status(
    db: Session,
    *,
    user_id: str,
    step_id: str,
    new_status: str,
    notes: str | None,
) -> OnboardingStepResponse:
    assignment = _get_or_create_user_assignment(db, user_id)
    progress = next(
        (item for item in assignment.progress_items if item.step_id == step_id),
        None,
    )
    if progress is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Onboarding step not found for current user",
        )

    now = utcnow()
    progress.status = new_status
    progress.notes = notes

    if new_status == TASK_NOT_STARTED:
        progress.started_at = None
        progress.completed_at = None
    elif new_status == TASK_IN_PROGRESS:
        progress.started_at = progress.started_at or now
        progress.completed_at = None
    elif new_status == TASK_DONE:
        progress.started_at = progress.started_at or now
        progress.completed_at = now

    if all(item.status == TASK_DONE for item in assignment.progress_items):
        assignment.status = ASSIGNMENT_COMPLETED
        assignment.completed_at = now
    else:
        assignment.status = ASSIGNMENT_IN_PROGRESS
        assignment.completed_at = None

    db.commit()
    db.refresh(progress)
    db.refresh(assignment)
    return _build_step_response(
        progress.step,
        progress,
        assignment_started_at=assignment.started_at,
    )


def _calculate_progress_report(assignment: UserFlowAssignment) -> dict[str, int]:
    statuses = [item.status for item in assignment.progress_items]
    total = len(statuses)
    done = statuses.count(TASK_DONE)
    in_progress = statuses.count(TASK_IN_PROGRESS)
    not_started = statuses.count(TASK_NOT_STARTED)
    now = utcnow()
    overdue = sum(
        1
        for item in assignment.progress_items
        if _is_step_overdue(item.step, item, assignment.started_at, now)
    )
    percent = int(round((done / total) * 100)) if total else 0

    return {
        "total": total,
        "done": done,
        "in_progress": in_progress,
        "not_started": not_started,
        "overdue": overdue,
        "percent": percent,
    }


def _progress_is_unmodified(assignment: UserFlowAssignment) -> bool:
    return all(
        item.status == TASK_NOT_STARTED
        and item.started_at is None
        and item.completed_at is None
        and item.notes is None
        for item in assignment.progress_items
    )


def _apply_demo_progress(
    assignment: UserFlowAssignment,
    demo_started_at: datetime,
) -> None:
    started_at = _as_utc(demo_started_at)
    assignment.started_at = started_at
    assignment.status = ASSIGNMENT_IN_PROGRESS
    assignment.completed_at = None

    for item in assignment.progress_items:
        status_value = DEMO_PROGRESS_BY_ORDER.get(item.step.step_order, TASK_NOT_STARTED)
        item.status = status_value
        item.notes = None

        if status_value == TASK_DONE:
            item.started_at = started_at
            item.completed_at = started_at + timedelta(days=item.step.step_order)
        elif status_value == TASK_IN_PROGRESS:
            item.started_at = started_at + timedelta(days=item.step.step_order)
            item.completed_at = None
        else:
            item.started_at = None
            item.completed_at = None


def _get_default_flow(db: Session) -> OnboardingFlow | None:
    return db.execute(
        select(OnboardingFlow).where(OnboardingFlow.key == DEFAULT_FLOW_KEY).limit(1)
    ).scalar_one_or_none()


def _get_assignment(
    db: Session,
    user_id: str,
    flow_id: str,
) -> UserFlowAssignment | None:
    return db.execute(
        select(UserFlowAssignment)
        .where(
            UserFlowAssignment.user_id == user_id,
            UserFlowAssignment.flow_id == flow_id,
        )
        .options(
            selectinload(UserFlowAssignment.flow).selectinload(OnboardingFlow.steps),
            selectinload(UserFlowAssignment.progress_items).selectinload(UserStepProgress.step),
        )
        .execution_options(populate_existing=True)
        .limit(1)
    ).scalar_one_or_none()


def _get_or_create_user_assignment(db: Session, user_id: str) -> UserFlowAssignment:
    user = db.get(User, user_id)
    if user is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid user",
        )

    assignment = assign_default_flow_to_user(db, user)
    return db.execute(
        select(UserFlowAssignment)
        .where(UserFlowAssignment.id == assignment.id)
        .options(
            selectinload(UserFlowAssignment.flow).selectinload(OnboardingFlow.steps),
            selectinload(UserFlowAssignment.progress_items).selectinload(UserStepProgress.step),
        )
        .execution_options(populate_existing=True)
    ).scalar_one()


def _sync_progress_items(
    db: Session,
    assignment: UserFlowAssignment,
    flow: OnboardingFlow,
) -> None:
    existing_step_ids = {
        item.step_id
        for item in db.execute(
            select(UserStepProgress).where(
                UserStepProgress.assignment_id == assignment.id
            )
        ).scalars()
    }

    steps = db.execute(
        select(OnboardingStep).where(OnboardingStep.flow_id == flow.id)
    ).scalars()
    for step in steps:
        if step.id not in existing_step_ids:
            db.add(
                UserStepProgress(
                    assignment_id=assignment.id,
                    step_id=step.id,
                    status=TASK_NOT_STARTED,
                )
            )


def _build_flow_response(assignment: UserFlowAssignment) -> OnboardingFlowResponse:
    progress_by_step_id = {item.step_id: item for item in assignment.progress_items}
    now = utcnow()
    steps = [
        _build_step_response(
            step,
            progress_by_step_id[step.id],
            assignment_started_at=assignment.started_at,
            now=now,
        )
        for step in sorted(assignment.flow.steps, key=lambda item: item.step_order)
    ]

    return OnboardingFlowResponse(
        id=assignment.flow.id,
        key=assignment.flow.key,
        name=assignment.flow.name,
        description=assignment.flow.description,
        assignmentId=assignment.id,
        status=assignment.status,
        startedAt=assignment.started_at,
        completedAt=assignment.completed_at,
        steps=steps,
    )


def _build_step_response(
    step: OnboardingStep,
    progress: UserStepProgress,
    *,
    assignment_started_at: datetime,
    now: datetime | None = None,
) -> OnboardingStepResponse:
    due_days = _get_step_due_days(step)
    due_at = _calculate_due_at(assignment_started_at, due_days)
    is_overdue = _is_step_overdue(
        step,
        progress,
        assignment_started_at,
        now or utcnow(),
    )

    return OnboardingStepResponse(
        id=step.id,
        order=step.step_order,
        title=step.title,
        body=step.body,
        ctaLabel=step.cta_label,
        ctaAction=step.cta_action,
        dueDays=due_days,
        dueAt=due_at,
        isOverdue=is_overdue,
        status=progress.status,
        startedAt=progress.started_at,
        completedAt=progress.completed_at,
        notes=progress.notes,
    )


def _get_step_due_days(step: OnboardingStep) -> int | None:
    value = (step.metadata_json or {}).get("due_days")
    if value is None:
        return None

    try:
        return int(value)
    except (TypeError, ValueError):
        return None


def _calculate_due_at(
    assignment_started_at: datetime,
    due_days: int | None,
) -> datetime | None:
    if due_days is None:
        return None
    return _as_utc(assignment_started_at) + timedelta(days=due_days)


def _is_step_overdue(
    step: OnboardingStep,
    progress: UserStepProgress,
    assignment_started_at: datetime,
    now: datetime,
) -> bool:
    if progress.status == TASK_DONE:
        return False

    due_at = _calculate_due_at(assignment_started_at, _get_step_due_days(step))
    if due_at is None:
        return False

    return due_at < _as_utc(now)


def _as_utc(value: datetime) -> datetime:
    if value.tzinfo is None:
        return value.replace(tzinfo=timezone.utc)
    return value.astimezone(timezone.utc)
