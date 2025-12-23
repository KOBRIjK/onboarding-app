package com.example.darkonboarding.data.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "auth_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(tokens: AuthTokens) {
        prefs.edit().apply {
            putString(KEY_ACCESS, tokens.accessToken)
            putString(KEY_REFRESH, tokens.refreshToken)
            putLong(
                KEY_EXPIRES_AT,
                System.currentTimeMillis() + tokens.expiresInSeconds * 1000
            )
        }.apply()
    }

    fun getAccess(): String? = prefs.getString(KEY_ACCESS, null)

    fun getRefresh(): String? = prefs.getString(KEY_REFRESH, null)

    fun isAccessExpired(): Boolean =
        System.currentTimeMillis() >= prefs.getLong(KEY_EXPIRES_AT, 0)

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_ACCESS = "access"
        const val KEY_REFRESH = "refresh"
        const val KEY_EXPIRES_AT = "expires_at"
    }
}
