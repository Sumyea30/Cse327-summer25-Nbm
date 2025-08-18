package com.example.project_application.io.input

import com.example.project_application.core.WorkflowInput
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class TelegramInput(
    private val botToken: String,
    private var lastUpdateId: Long = 0L
) : WorkflowInput<String> {

    private val client = OkHttpClient()

    override fun fetch(): List<String> {
        val url = "https://api.telegram.org/bot$botToken/getUpdates?offset=${lastUpdateId + 1}&limit=5"
        val request = Request.Builder().url(url).build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("TelegramInput HTTP error: ${response.code}")
                    return emptyList()
                }

                val body = response.body?.string() ?: return emptyList()
                val json = JSONObject(body)

                if (!json.getBoolean("ok")) return emptyList()

                val messages = mutableListOf<String>()
                val resultArray = json.getJSONArray("result")

                for (i in 0 until resultArray.length()) {
                    val update = resultArray.getJSONObject(i)
                    lastUpdateId = update.getLong("update_id")

                    val message = update.optJSONObject("message") ?: continue
                    val chat = message.optJSONObject("chat")
                    val chatId = chat?.optLong("id")
                    val text = message.optString("text", "")

                    if (text.isNotBlank()) {
                        println("📥 Telegram message from $chatId: $text")
                        messages.add(text)
                    }
                }

                messages
            }
        } catch (e: Exception) {
            println("TelegramInput Error: ${e.message}")
            emptyList()
        }
    }
}
