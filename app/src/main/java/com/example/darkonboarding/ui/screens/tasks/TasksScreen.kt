package com.example.darkonboarding.ui.screens.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.darkonboarding.data.onboarding.OnboardingApiClient
import com.example.darkonboarding.data.onboarding.OnboardingProgress
import com.example.darkonboarding.data.onboarding.OnboardingTask
import com.example.darkonboarding.ui.components.GlassCard
import com.example.darkonboarding.ui.components.GradientProgressBar
import com.example.darkonboarding.ui.theme.AccentCyan
import com.example.darkonboarding.ui.theme.AccentGray
import com.example.darkonboarding.ui.theme.AccentGreen
import com.example.darkonboarding.ui.theme.TextPrimary
import com.example.darkonboarding.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(accessToken: String) {
    var tasks by remember { mutableStateOf<List<OnboardingTask>>(emptyList()) }
    var progress by remember { mutableStateOf<OnboardingProgress?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var updatingStepId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun loadTasks() {
        isLoading = true
        error = null
        runCatching {
            val loadedTasks = OnboardingApiClient.getTasks(accessToken)
            val loadedProgress = OnboardingApiClient.getProgress(accessToken)
            loadedTasks to loadedProgress
        }.onSuccess { (loadedTasks, loadedProgress) ->
            tasks = loadedTasks
            progress = loadedProgress
        }.onFailure { throwable ->
            error = throwable.message ?: "Не удалось загрузить задачи адаптации."
        }
        isLoading = false
    }

    fun updateStatus(task: OnboardingTask) {
        if (updatingStepId != null) return

        scope.launch {
            updatingStepId = task.id
            error = null
            runCatching {
                val updatedTask = OnboardingApiClient.updateTaskStatus(
                    accessToken = accessToken,
                    stepId = task.id,
                    status = nextStatus(task.status)
                )
                val updatedProgress = OnboardingApiClient.getProgress(accessToken)
                updatedTask to updatedProgress
            }.onSuccess { (updatedTask, updatedProgress) ->
                tasks = tasks.map { current ->
                    if (current.id == updatedTask.id) updatedTask else current
                }
                progress = updatedProgress
            }.onFailure { throwable ->
                error = throwable.message ?: "Не удалось обновить статус задачи."
            }
            updatingStepId = null
        }
    }

    LaunchedEffect(accessToken) {
        loadTasks()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Онбординг", color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Text("Нажмите на задачу, чтобы сменить её статус", color = TextSecondary)
        }

        item {
            ProgressCard(progress = progress)
        }

        if (isLoading) {
            item {
                GlassCard(padding = PaddingValues(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(22.dp),
                            color = TextPrimary,
                            strokeWidth = 2.dp
                        )
                        Text("Загружаю маршрут адаптации...", color = TextSecondary)
                    }
                }
            }
        }

        if (error != null) {
            item {
                GlassCard(padding = PaddingValues(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(error.orEmpty(), color = Color(0xFFFF8A8A))
                        Text(
                            text = "Проверьте, что backend запущен и токен авторизации актуален.",
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        items(tasks, key = { it.id }) { task ->
            val taskStatus = toTaskStatus(task.status)
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = updatingStepId == null) {
                        updateStatus(task)
                    },
                padding = PaddingValues(16.dp),
                gradientStroke = taskStatus == UiTaskStatus.IN_PROGRESS
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(task.title, color = TextPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text(task.body, color = TextSecondary)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            task.dueDays?.let { dueDays ->
                                SmallInfoChip(
                                    text = "Срок: $dueDays дн.",
                                    color = AccentGray
                                )
                            }
                            if (task.isOverdue) {
                                SmallInfoChip(
                                    text = "Просрочено",
                                    color = Color(0xFFFF8A8A)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    if (updatingStepId == task.id) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(22.dp),
                            color = TextPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        StatusChip(taskStatus)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(progress: OnboardingProgress?) {
    val total = progress?.total ?: 0
    val done = progress?.done ?: 0
    val overdue = progress?.overdue ?: 0
    val percent = progress?.percent ?: 0

    GlassCard(padding = PaddingValues(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Прогресс", color = TextSecondary)
            Text("$percent%", color = TextSecondary)
        }
        Spacer(Modifier.height(10.dp))
        GradientProgressBar(progress = percent / 100f)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Задачи: $done / $total", color = TextSecondary)
            Text("Просрочено: $overdue", color = if (overdue > 0) Color(0xFFFF8A8A) else TextSecondary)
        }
    }
}

@Composable
private fun StatusChip(status: UiTaskStatus) {
    val (text, color, icon) = when (status) {
        UiTaskStatus.DONE -> Triple("Готово", AccentGreen, Icons.Default.CheckCircle)
        UiTaskStatus.IN_PROGRESS -> Triple("В процессе", AccentCyan, Icons.Default.Sync)
        UiTaskStatus.NOT_STARTED -> Triple("Не начато", AccentGray, Icons.Default.AccessTime)
    }

    Surface(
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(Modifier.width(8.dp))
            Text(text, color = color)
        }
    }
}

@Composable
private fun SmallInfoChip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

private enum class UiTaskStatus {
    DONE,
    IN_PROGRESS,
    NOT_STARTED
}

private fun toTaskStatus(status: String): UiTaskStatus =
    when (status) {
        "done" -> UiTaskStatus.DONE
        "in_progress" -> UiTaskStatus.IN_PROGRESS
        else -> UiTaskStatus.NOT_STARTED
    }

private fun nextStatus(status: String): String =
    when (status) {
        "not_started" -> "in_progress"
        "in_progress" -> "done"
        else -> "not_started"
    }
