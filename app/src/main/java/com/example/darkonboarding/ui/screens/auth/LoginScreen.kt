package com.example.darkonboarding.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.darkonboarding.data.auth.AuthApiClient
import com.example.darkonboarding.data.auth.AuthSession
import com.example.darkonboarding.ui.components.GlassCard
import com.example.darkonboarding.ui.components.GradientPillButton
import com.example.darkonboarding.ui.theme.TextPrimary
import com.example.darkonboarding.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onAuthenticated: (AuthSession) -> Unit
) {
    var email by remember { mutableStateOf("backend.dev@example.com") }
    var password by remember { mutableStateOf("password123") }
    var name by remember { mutableStateOf("Backend Developer") }
    var createAccount by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submit() {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        val trimmedName = name.trim().ifBlank { "Backend Developer" }

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank() || isLoading) return

        scope.launch {
            isLoading = true
            error = null

            runCatching {
                if (createAccount) {
                    AuthApiClient.signup(trimmedEmail, trimmedPassword, trimmedName)
                } else {
                    AuthApiClient.login(trimmedEmail, trimmedPassword)
                }
            }.onSuccess { session ->
                onAuthenticated(session)
            }.onFailure { throwable ->
                error = throwable.message ?: "Не удалось войти."
            }

            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 34.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Вход в онбординг", color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            "Авторизуйтесь, чтобы получить личный маршрут адаптации.",
            color = TextSecondary
        )
        Spacer(Modifier.height(18.dp))

        GlassCard(padding = PaddingValues(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email"
                )
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Пароль",
                    isPassword = true
                )
                if (createAccount) {
                    AuthTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Имя"
                    )
                }

                if (error != null) {
                    Text(error.orEmpty(), color = Color(0xFFFF8A8A))
                }

                GradientPillButton(
                    text = when {
                        isLoading -> "Подключаюсь..."
                        createAccount -> "Создать аккаунт"
                        else -> "Войти"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { submit() }
                )

                TextButton(
                    onClick = {
                        error = null
                        createAccount = !createAccount
                    }
                ) {
                    Text(
                        text = if (createAccount) {
                            "У меня уже есть аккаунт"
                        } else {
                            "Создать тестовый аккаунт"
                        },
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextSecondary) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
            cursorColor = Color.White.copy(alpha = 0.6f),
        )
    )
}
