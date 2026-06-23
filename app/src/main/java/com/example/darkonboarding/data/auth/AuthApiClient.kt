package com.example.darkonboarding.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val API_BASE_URL = "http://10.0.2.2:8000/api/v1"

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Int
)

object AuthApiClient {
    suspend fun login(email: String, password: String): AuthSession =
        postAuth("/auth/login", JSONObject().put("email", email).put("password", password))

    suspend fun signup(email: String, password: String, name: String): AuthSession =
        postAuth(
            "/auth/signup",
            JSONObject()
                .put("email", email)
                .put("password", password)
                .put("name", name)
        )

    private suspend fun postAuth(path: String, body: JSONObject): AuthSession =
        withContext(Dispatchers.IO) {
            val connection = (URL("$API_BASE_URL$path").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 30_000
                doOutput = true
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }

            try {
                connection.outputStream.use { output ->
                    output.write(body.toString().toByteArray(Charsets.UTF_8))
                }

                val statusCode = connection.responseCode
                val responseText = if (statusCode in 200..299) {
                    connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                        .orEmpty()
                }

                if (statusCode !in 200..299) {
                    throw IOException(parseErrorMessage(statusCode, responseText))
                }

                parseSession(responseText)
            } finally {
                connection.disconnect()
            }
        }

    private fun parseSession(responseText: String): AuthSession {
        val json = JSONObject(responseText)
        return AuthSession(
            accessToken = json.getString("accessToken"),
            refreshToken = json.getString("refreshToken"),
            expiresInSeconds = json.getInt("expiresInSeconds")
        )
    }

    private fun parseErrorMessage(statusCode: Int, responseText: String): String {
        val detail = runCatching {
            JSONObject(responseText).optString("detail")
        }.getOrNull()

        return if (detail.isNullOrBlank()) {
            "Auth API вернул ошибку $statusCode"
        } else {
            detail
        }
    }
}
