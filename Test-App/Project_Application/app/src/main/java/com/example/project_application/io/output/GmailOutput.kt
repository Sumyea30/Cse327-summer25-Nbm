package com.example.project_application.io.output

import android.util.Base64
import com.example.project_application.core.WorkflowOutput
import com.google.api.services.gmail.Gmail
import java.io.ByteArrayOutputStream
import java.util.Properties
import javax.mail.Message
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
            try {
                val email = MimeMessage(session).apply {
                    setFrom(InternetAddress(userEmail))
                    addRecipient(Message.RecipientType.TO, InternetAddress(recipient))
                    setSubject(subject)
                    setText(msg)
                }

                val buffer = ByteArrayOutputStream().apply {
                    email.writeTo(this)
                }

                val raw = Base64.encodeToString(buffer.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)

                val message = com.google.api.services.gmail.model.Message().apply {
                    this.raw = raw
                }

                service.users().messages().send(userEmail, message).execute()
                println("✅ Sent Gmail to $recipient: $subject")

            } catch (e: Exception) {
                println("GmailOutput Error: ${e.message}")
            }
        }
    }
}
