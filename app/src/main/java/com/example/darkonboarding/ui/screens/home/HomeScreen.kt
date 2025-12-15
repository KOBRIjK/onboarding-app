package com.example.darkonboarding.ui.screens.home

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.darkonboarding.ui.components.GlassCard
import com.example.darkonboarding.ui.theme.TextPrimary
import com.example.darkonboarding.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onQuestionClick: (String) -> Unit
) {
    val recentQuestions = listOf(
        "Как получить доступ к репозиторию?",
        "Кто мой тимлид?",
        "Процесс code review"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 22.dp,
            bottom = 110.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ===== Заголовок =====
        item {
            Text(
                text = "Добро пожаловать!",
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Задавайте вопросы и изучайте компанию с помощью AI-ассистента",
                color = TextSecondary
            )
        }

        // ===== Поиск (заглушка) =====
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                padding = PaddingValues(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Задай вопрос, чтобы начать...",
                        color = TextSecondary
                    )
                }
            }
        }

        // ===== Недавние вопросы =====
        item {
            Text(
                text = "Недавние вопросы",
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(recentQuestions) { question ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onQuestionClick(question)
                    },
                padding = PaddingValues(16.dp)
            ) {
                Text(
                    text = question,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun SearchBarStub() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        corner = 18.dp,
        padding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = Color(0xFF222A3A),
                shape = RoundedCornerShape(14.dp)
            ) {
                androidx.compose.foundation.layout.Box(Modifier.padding(10.dp)) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Text("Задай вопрос, чтобы начать...", color = TextSecondary)
        }
    }
}

@Composable
private fun QuickTopicCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.height(124.dp),
        corner = 18.dp,
        padding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
            Surface(
                color = Color(0xFF222A3A),
                shape = RoundedCornerShape(16.dp)
            ) {
                androidx.compose.foundation.layout.Box(Modifier.padding(10.dp)) {
                    Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                }
            }
            Text(title, color = TextPrimary)
        }
    }
}
