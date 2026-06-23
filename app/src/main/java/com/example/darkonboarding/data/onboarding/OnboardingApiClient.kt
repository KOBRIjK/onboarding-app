package com.example.darkonboarding.data.onboarding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val API_BASE_URL = "http://10.0.2.2:8000/api/v1"

data class OnboardingTask(
    val id: String,
    val order: Int,
    val title: String,
    val body: String,
    val ctaLabel: String?,
    val ctaAction: String?,
    val dueDays: Int?,
    val dueAt: String?,
    val isOverdue: Boolean,
    val status: String,
    val notes: String?
)

data class OnboardingProgress(
    val total: Int,
    val done: Int,
    val inProgress: Int,
    val notStarted: Int,
    val overdue: Int,
    val percent: Int,
    val assignmentStatus: String
)

object OnboardingApiClient {
    suspend fun getTasks(accessToken: String): List<OnboardingTask> =
        withContext(Dispatchers.IO) {
            val responseText = request(
                path = "/onboarding/tasks",
                accessToken = accessToken,
                method = "GET",
                body = null
            )
            val json = org.json.JSONArray(responseText)
            buildList {
                for (i in 0 until json.length()) {
                    add(parseTask(json.getJSONObject(i)))
                }
            }
        }

    suspend fun getProgress(accessToken: String): OnboardingProgress =
        withContext(Dispatchers.IO) {
            parseProgress(
                JSONObject(
                    request(
                        path = "/onboarding/progress",
                        accessToken = accessToken,
                        method = "GET",
                        body = null
                    )
                )
            )
        }

    suspend fun updateTaskStatus(
        accessToken: String,
        stepId: String,
        status: String
    ): OnboardingTask = withContext(Dispatchers.IO) {
        val body = JSONObject().put("status", status)
        parseTask(
            JSONObject(
                request(
                    path = "/onboarding/tasks/$stepId/status",
                    accessToken = accessToken,
                    method = "POST",
                    body = body
                )
            )
        )
    }

    private fun request(
        path: String,
        accessToken: String,
        method: String,
        body: JSONObject?
    ): String {
        val connection = (URL("$API_BASE_URL$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $accessToken")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }

        try {
            if (body != null) {
                connection.outputStream.use { output ->
                    output.write(body.toString().toByteArray(Charsets.UTF_8))
                }
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

            return responseText
        } finally {
            connection.disconnect()
        }
    }

    private fun parseTask(json: JSONObject): OnboardingTask =
        OnboardingTask(
            id = json.getString("id"),
            order = json.getInt("order"),
            title = json.getString("title"),
            body = json.getString("body"),
            ctaLabel = json.optString("ctaLabel").takeIf { it.isNotBlank() },
            ctaAction = json.optString("ctaAction").takeIf { it.isNotBlank() },
            dueDays = json.optNullableInt("dueDays"),
            dueAt = json.optString("dueAt").takeIf { it.isNotBlank() },
            isOverdue = json.optBoolean("isOverdue", false),
            status = json.getString("status"),
            notes = json.optString("notes").takeIf { it.isNotBlank() }
        )

    private fun parseProgress(json: JSONObject): OnboardingProgress =
        OnboardingProgress(
            total = json.getInt("total"),
            done = json.getInt("done"),
            inProgress = json.getInt("inProgress"),
            notStarted = json.getInt("notStarted"),
            overdue = json.optInt("overdue", 0),
            percent = json.getInt("percent"),
            assignmentStatus = json.getString("assignmentStatus")
        )

    private fun parseErrorMessage(statusCode: Int, responseText: String): String {
        val detail = runCatching {
            JSONObject(responseText).optString("detail")
        }.getOrNull()

        return if (detail.isNullOrBlank()) {
            "Onboarding API вернул ошибку $statusCode"
        } else {
            detail
        }
    }
}

private fun JSONObject.optNullableInt(name: String): Int? =
    if (has(name) && !isNull(name)) getInt(name) else null
