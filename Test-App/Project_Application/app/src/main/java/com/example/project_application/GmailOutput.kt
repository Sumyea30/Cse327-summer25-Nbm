package com.example.project_application

import android.util.Base64
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import java.io.ByteArrayOutputStream
import java.util.*
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import jakarta.mail.Message.RecipientType

class GmailOutput(
    credential: GoogleAccountCredential,
    private val user: String // usually "me"
) {

    private val gmailService: Gmail = Gmail.Builder(
        NetHttpTransport(), // Replaced deprecated AndroidHttp
        GsonFactory(),
        credential
    ).setApplicationName("ProjectApplication").build()

    fun sendMessages(subject: String, recipient: String, messages: List<String>) {
        messages.forEach { message ->
            sendEmail(subject, recipient, message)
        }
    }

    private fun sendEmail(subject: String, recipient: String, body: String) {
        val session = Session.getDefaultInstance(Properties(), null)
        val mimeMessage = MimeMessage(session)

        mimeMessage.setFrom(InternetAddress(user))
        mimeMessage.addRecipient(RecipientType.TO, InternetAddress(recipient))
        mimeMessage.subject = subject
        mimeMessage.setText(body)

        val buffer = ByteArrayOutputStream()
        mimeMessage.writeTo(buffer)
        val encodedEmail = Base64.encodeToString(buffer.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)

        val message = Message().apply { raw = encodedEmail }

        gmailService.users().messages().send(user, message).execute()
    }
}
