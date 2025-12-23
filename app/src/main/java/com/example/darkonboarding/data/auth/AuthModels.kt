package com.example.darkonboarding.data.auth

import com.google.gson.annotations.SerializedName

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
    @SerializedName("refresh_token") val refreshToken: String,
)

data class LogoutRequest(
    @SerializedName("refresh_token") val refreshToken: String,
)

data class AuthTokens(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresInSeconds: Long,
)

data class MeProfile(
    val id: String,
    val email: String,
    val name: String,
)
