package com.example.darkonboarding.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class AuthRepository(
    private val api: AuthApi,
    private val tokenStorage: TokenStorage,
) {
    suspend fun signup(email: String, password: String, name: String): AuthResult =
        withContext(Dispatchers.IO) {
            performAuthCall {
                api.signup(
                    SignupRequest(
                        email = email,
                        password = password,
                        name = name,
                    )
                )
            }
        }

    suspend fun login(email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            performAuthCall {
                api.login(
                    LoginRequest(
                        email = email,
                        password = password,
                    )
                )
            }
        }

    suspend fun refresh(): Boolean = withContext(Dispatchers.IO) {
        val refresh = tokenStorage.getRefresh() ?: return@withContext false
        try {
            val tokens = api.refresh(RefreshRequest(refreshToken = refresh))
            tokenStorage.save(tokens)
            true
        } catch (_: Exception) {
            tokenStorage.clear()
            false
        }
    }

    suspend fun logoutRemote() = withContext(Dispatchers.IO) {
        val refresh = tokenStorage.getRefresh() ?: return@withContext
        runCatching {
            api.logout(LogoutRequest(refreshToken = refresh))
        }
    }

    suspend fun hasValidSession(): Boolean = withContext(Dispatchers.IO) {
        val access = tokenStorage.getAccess() ?: return@withContext false
        if (!tokenStorage.isAccessExpired()) return@withContext true
        refresh().also { isValid ->
            if (!isValid && access.isNotEmpty()) {
                tokenStorage.clear()
            }
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        logoutRemote()
        tokenStorage.clear()
    }

    private suspend fun performAuthCall(block: suspend () -> AuthTokens): AuthResult {
        return try {
            val tokens = block()
            tokenStorage.save(tokens)
            AuthResult.Success
        } catch (error: Exception) {
            when (error) {
                is HttpException -> {
                    when (error.code()) {
                        400, 401 -> AuthResult.InvalidCredentials
                        409 -> AuthResult.Conflict
                        else -> AuthResult.Unknown(error.message())
                    }
                }

                else -> AuthResult.Unknown(error.message.orEmpty())
            }
        }
    }
}

sealed interface AuthResult {
    data object Success : AuthResult
    data object InvalidCredentials : AuthResult
    data object Conflict : AuthResult
    data class Unknown(val message: String) : AuthResult
}
