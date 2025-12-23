package com.example.darkonboarding.data.auth

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/signup")
    suspend fun signup(@Body payload: SignupRequest): AuthTokens

    @POST("/auth/login")
    suspend fun login(@Body payload: LoginRequest): AuthTokens

    @POST("/auth/refresh")
    suspend fun refresh(@Body payload: RefreshRequest): AuthTokens
}
