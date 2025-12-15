package com.example.darkonboarding.ui.screens.tasks

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.darkonboarding.data.TaskItem
import com.example.darkonboarding.data.TaskStatus
import com.example.darkonboarding.ui.components.GlassCard
import com.example.darkonboarding.ui.components.GradientProgressBar
import com.example.darkonboarding.ui.theme.AccentCyan
import com.example.darkonboarding.ui.theme.AccentGray
import com.example.darkonboarding.ui.theme.AccentGreen
import com.example.darkonboarding.ui.theme.TextPrimary
import com.example.darkonboarding.ui.theme.TextSecondary

@Composable
fun TasksScreen() {
    val tasks = listOf(
        TaskItem("Установить окружение", "Настроить IDE, установить\nзависимости проекта", TaskStatus.DONE),
        TaskItem("Получить доступы", "GitHub, Jira, Confluence,\nкорпоративная почта", TaskStatus.IN_PROGRESS),
        TaskItem("Изучить архитектуру", "Ознакомиться с\nмикросервисами и\nструктурой проекта", TaskStatus.IN_PROGRESS),
        TaskItem("Первый code review", "Провести ревью кода коллег", TaskStatus.NOT_STARTED),
        TaskItem("Деплой тестового\nфичера", "Задеплоить изменения в\nstaging окружение", TaskStatus.NOT_STARTED),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Онбординг", color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Text("Выполните задачи для успешного старта", color = TextSecondary)
        }

        item {
            GlassCard(padding = PaddingValues(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Прогресс", color = TextSecondary)
                    Text("1 / 5", color = TextSecondary)
                }
                Spacer(Modifier.height(10.dp))
                GradientProgressBar(progress = 0.2f)
            }
        }

        items(tasks) { t ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                padding = PaddingValues(16.dp),
                gradientStroke = (t.status == TaskStatus.IN_PROGRESS)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(t.title, color = TextPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text(t.subtitle, color = TextSecondary)
                    }
                    Spacer(Modifier.width(10.dp))
                    StatusChip(t.status)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: TaskStatus) {
    val (text, color, icon) = when (status) {
        TaskStatus.DONE -> Triple("Готово", AccentGreen, Icons.Default.CheckCircle)
        TaskStatus.IN_PROGRESS -> Triple("В процессе", AccentCyan, Icons.Default.Sync)
        TaskStatus.NOT_STARTED -> Triple("Не начато", AccentGray, Icons.Default.AccessTime)
    }

    androidx.compose.material3.Surface(
        color = color.copy(alpha = 0.16f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.55f))
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
