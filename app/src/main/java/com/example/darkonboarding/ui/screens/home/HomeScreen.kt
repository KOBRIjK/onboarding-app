package com.example.darkonboarding.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.darkonboarding.ui.components.GlassCard
import com.example.darkonboarding.ui.components.GradientCardButton
import com.example.darkonboarding.ui.theme.TextPrimary
import com.example.darkonboarding.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onQuestionClick: (String) -> Unit,
    onSearchClick: () -> Unit = {}
) {
    val recentQuestions = listOf(
        "Как получить доступ к репозиторию?",
        "Кто мой тимлид?",
        "Процесс code review"
    )

    LazyColumn(
        modifier = Modifier.padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // ───────── Welcome ─────────
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Добро пожаловать!", color = TextPrimary)
                Text(
                    "Задавайте вопросы и изучайте компанию с помощью AI-ассистента",
                    color = TextSecondary
                )
            }
        }

        // ───────── Search ─────────
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSearchClick() },
                padding = PaddingValues(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                    Text(
                        text = "Задай вопрос, чтобы начать...",
                        color = TextSecondary
                    )
                }
            }
        }

        // ───────── Recent questions ─────────
        item {
            Text("Недавние вопросы", color = TextSecondary)
        }

        items(recentQuestions) { question ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onQuestionClick(question) },
                padding = PaddingValues(14.dp)
            ) {
                Text(text = question, color = TextPrimary)
            }
        }

        // ───────── Quick topics ─────────
        item {
            Text("Быстрые темы", color = TextSecondary)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GradientCardButton(
                        title = "Как устроена команда",
                        icon = Icons.Default.Group,
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp),
                        onClick = {
                            onQuestionClick("Как устроена команда")
                        }
                    )

                    GradientCardButton(
                        title = "Старт проекта",
                        icon = Icons.Default.RocketLaunch,
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp),
                        onClick = {
                            onQuestionClick("Старт проекта")
                        }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GradientCardButton(
                        title = "Инфраструктура",
                        icon = Icons.Default.Storage,
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp),
                        onClick = {
                            onQuestionClick("Инфраструктура")
                        }
                    )

                    GradientCardButton(
                        title = "Процессы компании",
                        icon = Icons.Default.Work,
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp),
                        onClick = {
                            onQuestionClick("Процессы компании")
                        }
                    )
                }
            }
        }
    }
}
