package com.example.project_application.image_workflow

import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.util.Properties
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart

object GmailSender {
    private const val TAG = "GmailSender"

    fun sendEmail(
        context: Context,
        credential: GoogleAccountCredential,
        to: String,
        subject: String,
        body: String,
        attachmentUris: Array<String>
    ): Boolean {
        if (credential.selectedAccount == null) {
            Log.e(TAG, "No account selected for credential")
            return false
        }

        return try {
            val transport = NetHttpTransport()
            val jsonFactory = GsonFactory.getDefaultInstance()
            val gmailService = Gmail.Builder(transport, jsonFactory, credential)
                .setApplicationName("Smart Image Workflow")
                .build()

            val props = Properties()
            val session = Session.getInstance(props, null)

            val mimeMessage = MimeMessage(session).apply {
                setFrom(InternetAddress(credential.selectedAccount.name))
                setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(to))
                setSubject(subject)
                setText(body)
                if (attachmentUris.isNotEmpty()) {
                    val multipart = MimeMultipart()
                    val messagePart = MimeBodyPart()
                    messagePart.setText(body)
                    multipart.addBodyPart(messagePart)
                    attachmentUris.forEach { uri ->
                        val attachmentPart = MimeBodyPart()
                        val inputStream = context.contentResolver.openInputStream(android.net.Uri.parse(uri))
                        val byteArray = inputStream?.readBytes() ?: byteArrayOf()
                        attachmentPart.setDataHandler(javax.activation.DataHandler(ByteArrayInputStream(byteArray), "image/jpeg"))
                        attachmentPart.fileName = "attachment.jpg"
                        multipart.addBodyPart(attachmentPart)
                    }
                    setContent(multipart)
                }
            }

            val buffer = ByteArrayOutputStream()
            mimeMessage.writeTo(buffer)
            val bytes = buffer.toByteArray()
            val encodedEmail = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                .replace("\r\n", "")

            val message = Message().setRaw(encodedEmail)
            gmailService.users().messages().send("me", message).execute()
            Log.d(TAG, "Email sent successfully to $to")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email: ${e.message}", e)
            false
        }
    }
}