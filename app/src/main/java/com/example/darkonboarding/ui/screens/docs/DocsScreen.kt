package com.example.darkonboarding.ui.screens.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.darkonboarding.ui.components.GlassCard
import com.example.darkonboarding.ui.components.SimpleRowItem
import com.example.darkonboarding.ui.theme.TextPrimary
import com.example.darkonboarding.ui.theme.TextSecondary

@Composable
fun DocsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Документация", color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Text("Изучите основные разделы компании", color = TextSecondary)
        }

        item {
            SimpleRowItem(
                title = "Команда",
                subtitle = "Структура, роли и контакты",
                leading = { Icon(Icons.Default.People, contentDescription = null, tint = TextPrimary) }
            )
        }
        item {
            SimpleRowItem(
                title = "Проект",
                subtitle = "Обзор продукта и roadmap",
                leading = { Icon(Icons.Default.Work, contentDescription = null, tint = TextPrimary) }
            )
        }
        item {
            SimpleRowItem(
                title = "Микросервисы",
                subtitle = "Архитектура и взаимодействие",
                leading = { Icon(Icons.Default.ViewModule, contentDescription = null, tint = TextPrimary) }
            )
        }
        item {
            SimpleRowItem(
                title = "Инфраструктура",
                subtitle = "CI/CD, мониторинг,\nлогирование",
                leading = { Icon(Icons.Default.Storage, contentDescription = null, tint = TextPrimary) }
            )
        }
        item {
            SimpleRowItem(
                title = "Данные",
                subtitle = "Базы данных и схемы",
                leading = { Icon(Icons.Default.Timeline, contentDescription = null, tint = TextPrimary) }
            )
        }

        item {
            GlassCard(padding = PaddingValues(16.dp)) {
                androidx.compose.foundation.layout.Row {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = TextSecondary)
                    Spacer(Modifier.padding(6.dp))
                    Text(
                        "Воспользуйтесь поиском на главном\nэкране, чтобы быстро найти нужную\nинформацию по всем разделам\nдокументации.",
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
