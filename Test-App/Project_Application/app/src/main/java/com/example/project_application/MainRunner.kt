package com.example.project_application

import com.example.project_application.Config.TELEGRAM_BOT_TOKEN
import com.example.project_application.Config.TELEGRAM_CHAT_ID
import com.example.project_application.Config.GMAIL_USER_EMAIL
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential

class MainRunner(private val credential: GoogleAccountCredential) {

    fun run() {
        val gmailInput = object : WorkflowInput<String> {
            override fun fetchLatestMessages(): List<String> {
                return GmailInput(credential).fetchLatestMessagesBlocking()
            }
        }

        val telegramInput = object : WorkflowInput<String> {
            override fun fetchLatestMessages(): List<String> {
                return TelegramInput(TELEGRAM_BOT_TOKEN).fetchLatestMessages()
            }
        }

        val telegramOutput = TelegramOutput(TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID)
        val gmailOutput = GmailOutput(credential, GMAIL_USER_EMAIL)

        val processor = SimpleForwardProcessor()

        val gmailToTelegram = GmailToTelegramWorkflow(gmailInput, processor, telegramOutput)
        val telegramToGmail = TelegramToGmailWorkflow(telegramInput, processor, gmailOutput)

        val manager = WorkflowManager(listOf(gmailToTelegram, telegramToGmail))
        manager.runAll()
    }
}
