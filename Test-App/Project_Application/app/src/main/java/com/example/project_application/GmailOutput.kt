package com.example.project_application

import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import android.util.Base64
import java.util.*
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class GmailOutput(
    private val service: Gmail,
    private val userEmail: String
) : WorkflowOutput<String> {

    override fun sendMessages(subject: String, recipient: String, messages: List<String>) {
        val session = Session.getDefaultInstance(Properties(), null)

        messages.forEach { msg ->
            val email = MimeMessage(session).apply {
                setFrom(InternetAddress(userEmail))
                addRecipient(javax.mail.Message.RecipientType.TO, InternetAddress(recipient))
                setSubject(subject)
                setText(msg)
            }

            val buffer = java.io.ByteArrayOutputStream().apply {
                email.writeTo(this)
            }

            val raw = Base64.encodeToString(buffer.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)

            val message = Message().apply {
                this.raw = raw
            }

            service.users().messages().send(userEmail, message).execute()
        }
    }
}
