package com.example.project_application

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail

object GmailServiceFactory {
    fun createGmailService(context: Context, credential: GoogleAccountCredential): Gmail {
        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()

        return Gmail.Builder(transport, jsonFactory, credential)
            .setApplicationName("ProjectApplication")
            .build()
    }
}
