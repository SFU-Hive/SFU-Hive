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

    // ✅ FILL THESE WITH YOUR REAL VALUES
    // Endpoint example: "https://sfu-hive.cognitiveservices.azure.com"
    private const val AZURE_ENDPOINT = ""
    private const val AZURE_KEY = ""

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /** Compress bitmap to JPEG bytes */
    private fun bitmapToJpeg(bitmap: Bitmap): ByteArray {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        return out.toByteArray()
    }

    /**
     * Blocking helper that calls Azure READ API:
     * 1. POST /vision/v3.2/read/analyze
     * 2. Poll GET analyzeResults until status == succeeded
     * Returns all text as a single String, or null on failure.
     */
    fun analyzeHandwriting(bitmap: Bitmap): String? {
        val bytes = bitmapToJpeg(bitmap)
        val mediaType = "application/octet-stream".toMediaType()
        val body = bytes.toRequestBody(mediaType)

        val postUrl = "$AZURE_ENDPOINT/vision/v3.2/read/analyze"

        val postReq = Request.Builder()
            .url(postUrl)
            .addHeader("Ocp-Apim-Subscription-Key", AZURE_KEY)
            .addHeader("Content-Type", "application/octet-stream")
            .post(body)
            .build()

        try {
            client.newCall(postReq).execute().use { postResp ->
                if (!postResp.isSuccessful) {
                    return null
                }

                // Azure returns operation URL in header
                val operationLocation = postResp.header("Operation-Location")
                    ?: return null

                // Poll the operation until it's done or timeout
                repeat(10) { _ ->
                    Thread.sleep(800L) // 0.8s between polls

                    val getReq = Request.Builder()
                        .url(operationLocation)
                        .addHeader("Ocp-Apim-Subscription-Key", AZURE_KEY)
                        .get()
                        .build()

                    client.newCall(getReq).execute().use { getResp ->
                        if (!getResp.isSuccessful) return@use

                        val bodyStr = getResp.body?.string() ?: return@use
                        val json = JSONObject(bodyStr)
                        val status = json.optString("status")

                        when (status.lowercase()) {
                            "succeeded" -> {
                                return extractReadText(json)
                            }
                            "failed" -> {
                                return null
                            }
                            // "running" or "notStarted" → continue polling
                        }
                    }
                }
            }
        } catch (_: Exception) {
            return null
        }

        return null
    }

    /** Extract text from READ API JSON */
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
                if (text.isNotBlank()) {
                    builder.append(text.trim()).append("\n")
                }
            }
        }

        return builder.toString().trim()
    }
}
