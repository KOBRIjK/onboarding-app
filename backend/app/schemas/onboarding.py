from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


class OnboardingStepResponse(BaseModel):
    id: str
    order: int
    title: str
    body: str
    ctaLabel: Optional[str]
    ctaAction: Optional[str]
    dueDays: Optional[int]
    dueAt: Optional[datetime]
    isOverdue: bool
    status: str
    startedAt: Optional[datetime]
    completedAt: Optional[datetime]
    notes: Optional[str]


class OnboardingFlowResponse(BaseModel):
    id: str
    key: str
    name: str
    description: str
    assignmentId: str
    status: str
    startedAt: datetime
    completedAt: Optional[datetime]
    steps: list[OnboardingStepResponse]


class OnboardingTaskStatusRequest(BaseModel):
    status: str = Field(pattern="^(not_started|in_progress|done)$")
    notes: Optional[str] = None


class OnboardingProgressResponse(BaseModel):
    total: int
    done: int
    inProgress: int
    notStarted: int
    overdue: int
    percent: int
    assignmentStatus: str


class OnboardingReportResponse(BaseModel):
    flowKey: str
    flowName: str
    assignmentStatus: str
    startedAt: datetime
    completedAt: Optional[datetime]
    totalTasks: int
    completedTasks: int
    inProgressTasks: int
    notStartedTasks: int
    overdueTasks: int
    percent: int
    overdueTaskItems: list[OnboardingStepResponse]
