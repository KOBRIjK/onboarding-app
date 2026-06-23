package com.example.darkonboarding.ui.screens.answer

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.darkonboarding.data.rag.RagApiClient
import com.example.darkonboarding.data.rag.RagDocument
import com.example.darkonboarding.ui.components.GlassCard
import com.example.darkonboarding.ui.components.GradientPillButton
import com.example.darkonboarding.ui.theme.TextPrimary
import com.example.darkonboarding.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerScreen(
    question: String
) {
    var query by remember(question) { mutableStateOf(question) }
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var documents by remember { mutableStateOf<List<RagDocument>>(emptyList()) }
    val scope = rememberCoroutineScope()

    suspend fun askRag(currentQuery: String) {
        val trimmedQuery = currentQuery.trim()
        if (trimmedQuery.isBlank() || isLoading) return

        isLoading = true
        error = null

        runCatching {
            RagApiClient.ask(trimmedQuery)
        }.onSuccess { result ->
            answer = result.answer.ifBlank { "Нет информации." }
            documents = result.documents
            expanded = false
        }.onFailure { throwable ->
            error = throwable.message ?: "Не удалось получить ответ от RAG."
        }

        isLoading = false
    }

    fun submitQuestion() {
        scope.launch {
            askRag(query)
        }
    }

    LaunchedEffect(question) {
        if (question.isNotBlank()) {
            askRag(question)
        }
    }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassCard(padding = PaddingValues(16.dp)) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(text = "Задайте вопрос...", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            submitQuestion()
                        }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White.copy(alpha = 0.7f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedLeadingIconColor = Color.White.copy(alpha = 0.6f),
                        unfocusedLeadingIconColor = Color.White.copy(alpha = 0.6f),
                        disabledLeadingIconColor = Color.White.copy(alpha = 0.4f),
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                        disabledPlaceholderColor = Color.White.copy(alpha = 0.4f),
                        cursorColor = Color.White.copy(alpha = 0.6f),
                    )
                )
            }
        }

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
                        Text("Ответ", color = TextPrimary)
                    }

                    when {
                        isLoading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.width(22.dp),
                                    color = TextPrimary,
                                    strokeWidth = 2.dp
                                )
                                Text("Ищу ответ в базе знаний...", color = TextSecondary)
                            }
                        }

                        error != null -> {
                            Text(
                                text = error.orEmpty(),
                                color = Color(0xFFFF8A8A)
                            )
                        }

                        answer.isNotBlank() -> {
                            Text(text = answer, color = TextPrimary)
                        }

                        else -> {
                            Text(
                                text = "Введите вопрос в поиске и нажмите кнопку ниже.",
                                color = TextSecondary
                            )
                        }
                    }

                    if (documents.isNotEmpty()) {
                        Divider(color = TextSecondary.copy(alpha = 0.25f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Найденные фрагменты", color = TextSecondary)
                            Icon(
                                Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.rotate(if (expanded) 180f else 0f)
                            )
                        }

                        if (expanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                documents.forEach { document ->
                                    Text(
                                        text = "Фрагмент ${document.index}: ${document.content}",
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Источники",
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        if (documents.isEmpty()) {
            item {
                SourceCard(
                    title = "Confluence",
                    subtitle = "База знаний Confluence"
                )
            }
        } else {
            items(documents) { document ->
                SourceCard(
                    title = "Confluence · фрагмент ${document.index}",
                    subtitle = document.content.take(120).replace("\n", " ")
                )
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            GradientPillButton(
                text = if (isLoading) "Ищу..." else "Задать вопрос",
                onClick = {
                    if (!isLoading) submitQuestion()
                }
            )
        }
    }
}

@Composable
private fun SourceCard(
    title: String,
    subtitle: String
) {
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
                Text(title, color = TextPrimary)
                Text(subtitle, color = TextSecondary)
            }
        }
    }
}
