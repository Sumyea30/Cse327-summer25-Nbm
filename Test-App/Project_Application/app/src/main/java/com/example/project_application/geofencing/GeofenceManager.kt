package com.example.project_application.geofencing

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.tasks.await
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class GeofenceManager(private val context: Context) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private val geofenceHelper = GeofenceHelper(context)
    val scope = CoroutineScope(Dispatchers.IO)

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    suspend fun addGeofence(geofenceData: GeofenceData): Boolean {
        return try {
            val geofence = geofenceHelper.getGeofence(
                id = geofenceData.id,
                latLng = LatLng(geofenceData.latitude, geofenceData.longitude),
                radius = geofenceData.radius,
                transitionTypes = Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_DWELL or
                        Geofence.GEOFENCE_TRANSITION_EXIT
            )
            val request = geofenceHelper.getGeofencingRequest(geofence)
            geofencingClient.addGeofences(request, geofenceHelper.getPendingIntent()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removeGeofence(geofenceId: String): Boolean {
        return try {
            geofencingClient.removeGeofences(listOf(geofenceId)).await()
            context.getSharedPreferences("geofence_prefs", Context.MODE_PRIVATE)
                .edit {
                    remove("geofence_$geofenceId")
                }
            true
        } catch (e: Exception) {
            false
        }
    }
}