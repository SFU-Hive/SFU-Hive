package com.project362.sfuhive.Calendar

import android.app.Activity
import android.graphics.BitmapFactory
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AzureOcrHelper(
    private val activity: Activity,
    private val subscriptionKey: String,
    private val endpoint: String,
    private val onResult: (String) -> Unit
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val executor = Executors.newSingleThreadExecutor()

    /** Convert ByteArray → JPEG RequestBody */
    private fun bytesToRequestBody(bytes: ByteArray): RequestBody {
        return RequestBody.create(
            "application/octet-stream".toMediaType(),
            bytes
        )
    }

    /** Public entry point */
    fun run(bytes: ByteArray) {
        executor.execute { performOCR(bytes) }
    }

    /** Main OCR pipeline */
    private fun performOCR(bytes: ByteArray) {
        try {
            val body = bytesToRequestBody(bytes)

            val postUrl = "$endpoint/vision/v3.2/read/analyze"

            val postReq = Request.Builder()
                .url(postUrl)
                .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .addHeader("Content-Type", "application/octet-stream")
                .post(body)
                .build()

            client.newCall(postReq).execute().use { postResp ->
                if (!postResp.isSuccessful) {
                    deliver("OCR failed at upload stage")
                    return
                }

                val opLocation = postResp.header("Operation-Location")
                    ?: run {
                        deliver("Missing operation location")
                        return
                    }

                pollResult(opLocation)
            }
        } catch (e: Exception) {
            deliver("OCR exception: ${e.message}")
        }
    }

    /** Poll Azure until OCR completed */
    private fun pollResult(url: String) {
        repeat(10) {
            Thread.sleep(800L)

            val getReq = Request.Builder()
                .url(url)
                .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .get()
                .build()

            client.newCall(getReq).execute().use { getResp ->
                if (!getResp.isSuccessful) return@use

                val jsonStr = getResp.body?.string() ?: return@use
                val root = JSONObject(jsonStr)

                val status = root.optString("status").lowercase()

                when (status) {
                    "succeeded" -> {
                        val text = extractText(root)
                        deliver(text)
                        return
                    }

                    "failed" -> {
                        deliver("OCR failed")
                        return
                    }
                }
            }
        }

        deliver("Timed out waiting for OCR")
    }

    /** Extract lines */
    private fun extractText(root: JSONObject): String {
        val sb = StringBuilder()
        val analyze = root.optJSONObject("analyzeResult") ?: return ""
        val pages = analyze.optJSONArray("readResults") ?: return ""

        for (i in 0 until pages.length()) {
            val page = pages.getJSONObject(i)
            val lines = page.optJSONArray("lines") ?: continue

            for (j in 0 until lines.length()) {
                val line = lines.getJSONObject(j).optString("text")
                if (line.isNotBlank()) sb.append(line.trim()).append("\n")
            }
        }

        return sb.toString().trim()
    }

    /** Safely return result on UI thread */
    private fun deliver(text: String) {
        activity.runOnUiThread {
            onResult(text)
        }
    }
}
