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

/**
 * Utility class that uploads image bytes to Azure's Read/OCR API, polls the
 * operation URL until a result is ready, parses the JSON response into plain text,
 * and delivers the extracted text back to the caller on the main (UI) thread.
 */
class AzureOcrHelper(
    private val activity: Activity,
    private val subscriptionKey: String,
    private val endpoint: String,
    private val onResult: (String) -> Unit
) {

    // OkHttp client configured with reasonable timeouts for uploads and polling.
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // A single-threaded executor keeps OCR work serialized and off the UI thread.
    private val executor = Executors.newSingleThreadExecutor()

    /**
     * Convert raw bytes into an OkHttp RequestBody using octet-stream media type.
     * This is what the Azure Read API expects for image uploads.
     */
    private fun bytesToRequestBody(bytes: ByteArray): RequestBody {
        return RequestBody.create(
            "application/octet-stream".toMediaType(),
            bytes
        )
    }


     //caller passes image bytes and the helper runs OCR in background.

    fun run(bytes: ByteArray) {
        // Kick off background work on the internal executor so UI stays responsive.
        executor.execute { performOCR(bytes) }
    }

    //Upload image bytes to Azure Read API and start polling the returned operation URL.
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

            // Execute upload synchronously on the executor thread and check response
            client.newCall(postReq).execute().use { postResp ->
                if (!postResp.isSuccessful) {
                    // Surface a friendly message to the caller; caller can decide how to show it.
                    deliver("OCR failed at upload stage")
                    return
                }

                // Azure returns an Operation-Location header with the polling URL
                val opLocation = postResp.header("Operation-Location")
                    ?: run {
                        deliver("Missing operation location")
                        return
                    }

                // Start polling the operation URL until the service returns a final result
                pollResult(opLocation)
            }
        } catch (e: Exception) {
            // Catch everything and return a readable message; prevents crashes from network issues.
            deliver("OCR exception: ${e.message}")
        }
    }

    // Poll the given URL until the OCR operation completes or a timeout is reached.
    private fun pollResult(url: String) {
        // Try a limited number of times to avoid infinite polling.
        repeat(10) {
            // Small pause to allow the service to process the image
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

                // Status values are typically "running", "succeeded", or "failed".
                val status = root.optString("status").lowercase()

                when (status) {
                    "succeeded" -> {
                        // Parse text and return to caller
                        val text = extractText(root)
                        deliver(text)
                        return
                    }

                    "failed" -> {
                        // Let caller know the operation failed
                        deliver("OCR failed")
                        return
                    }

                    // If still running, the loop continues and we poll again.
                }
            }
        }

        // If we reach here, we timed out waiting for a final result
        deliver("Timed out waiting for OCR")
    }

    //Extract readable lines of text from the JSON response returned by Azure.
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

        // Trim trailing newline and return clean text
        return sb.toString().trim()
    }

   // Deliver the final text back to the caller on the UI thread.
    private fun deliver(text: String) {
        activity.runOnUiThread {
            onResult(text)
        }
    }
}
