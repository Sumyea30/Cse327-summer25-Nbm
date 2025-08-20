package com.example.project_application.image_workflow

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.example.project_application.sendTelegram
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.example.project_application.image_workflow.GmailSender.sendEmail

fun storeForLater(context: Context, type: String, uriString: String) {
    val prefs = context.getSharedPreferences("workflow_prefs", Context.MODE_PRIVATE)
    val existing = prefs.getString("pending_items", "") ?: ""
    prefs.edit { putString("pending_items", "$existing,$type:$uriString") }
}

@RequiresApi(Build.VERSION_CODES.O)
fun processStoredData(context: Context, credential: GoogleAccountCredential) {
    val prefs = context.getSharedPreferences("workflow_prefs", Context.MODE_PRIVATE)
    val pending = prefs.getString("pending_items", "") ?: ""
    val items = pending.split(",").filter { it.isNotEmpty() }

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notification = NotificationCompat.Builder(context, "workflow_channel")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Workflow Processed")
        .setContentText("Processed ${items.size} items.")
        .setAutoCancel(true)
        .build()
    notificationManager.notify(1, notification)

    Toast.makeText(context, "Processed ${items.size} items", Toast.LENGTH_LONG).show()

    items.forEach { item ->
        val (type, uriStr) = item.split(":")
        val uri = uriStr.toUri()
        when (type) {
            "image" -> {
                val prefs = context.getSharedPreferences("workflow_prefs", Context.MODE_PRIVATE)
                val delivery = prefs.getString("delivery", "Gmail") ?: "Gmail"
                val receiver = prefs.getString("receiver", "") ?: ""
                val message = prefs.getString("message", "") ?: ""
                kotlinx.coroutines.runBlocking {
                    when (delivery) {
                        "Gmail" -> sendEmail(context, credential, receiver, "Workflow Image", message, arrayOf(uriStr))
                        "Telegram" -> sendTelegram(context, message, uri)
                    }
                }
            }
        }
    }

    prefs.edit { remove("pending_items") }
}