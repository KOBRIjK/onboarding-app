package com.example.darkonboarding.data

enum class TaskStatus { DONE, IN_PROGRESS, NOT_STARTED }

data class TaskItem(
    val title: String,
    val subtitle: String,
    val status: TaskStatus
)

data class SourceItem(
    val title: String,
    val subtitle: String
)

data class DocSection(
    val title: String,
    val subtitle: String
)
