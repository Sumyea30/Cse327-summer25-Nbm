package com.example.project_application

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.ListMessagesResponse
import com.google.api.services.gmail.model.Message

class GmailInput(
    private val credential: GoogleAccountCredential
) {

    suspend fun fetchLatestMessages(
        userId: String = "me",
        maxResults: Long = 5
    ): List<String> {
        val gmailService = Gmail.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("ProjectApplication")
            .build()

        val listResponse: ListMessagesResponse = gmailService.users().messages()
            .list(userId)
            .setQ("is:unread")
            .setMaxResults(maxResults)
            .execute()

        val messages = listResponse.messages ?: return emptyList()

        return messages.mapNotNull { msg ->
            try {
                val fullMessage: Message = gmailService.users().messages()
                    .get(userId, msg.id)
                    .setFormat("full")
                    .execute()
                fullMessage.snippet
            } catch (e: Exception) {
                null
            }
        }
    }
    fun fetchLatestMessagesBlocking(): List<String> {
        return kotlinx.coroutines.runBlocking {
            fetchLatestMessages()
        }
    }

}
