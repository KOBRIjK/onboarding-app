package com.example.darkonboarding.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.darkonboarding.data.auth.AuthRepository
import com.example.darkonboarding.data.auth.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val state: StateFlow<AuthUiState> = _state

    init {
        viewModelScope.launch {
            val hasSession = repository.hasValidSession()
            _state.value = if (hasSession) AuthUiState.Authenticated else AuthUiState.Unauthenticated
        }
    }

    fun signup(email: String, password: String, name: String) {
        _state.value = AuthUiState.Submitting
        viewModelScope.launch {
            when (repository.signup(email = email, password = password, name = name)) {
                AuthResult.Success -> _state.value = AuthUiState.Authenticated
                AuthResult.InvalidCredentials -> _state.value =
                    AuthUiState.Error("Неверные данные. Проверьте почту и пароль")

                AuthResult.Conflict -> _state.value = AuthUiState.Error("Пользователь уже существует")
                is AuthResult.Unknown -> _state.value = AuthUiState.Error("Ошибка сети или сервера")
            }
        }
    }

    fun login(email: String, password: String) {
        _state.value = AuthUiState.Submitting
        viewModelScope.launch {
            when (repository.login(email = email, password = password)) {
                AuthResult.Success -> _state.value = AuthUiState.Authenticated
                AuthResult.InvalidCredentials -> _state.value =
                    AuthUiState.Error("Неверный логин или пароль")

                AuthResult.Conflict -> _state.value = AuthUiState.Error("Пользователь уже существует")
                is AuthResult.Unknown -> _state.value = AuthUiState.Error("Ошибка сети или сервера")
            }
        }
    }

    fun logout() {
        repository.logout()
        _state.value = AuthUiState.Unauthenticated
    }

    fun clearError() {
        if (_state.value is AuthUiState.Error) {
            _state.update { AuthUiState.Unauthenticated }
        }
    }

    class Factory(
        private val repository: AuthRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repository) as T
        }
    }
}

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object Unauthenticated : AuthUiState
    data object Submitting : AuthUiState
    data object Authenticated : AuthUiState
    data class Error(val message: String) : AuthUiState
}
