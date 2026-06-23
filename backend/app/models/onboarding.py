from __future__ import annotations

import uuid
from datetime import datetime, timezone
from typing import Optional

from sqlalchemy import Boolean, DateTime, ForeignKey, Integer, JSON, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from ..db.base import Base


def utcnow() -> datetime:
    return datetime.now(timezone.utc)


class OnboardingFlow(Base):
    __tablename__ = "onboarding_flows"
    __table_args__ = (UniqueConstraint("key", name="uq_onboarding_flows_key"),)

    id: Mapped[str] = mapped_column(
        String(36),
        primary_key=True,
        default=lambda: str(uuid.uuid4()),
    )
    key: Mapped[str] = mapped_column(String(100), nullable=False)
    name: Mapped[str] = mapped_column(String(200), nullable=False)
    description: Mapped[str] = mapped_column(Text, nullable=False, default="")
    is_active: Mapped[bool] = mapped_column(Boolean, nullable=False, default=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        default=utcnow,
        onupdate=utcnow,
    )

    steps: Mapped[list["OnboardingStep"]] = relationship(
        back_populates="flow",
        cascade="all, delete-orphan",
        order_by="OnboardingStep.step_order",
    )
    assignments: Mapped[list["UserFlowAssignment"]] = relationship(
        back_populates="flow",
        cascade="all, delete-orphan",
    )


class OnboardingStep(Base):
    __tablename__ = "onboarding_steps"
    __table_args__ = (
        UniqueConstraint("flow_id", "step_order", name="uq_onboarding_steps_flow_order"),
    )

    id: Mapped[str] = mapped_column(
        String(36),
        primary_key=True,
        default=lambda: str(uuid.uuid4()),
    )
    flow_id: Mapped[str] = mapped_column(
        String(36),
        ForeignKey("onboarding_flows.id", ondelete="CASCADE"),
        nullable=False,
    )
    step_order: Mapped[int] = mapped_column(Integer, nullable=False)
    title: Mapped[str] = mapped_column(String(200), nullable=False)
    body: Mapped[str] = mapped_column(Text, nullable=False, default="")
    cta_label: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    cta_action: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    metadata_json: Mapped[dict] = mapped_column("metadata", JSON, nullable=False, default=dict)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        default=utcnow,
        onupdate=utcnow,
    )

    flow: Mapped["OnboardingFlow"] = relationship(back_populates="steps")
    progress_items: Mapped[list["UserStepProgress"]] = relationship(
        back_populates="step",
        cascade="all, delete-orphan",
    )


class UserFlowAssignment(Base):
    __tablename__ = "user_flow_assignments"
    __table_args__ = (
        UniqueConstraint("user_id", "flow_id", name="uq_user_flow_assignments_user_flow"),
    )

    id: Mapped[str] = mapped_column(
        String(36),
        primary_key=True,
        default=lambda: str(uuid.uuid4()),
    )
    user_id: Mapped[str] = mapped_column(
        String(36),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
    )
    flow_id: Mapped[str] = mapped_column(
        String(36),
        ForeignKey("onboarding_flows.id", ondelete="CASCADE"),
        nullable=False,
    )
    status: Mapped[str] = mapped_column(String(30), nullable=False, default="in_progress")
    started_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utcnow)
    completed_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        default=utcnow,
        onupdate=utcnow,
    )

    user: Mapped["User"] = relationship(back_populates="onboarding_assignments")
    flow: Mapped["OnboardingFlow"] = relationship(back_populates="assignments")
    progress_items: Mapped[list["UserStepProgress"]] = relationship(
        back_populates="assignment",
        cascade="all, delete-orphan",
    )


class UserStepProgress(Base):
    __tablename__ = "user_step_progress"
    __table_args__ = (
        UniqueConstraint("assignment_id", "step_id", name="uq_user_step_progress_assignment_step"),
    )

    id: Mapped[str] = mapped_column(
        String(36),
        primary_key=True,
        default=lambda: str(uuid.uuid4()),
    )
    assignment_id: Mapped[str] = mapped_column(
        String(36),
        ForeignKey("user_flow_assignments.id", ondelete="CASCADE"),
        nullable=False,
    )
    step_id: Mapped[str] = mapped_column(
        String(36),
        ForeignKey("onboarding_steps.id", ondelete="CASCADE"),
        nullable=False,
    )
    status: Mapped[str] = mapped_column(String(30), nullable=False, default="not_started")
    started_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    completed_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    notes: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False, default=utcnow)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        default=utcnow,
        onupdate=utcnow,
    )

    assignment: Mapped["UserFlowAssignment"] = relationship(back_populates="progress_items")
    step: Mapped["OnboardingStep"] = relationship(back_populates="progress_items")
