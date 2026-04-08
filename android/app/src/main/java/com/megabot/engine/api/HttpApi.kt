package com.megabot.engine.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * HTTP API exposed to JavaScript scripts.
 * Provides synchronous HTTP requests.
 */
class HttpApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun request(url: String): HttpResponse {
        return requestSync(url, "GET", null, null)
    }

    fun request(url: String, method: String): HttpResponse {
        return requestSync(url, method, null, null)
    }

    fun requestSync(url: String): HttpResponse {
        return requestSync(url, "GET", null, null)
    }

    fun requestSync(url: String, method: String, body: String?, contentType: String?): HttpResponse {
        val reqBuilder = Request.Builder().url(url)

        val requestBody = if (body != null) {
            val mediaType = (contentType ?: "application/json").toMediaType()
            body.toRequestBody(mediaType)
        } else null

        when (method.uppercase()) {
            "GET" -> reqBuilder.get()
            "POST" -> reqBuilder.post(requestBody ?: "".toRequestBody(null))
            "PUT" -> reqBuilder.put(requestBody ?: "".toRequestBody(null))
            "DELETE" -> if (requestBody != null) reqBuilder.delete(requestBody) else reqBuilder.delete()
            "PATCH" -> reqBuilder.patch(requestBody ?: "".toRequestBody(null))
        }

        val response = client.newCall(reqBuilder.build()).execute()
        return HttpResponse(
            statusCode = response.code,
            body = response.body?.string() ?: "",
            headers = response.headers.toMultimap().mapValues { it.value.joinToString(", ") }
        )
    }

    data class HttpResponse(
        val statusCode: Int,
        val body: String,
        val headers: Map<String, String>
    )
}
