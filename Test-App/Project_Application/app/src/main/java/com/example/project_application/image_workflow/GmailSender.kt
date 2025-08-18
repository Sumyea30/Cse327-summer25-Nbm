package com.example.project_application.image_workflow

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Properties
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import java.util.Base64

fun getGmailService(context: Context, credential: GoogleAccountCredential): Gmail {
    val transport = GoogleNetHttpTransport.newTrustedTransport()
    val jsonFactory = GsonFactory.getDefaultInstance()
    return Gmail.Builder(transport, jsonFactory, credential)
        .setApplicationName("ProjectApplication")
        .build()
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun sendEmail(context: Context, credential: GoogleAccountCredential, to: String, subject: String, body: String, attachmentUri: Uri? = null) = withContext(Dispatchers.IO) {
    val service = getGmailService(context, credential)
    val mimeMessage = MimeMessage(Session.getDefaultInstance(Properties()))
    mimeMessage.setRecipient(javax.mail.Message.RecipientType.TO, InternetAddress(to))
    mimeMessage.subject = subject
    mimeMessage.setText(body)

    // Attachment handling (simplified; add multipart for production)
    if (attachmentUri != null) {
        // Implement multipart with DataSource if needed

    }

    val buffer = ByteArrayOutputStream()
    mimeMessage.writeTo(buffer)
    val encodedEmail = Base64.getUrlEncoder().encodeToString(buffer.toByteArray())
    val message = Message().setRaw(encodedEmail)
    service.users().messages().send("me", message).execute()
}