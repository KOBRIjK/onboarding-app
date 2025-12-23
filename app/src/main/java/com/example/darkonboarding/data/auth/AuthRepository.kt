package com.example.darkonboarding.data.auth

import retrofit2.HttpException

class AuthRepository(
    private val api: AuthApi,
    private val tokenStorage: TokenStorage,
) {
    suspend fun signup(email: String, password: String, name: String): AuthResult {
        return performAuthCall {
            api.signup(
                SignupRequest(
                    email = email,
                    password = password,
                    name = name,
                )
            )
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        return performAuthCall {
            api.login(
                LoginRequest(
                    email = email,
                    password = password,
                )
            )
        }
    }

    suspend fun refresh(): Boolean {
        val refresh = tokenStorage.getRefresh() ?: return false
        return try {
            val tokens = api.refresh(RefreshRequest(refreshToken = refresh))
            tokenStorage.save(tokens)
            true
        } catch (_: Exception) {
            tokenStorage.clear()
            false
        }
    }

    suspend fun hasValidSession(): Boolean {
        val access = tokenStorage.getAccess() ?: return false
        if (!tokenStorage.isAccessExpired()) return true
        return refresh().also { isValid ->
            if (!isValid && access.isNotEmpty()) {
                tokenStorage.clear()
            }
        }
    }

    fun logout() {
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
