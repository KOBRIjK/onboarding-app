package com.example.darkonboarding.ui.screens.answer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.darkonboarding.ui.components.GlassCard
import com.example.darkonboarding.ui.components.GradientPillButton
import com.example.darkonboarding.ui.theme.TextPrimary
import com.example.darkonboarding.ui.theme.TextSecondary

@Composable
fun AnswerScreen(
    question: String,
    autoFocus: Boolean = false,
    onBackToHome: () -> Unit = {}
) {
    var questionText by rememberSaveable { mutableStateOf(question) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (autoFocus) {
            focusRequester.requestFocus()
        }
    }

    val sources = listOf(
        SourceItem("Confluence", "confluence.company.com/wiki"),
        SourceItem("Git", "github.company.com/docs"),
        SourceItem("OpenMetadata", "metadata.company.com")
    )

    var expanded by remember { mutableStateOf(false) }

    BackHandler {
        onBackToHome()
    }


    LazyColumn(
        modifier = Modifier.padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ───────────── ВОПРОС ─────────────
        item {
            GlassCard(padding = PaddingValues(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Я", color = TextSecondary)
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        value = questionText,
                        onValueChange = { questionText = it },
                        placeholder = {
                            Text(
                                text = "Задай вопрос, чтобы начать...",
                                color = TextSecondary
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = TextSecondary,
                            unfocusedIndicatorColor = TextSecondary.copy(alpha = 0.6f),
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = TextPrimary
                        )
                    )
                }
            }
        }

        // ───────────── ОТВЕТ ─────────────
        item {
            GlassCard(padding = PaddingValues(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TextPrimary
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Краткий ответ", color = TextPrimary)
                    }

                    Text(
                        text = "Команда разработки состоит из 4 кросс-функциональных команд. " +
                                "Каждая включает frontend, backend, QA и дизайнера. " +
                                "Команды работают по Scrum с двухнедельными спринтами.",
                        color = TextPrimary
                    )

                    Divider(color = TextSecondary.copy(alpha = 0.25f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Подробнее", color = TextSecondary)

                        val rotation by animateFloatAsState(
                            targetValue = if (expanded) 180f else 0f,
                            label = "expand"
                        )

                        Icon(
                            Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.rotate(rotation),
                            tint = TextSecondary
                        )
                    }

                    AnimatedVisibility(visible = expanded) {
                        Text(
                            text = "Каждая команда автономна и отвечает за полный цикл разработки: " +
                                    "от планирования и реализации до поддержки и улучшений. " +
                                    "Коммуникация между командами осуществляется через синхронизации и общие гайды.",
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // ───────────── ИСТОЧНИКИ ─────────────
        item {
            Text(
                text = "Источники",
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        items(sources) { source ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                padding = PaddingValues(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(source.title, color = TextPrimary)
                        Text(source.subtitle, color = TextSecondary)
                    }
                }
            }
        }

        // ───────────── КНОПКА ─────────────
        item {
            Spacer(Modifier.height(8.dp))
            GradientPillButton(
                text = "Уточнить вопрос",
                onClick = { /* TODO */ }
            )
        }
    }
}

private data class SourceItem(
    val title: String,
    val subtitle: String
)
