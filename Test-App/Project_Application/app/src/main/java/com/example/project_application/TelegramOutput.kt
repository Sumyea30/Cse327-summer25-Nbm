package com.example.project_application

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class TelegramOutput(private val botToken: String, private val chatId: String) {

    private val client = OkHttpClient()

    fun sendMessages(messages: List<String>) {
        messages.forEach { message ->
            sendMessage(message)
        }
    }

    private fun sendMessage(text: String) {
        val url = "https://api.telegram.org/bot$botToken/sendMessage"

        val body = FormBody.Builder()
            .add("chat_id", chatId)
            .add("text", text)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use {
            if (!it.isSuccessful) {
                println("Telegram send failed: ${it.code}")
            }
        }
    }
}
