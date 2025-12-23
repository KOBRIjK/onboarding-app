package com.example.darkonboarding.data.auth

data class SignupRequest(
    val email: String,
    val password: String,
    val name: String,
)

data class LoginRequest(
    val email: String,
    val password: String,
)

data class RefreshRequest(
    val refreshToken: String,
)

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long,
)
