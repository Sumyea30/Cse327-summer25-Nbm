package com.example.project_application.image_workflow

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.core.net.toUri
import com.example.project_application.sendTelegram
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential

fun storeForLater(context: Context, type: String, uriString: String) {
    val prefs = context.getSharedPreferences("workflow_prefs", Context.MODE_PRIVATE)
    val existing = prefs.getString("pending_items", "") ?: ""
    prefs.edit { putString("pending_items", "$existing,$type:$uriString") }
}

fun storeExpense(context: Context, amount: Float) {
    val prefs = context.getSharedPreferences("workflow_prefs", Context.MODE_PRIVATE)
    val total = prefs.getFloat("total_expense", 0f) + amount
    prefs.edit { putFloat("total_expense", total) }
}

fun extractAmount(text: String): Float {
    val regex = Regex("\\d+\\.\\d{2}")
    return regex.findAll(text).map { it.value.toFloat() }.sum()
}

@RequiresApi(Build.VERSION_CODES.O)
fun processStoredData(context: Context, credential: GoogleAccountCredential) {
    val prefs = context.getSharedPreferences("workflow_prefs", Context.MODE_PRIVATE)
    val pending = prefs.getString("pending_items", "") ?: ""
    val items = pending.split(",").filter { it.isNotEmpty() }
    val totalExpense = prefs.getFloat("total_expense", 0f)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notification = Notification.Builder(context, "workflow_channel")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Smart Image Workflow Summary")
        .setContentText("Processed ${items.size} items. Total spent: $$totalExpense")
        .setAutoCancel(true)
        .build()
    notificationManager.notify(1, notification)


    Toast.makeText(context, "Summary: Spent $$totalExpense", Toast.LENGTH_LONG).show()

    items.forEach { item ->
        val (type, uriStr) = item.split(":")
        val uri = uriStr.toUri()
        when (type) {
            "son_pic" -> {
                val email = prefs.getString("son_email", "") ?: ""
                if (email.isNotEmpty()) {
                    kotlinx.coroutines.runBlocking {
                        sendEmail(context, credential, email, "Son's Picture", "Automated send", uri)
                    }
                }
            }
            "hospital_report" -> {
                val email = prefs.getString("doctor_email", "") ?: ""
                if (email.isNotEmpty()) {
                    kotlinx.coroutines.runBlocking {
                        sendEmail(context, credential, email, "Hospital Report", "Automated send", uri)
                    }
                }
            }
            "receipt" -> {
                kotlinx.coroutines.runBlocking {
                    sendTelegram(context, "Receipt Image", uri)
                }
            }
        }
    }

    prefs.edit {
        remove("pending_items")
        putFloat("total_expense", 0f)
    }
}