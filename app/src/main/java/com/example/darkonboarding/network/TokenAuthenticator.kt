package com.example.darkonboarding.network

import com.example.darkonboarding.data.auth.AuthApi
import com.example.darkonboarding.data.auth.RefreshRequest
import com.example.darkonboarding.data.auth.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenStorage: TokenStorage,
    private val authApi: AuthApi,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null
        val refresh = tokenStorage.getRefresh() ?: return null
        val newTokens = runBlocking {
            try {
                authApi.refresh(RefreshRequest(refreshToken = refresh))
            } catch (_: Exception) {
                null
            }
        }

        return newTokens?.let { tokens ->
            tokenStorage.save(tokens)
            response.request.newBuilder()
                .header("Authorization", "Bearer ${tokens.accessToken}")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var current = response
        var count = 1
        while (current.priorResponse != null) {
            count++
            current = current.priorResponse!!
        }
        return count
    }
}
