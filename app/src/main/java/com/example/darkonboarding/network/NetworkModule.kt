package com.example.darkonboarding.network

import com.example.darkonboarding.data.auth.AuthApi
import com.example.darkonboarding.data.auth.TokenStorage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {
    fun loggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    fun unauthenticatedClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .build()
    }

    fun authenticatedClient(tokenStorage: TokenStorage, authApi: AuthApi): OkHttpClient {
        return unauthenticatedClient().newBuilder()
            .addInterceptor(AccessTokenInterceptor(tokenStorage))
            .authenticator(TokenAuthenticator(tokenStorage, authApi))
            .build()
    }

    fun retrofit(client: OkHttpClient): Retrofit {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    fun authApi(client: OkHttpClient): AuthApi = retrofit(client).create(AuthApi::class.java)
}
