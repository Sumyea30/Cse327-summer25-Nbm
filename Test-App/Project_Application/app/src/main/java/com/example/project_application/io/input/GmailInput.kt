package com.example.project_application.io.input

import android.util.Base64
import com.example.project_application.core.WorkflowInput
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.ListMessagesResponse
import com.google.api.services.gmail.model.Message
import java.util.*


class GmailInput(
    private val gmailService: Gmail,
    private val userId: String = "me"
) : WorkflowInput<String> {

    override fun fetch(): List<String> {
        return try {
            val result: ListMessagesResponse = gmailService.users().messages().list(userId)
                .setMaxResults(5)
                .execute()

            val messages = mutableListOf<String>()

            for (msgRef in result.messages ?: emptyList()) {
                val fullMessage: Message = gmailService.users().messages().get(userId, msgRef.id).setFormat("full").execute()
                val body = extractPlainTextBody(fullMessage)
                messages.add(body)
            }

            messages
        } catch (e: Exception) {
            println("GmailInput Error: ${e.message}")
            emptyList()
        }
    }

    private fun extractPlainTextBody(message: Message): String {
        val parts = message.payload?.parts ?: return message.snippet ?: ""

        for (part in parts) {
            if (part.mimeType == "text/plain") {
                val data = part.body?.data ?: continue
                val decodedData = Base64.decode(data, Base64.URL_SAFE or Base64.NO_WRAP)
                return String(decodedData, charset("UTF-8"))
            }
        }

        return message.snippet ?: ""
    }

}
