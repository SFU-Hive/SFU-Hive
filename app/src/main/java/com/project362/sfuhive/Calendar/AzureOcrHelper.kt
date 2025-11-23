package com.project362.sfuhive.Calendar

import android.graphics.Bitmap
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object AzureOcrHelper {

    private var endpoint: String = ""
    private var key: String = ""

    fun initialize(endpoint: String, key: String) {
        this.endpoint = endpoint
        this.key = key
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun bitmapToJpeg(bitmap: Bitmap): ByteArray {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        return out.toByteArray()
    }

    fun analyzeHandwriting(bitmap: Bitmap): String? {
        if (endpoint.isBlank() || key.isBlank()) return null

        val bytes = bitmapToJpeg(bitmap)
        val mediaType = "application/octet-stream".toMediaType()
        val body = bytes.toRequestBody(mediaType)

        val postUrl = "$endpoint/vision/v3.2/read/analyze"

        val postReq = Request.Builder()
            .url(postUrl)
            .addHeader("Ocp-Apim-Subscription-Key", key)
            .addHeader("Content-Type", "application/octet-stream")
            .post(body)
            .build()

        try {
            client.newCall(postReq).execute().use { postResp ->
                if (!postResp.isSuccessful) return null

                val operationLocation = postResp.header("Operation-Location")
                    ?: return null

                repeat(10) {
                    Thread.sleep(800L)

                    val getReq = Request.Builder()
                        .url(operationLocation)
                        .addHeader("Ocp-Apim-Subscription-Key", key)
                        .get()
                        .build()

                    client.newCall(getReq).execute().use { getResp ->
                        if (!getResp.isSuccessful) return@use

                        val bodyStr = getResp.body?.string() ?: return@use
                        val json = JSONObject(bodyStr)
                        val status = json.optString("status")

                        when (status.lowercase()) {
                            "succeeded" -> return extractReadText(json)
                            "failed" -> return null
                        }
                    }
                }
            }
        } catch (_: Exception) {
            return null
        }

        return null
    }

    private fun extractReadText(root: JSONObject): String {
        val builder = StringBuilder()
        val analyzeResult = root.optJSONObject("analyzeResult") ?: return ""
        val readResults = analyzeResult.optJSONArray("readResults") ?: return ""

        for (i in 0 until readResults.length()) {
            val page = readResults.getJSONObject(i)
            val lines = page.optJSONArray("lines") ?: continue

            for (j in 0 until lines.length()) {
                val line = lines.getJSONObject(j)
                val text = line.optString("text")
                if (text.isNotBlank()) builder.append(text.trim()).append("\n")
            }
        }
        return builder.toString().trim()
    }
}
