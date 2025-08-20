package com.example.project_application

import android.content.Context
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

suspend fun sendTelegram(context: Context, caption: String, photoUri: Uri? = null): Boolean = withContext(Dispatchers.IO) {
    val botToken = "YOUR_BOT_TOKEN" // Replace with actual token
    val prefs = context.getSharedPreferences("workflow_prefs", Context.MODE_PRIVATE)
    val chatId = prefs.getString("telegram_chat_id", "") ?: run {
        withContext(Dispatchers.Main) { Toast.makeText(context, "Telegram chat ID not set", Toast.LENGTH_SHORT).show() }
        return@withContext false
    }

    val client = OkHttpClient()
    val requestBody = if (photoUri != null) {
        val file = File(photoUri.path ?: return@withContext false)
        MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", chatId)
            .addFormDataPart("caption", caption)
            .addFormDataPart("photo", "image.jpg", file.asRequestBody("image/jpeg".toMediaType()))
            .build()
    } else {
        MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", chatId)
            .addFormDataPart("text", caption)
            .build()
    }

    val request = Request.Builder()
        .url(if (photoUri != null) "https://api.telegram.org/bot$botToken/sendPhoto" else "https://api.telegram.org/bot$botToken/sendMessage")
        .post(requestBody)
        .build()

    try {
        val response = client.newCall(request).execute()
        val success = response.isSuccessful
        withContext(Dispatchers.Main) {
            Toast.makeText(context, if (success) "Telegram sent to $chatId" else "Failed to send Telegram", Toast.LENGTH_SHORT).show()
        }
        response.close()
        success
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Telegram error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        false
    }
}