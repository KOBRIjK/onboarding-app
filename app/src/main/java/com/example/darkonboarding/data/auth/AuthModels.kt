package com.example.darkonboarding.data.auth

import com.squareup.moshi.Json

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
    @Json(name = "refresh_token") val refreshToken: String,
)

data class LogoutRequest(
    @Json(name = "refresh_token") val refreshToken: String,
)

data class AuthTokens(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String,
    @Json(name = "expires_in") val expiresInSeconds: Long,
)

data class MeProfile(
    val id: String,
    val email: String,
    val name: String,
)
