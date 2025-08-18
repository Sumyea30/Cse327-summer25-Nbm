package com.example.project_application.core

import android.content.Context
import com.example.project_application.services.GmailService
import com.example.project_application.services.TelegramService
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential

data class WorkflowConfig(
    val source: String,
    val destination: String,
    val receiver: String,
    val workflowType: String,
    val filterValue: String?,
    val schedule: String
)

class WorkflowExecutor(
    private val context: Context,
    private val credential: GoogleAccountCredential,
    private val telegramToken: String
) {
    fun run(config: WorkflowConfig) {
        val gmail = GmailService(context, credential)
        val telegram = TelegramService(telegramToken)

        when {
            config.source == "Gmail" && config.destination == "Telegram" -> {
                val messages = gmail.readEmails("from:${config.filterValue ?: ""}")
                messages.forEach { msg ->
                    telegram.sendMessage(config.receiver, "New Gmail message ID: ${msg.id}")
                }
            }

            config.source == "Telegram" && config.destination == "Gmail" -> {
                // Telegram inbound logic would go here (polling/webhook)
                gmail.sendEmail(config.receiver, "me", "From Telegram", "This is a forwarded Telegram message.")
            }
        }
    }
}
