package com.example.project_application.services

import android.content.Context
import android.util.Base64
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import java.io.ByteArrayOutputStream
import java.util.Properties
import javax.mail.Message.RecipientType
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class GmailService(private val context: Context, private val credential: GoogleAccountCredential) {

    val service: Gmail = Gmail.Builder(
        NetHttpTransport(),
        GsonFactory.getDefaultInstance(),
        credential
    ).setApplicationName("ProjectApplication").build()

    fun readEmails(query: String): List<Message> {
        val list = service.users().messages().list("me").setQ(query).execute()
        return list.messages ?: emptyList()
    }

    fun sendEmail(to: String, from: String, subject: String, bodyText: String) {
        val session = Session.getDefaultInstance(Properties(), null)
        val email = MimeMessage(session).apply {
            setFrom(InternetAddress(from))
            addRecipient(RecipientType.TO, InternetAddress(to))
            this.subject = subject
            setText(bodyText)
        }
        val buffer = ByteArrayOutputStream()
        email.writeTo(buffer)
        val raw = Base64.encodeToString(buffer.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
        val message = Message().setRaw(raw)
        service.users().messages().send("me", message).execute()
    }
}
