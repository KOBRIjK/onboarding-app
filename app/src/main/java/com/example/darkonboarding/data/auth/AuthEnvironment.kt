package com.example.darkonboarding.data.auth

import android.content.Context
import com.example.darkonboarding.network.NetworkModule
import okhttp3.OkHttpClient

class AuthEnvironment(context: Context) {
    val tokenStorage = TokenStorage(context)
    private val publicClient: OkHttpClient = NetworkModule.unauthenticatedClient()
    val authApi: AuthApi = NetworkModule.authApi(publicClient)
    val authRepository = AuthRepository(authApi, tokenStorage)
    val authenticatedClient: OkHttpClient =
        NetworkModule.authenticatedClient(tokenStorage = tokenStorage, authApi = authApi)
}
