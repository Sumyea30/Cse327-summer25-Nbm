package com.example.project_application

import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.ListMessagesResponse
import com.google.api.services.gmail.model.Message

class GmailInput(
    private val gmailService: Gmail,
    private val userId: String = "me"
) : WorkflowInput<String> {

    override fun fetch(): List<String> {
        val result: ListMessagesResponse = gmailService.users().messages().list(userId)
            .setMaxResults(5)
            .execute()

        val messages = mutableListOf<String>()

        for (msgRef in result.messages ?: emptyList()) {
            val fullMessage: Message = gmailService.users().messages().get(userId, msgRef.id).execute()
            val snippet = fullMessage.snippet ?: continue
            messages.add(snippet)
        }

        return messages
    }

}
