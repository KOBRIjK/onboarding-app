package com.example.darkonboarding.ui.screens.answer

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.darkonboarding.data.SourceItem
import com.example.darkonboarding.ui.components.GlassCard
import com.example.darkonboarding.ui.components.GradientPillButton
import com.example.darkonboarding.ui.theme.TextPrimary
import com.example.darkonboarding.ui.theme.TextSecondary

@Composable
fun AnswerScreen() {
    val sources = listOf(
        SourceItem("Confluence", "confluence.company.com/wiki"),
        SourceItem("Git", "github.company.com/docs"),
        SourceItem("OpenMetadata", "metadata.company.com"),
    )

    var expanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassCard(padding = PaddingValues(16.dp)) {
                Text("Я    Как устроена команда разработки?", color = TextPrimary)
            }
        }

        item {
            GlassCard(padding = PaddingValues(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TextPrimary)
                        Spacer(Modifier.width(10.dp))
                        Text("Краткий ответ", color = TextPrimary)
                    }

                    Text(
                        "Команда разработки состоит из 4 кросс-функциональных команд, " +
                                "каждая включает frontend, backend, QA и дизайнера. " +
                                "Команды работают по Scrum методологии с двухнедельными спринтами.",
                        color = TextPrimary
                    )

                    Divider(color = TextSecondary.copy(alpha = 0.22f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Подробнее", color = TextSecondary)
                        Icon(Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondary)
                    }

                    AnimatedVisibility(visible = expanded) {
                        Text(
                            "Здесь можно показать расширенную версию ответа, примеры команд, " +
                                    "ссылки на регламенты и контактные точки.",
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        item { Text("Источники", color = TextSecondary, modifier = Modifier.padding(top = 4.dp)) }

        items(sources.size) { i ->
            val s = sources[i]
            GlassCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = TextSecondary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(s.title, color = TextPrimary)
                        Text(s.subtitle, color = TextSecondary)
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(10.dp))
            GradientPillButton(text = "Уточнить вопрос") { }
        }
    }
}
