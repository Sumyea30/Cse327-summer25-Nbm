package com.example.project_application.services

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class TelegramService(private val botToken: String) {

    private val client = OkHttpClient()

    fun sendMessage(chatId: String, text: String) {
        val url = "https://api.telegram.org/bot$botToken/sendMessage"
        val json = JSONObject().apply {
            put("chat_id", chatId)
            put("text", text)
        }
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder().url(url).post(body).build()
        client.newCall(request).execute().close()
    }
}
