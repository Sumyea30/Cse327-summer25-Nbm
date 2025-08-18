package com.example.project_application.io.output

import com.example.project_application.core.WorkflowOutput
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class TelegramOutput(
    private val botToken: String,
    private val chatId: String
) : WorkflowOutput<String> {

    private val client = OkHttpClient()

    override fun sendMessages(subject: String, recipient: String, messages: List<String>) {
        for (msg in messages) {
            try {
                val url = "https://api.telegram.org/bot$botToken/sendMessage"
                val json = JSONObject().apply {
                    put("chat_id", chatId)
                    put("text", "[$subject] $msg")
                }

                val request = Request.Builder()
                    .url(url)
                    .post(json.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        println("Telegram send error: ${response.code}")
                    } else {
                        println("✅ Sent Telegram message: $msg")
                    }
                }

            } catch (e: Exception) {
                println("TelegramOutput Error: ${e.message}")
            }
        }
    }
}
