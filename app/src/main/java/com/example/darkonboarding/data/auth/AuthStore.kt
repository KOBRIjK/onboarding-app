package com.example.darkonboarding.data.auth

import android.content.Context

private const val PREFS_NAME = "auth_session"
private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
private const val KEY_EXPIRES_IN = "expires_in"

object AuthStore {
    fun load(context: Context): AuthSession? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) return null

        return AuthSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresInSeconds = prefs.getInt(KEY_EXPIRES_IN, 0)
        )
    }

    fun save(context: Context, session: AuthSession) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ACCESS_TOKEN, session.accessToken)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .putInt(KEY_EXPIRES_IN, session.expiresInSeconds)
            .apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
