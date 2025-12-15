package com.example.darkonboarding.ui.screens.home

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
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Workspaces
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
fun HomeScreen() {
    val recent = listOf(
        "Как получить доступ к репозиторию?",
        "Кто мой тимлид?",
        "Процесс code review"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Добро пожаловать!", color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Text("Задавайте вопросы и изучайте компанию с помощью\nAI-ассистента", color = TextSecondary)
        }

        item { SearchBarStub() }

        item {
            Text("Недавние вопросы", color = TextSecondary, modifier = Modifier.padding(top = 6.dp))
        }

        items(recent) { q ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) { Text(q, color = TextPrimary) }
        }

        item {
            Text("Быстрые темы", color = TextSecondary, modifier = Modifier.padding(top = 6.dp))
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickTopicCard("Как устроена\nкоманда", Icons.Default.People, Modifier.weight(1f))
                    QuickTopicCard("Старт проекта", Icons.Default.RocketLaunch, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickTopicCard("Инфраструктура", Icons.Default.Storage, Modifier.weight(1f))
                    QuickTopicCard("Процессы\nкомпании", Icons.Default.Workspaces, Modifier.weight(1f))
                }
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
