package com.example.darkonboarding.network

import com.example.darkonboarding.data.auth.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

class AccessTokenInterceptor(
    private val tokenStorage: TokenStorage,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val access = tokenStorage.getAccess()
        val authenticated = if (!access.isNullOrEmpty()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $access")
                .build()
        } else {
            original
        }
        return chain.proceed(authenticated)
    }
}
