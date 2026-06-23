package com.example.darkonboarding.data.rag

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val RAG_ASK_URL = "http://10.0.2.2:8000/api/v1/rag/ask"

data class RagDocument(
    val index: Int,
    val content: String
)

data class RagAnswer(
    val answer: String,
    val documents: List<RagDocument>
)

object RagApiClient {
    suspend fun ask(query: String): RagAnswer = withContext(Dispatchers.IO) {
        val connection = (URL(RAG_ASK_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 180_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }

        try {
            val requestBody = JSONObject()
                .put("query", query)
                .toString()

            connection.outputStream.use { output ->
                output.write(requestBody.toByteArray(Charsets.UTF_8))
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

            parseAnswer(responseText)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseAnswer(responseText: String): RagAnswer {
        val json = JSONObject(responseText)
        val documentsJson = json.optJSONArray("documents")
        val documents = buildList {
            if (documentsJson == null) return@buildList
            for (i in 0 until documentsJson.length()) {
                val item = documentsJson.getJSONObject(i)
                add(
                    RagDocument(
                        index = item.optInt("index", i + 1),
                        content = item.optString("content")
                    )
                )
            }
        }

        return RagAnswer(
            answer = json.optString("answer"),
            documents = documents
        )
    }

    private fun parseErrorMessage(statusCode: Int, responseText: String): String {
        val detail = runCatching {
            JSONObject(responseText).optString("detail")
        }.getOrNull()

        return if (detail.isNullOrBlank()) {
            "RAG API вернул ошибку $statusCode"
        } else {
            detail
        }
    }
}
