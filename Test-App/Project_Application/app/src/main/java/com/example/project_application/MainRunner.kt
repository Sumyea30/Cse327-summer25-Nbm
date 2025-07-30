package com.example.project_application

import com.example.project_application.Config.TELEGRAM_BOT_TOKEN
import com.example.project_application.Config.TELEGRAM_CHAT_ID
import com.example.project_application.Config.GMAIL_USER_EMAIL
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.gmail.Gmail

class MainRunner(
    private val credential: GoogleAccountCredential,
    private val gmailService: Gmail
) {

    fun run() {
        val telegramInput = TelegramInput(TELEGRAM_BOT_TOKEN)
        val processor = SimpleForwardProcessor()
        val gmailOutput = GmailOutput(gmailService, GMAIL_USER_EMAIL)

        val telegramToGmail = TelegramToGmailWorkflow(
            telegramInput,
            processor,
            gmailOutput
        )

        val manager = WorkflowManager(listOf(telegramToGmail))
        manager.runAll("Freely","ssadman552@gmail.com")
    }
}
