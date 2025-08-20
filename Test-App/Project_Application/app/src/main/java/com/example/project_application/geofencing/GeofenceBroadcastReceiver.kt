package com.example.project_application.geofencing

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.project_application.CredentialHolder
import com.example.project_application.image_workflow.GmailSender.sendEmail
import com.example.project_application.sendTelegram
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GeofenceData(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float,
    val receiverType: String,
    val receiver: String,
    val message: String
)

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private companion object {
        const val TAG = "GeofenceBroadcastReceiver"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent?.hasError() == true) {
            Log.e(TAG, "Error receiving geofence event: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceList = geofencingEvent?.triggeringGeofences ?: return
        val transitionType = geofencingEvent.geofenceTransition
        val transitionString = when (transitionType) {
            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER -> "ENTER"
            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL -> "DWELL"
            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT -> "EXIT"
            else -> return
        }

        val prefs = context.getSharedPreferences("geofence_prefs", Context.MODE_PRIVATE)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        geofenceList.forEach { geofence ->
            val geofenceId = geofence.requestId
            val geofenceDataJson = prefs.getString("geofence_$geofenceId", null) ?: return@forEach
            val geofenceData = Json.decodeFromString<GeofenceData>(geofenceDataJson)

            // Send notification if permission is granted
            if (hasNotificationPermission) {
                val notification = Notification.Builder(context, "workflow_channel")
                    .setSmallIcon(com.example.project_application.R.drawable.ic_launcher_foreground)
                    .setContentTitle("Geofence $transitionString: ${geofenceData.name}")
                    .setContentText(geofenceData.message)
                    .setAutoCancel(true)
                    .build()
                NotificationManagerCompat.from(context).notify(geofenceId.hashCode(), notification)
            }

            // Send message based on receiver type
            runBlocking {
                when (geofenceData.receiverType) {
                    "Gmail" -> {
                        CredentialHolder.credential?.let { credential ->
                            sendEmail(context, credential, geofenceData.receiver, "Geofence $transitionString: ${geofenceData.name}", geofenceData.message, arrayOf()) // Added empty array for attachmentUris
                        }
                    }
                    "Telegram" -> {
                        sendTelegram(context, geofenceData.message, null)
                    }
                }
            }
        }
    }
}