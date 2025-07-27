package com.example.project_application.workflow.input

import android.content.Context
import android.util.Log
import com.example.project_application.workflow.WorkflowData
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.ListMessagesResponse
import com.google.api.services.gmail.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GmailInput(
    private val context: Context,
    private val credential: GoogleAccountCredential
) {
    private val gmailService: Gmail by lazy {
        Gmail.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Project Application").build()
    }
    suspend fun fetchRecentEmails(limit: Int = 5): List<WorkflowData<String, Unit>> = withContext(Dispatchers.IO) {
        try {
            val user = "me"
            val messagesResponse: ListMessagesResponse = gmailService.users().messages().list(user)
                .setMaxResults(limit.toLong())
                .setQ("is:unread")
                .execute()

            val messages: List<Message> = messagesResponse.messages ?: emptyList()

            messages.map { message ->
                val fullMessage = gmailService.users().messages().get(user, message.id).setFormat("FULL").execute()
                val body = fullMessage.snippet
                WorkflowData(input = body, output = Unit)
            }
        } catch (e: UserRecoverableAuthIOException) {
            Log.e("GmailInput", "Authorization required", e)
            // You should handle this by startingActivityForResult using e.intent
            emptyList()
        } catch (e: Exception) {
            Log.e("GmailInput", "Error fetching emails", e)
            emptyList()
        }
    }
}