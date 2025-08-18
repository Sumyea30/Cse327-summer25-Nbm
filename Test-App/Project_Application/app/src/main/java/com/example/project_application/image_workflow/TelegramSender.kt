package com.example.project_application

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

suspend fun sendTelegram(context: Context, caption: String, photoUri: Uri? = null) = withContext(Dispatchers.IO) {
    val botToken = "" // Replace with your actual token
    val prefs = context.getSharedPreferences("workflow_prefs", Context.MODE_PRIVATE)
    val chatId = prefs.getString("telegram_chat_id", "") ?: return@withContext

    val client = OkHttpClient()
    val requestBody = if (photoUri != null) {
        val file = File(context.contentResolver.openFileDescriptor(photoUri, "r")?.fileDescriptor?.let {
            photoUri.path ?: return@withContext
        } ?: return@withContext)
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

    client.newCall(request).execute()
}