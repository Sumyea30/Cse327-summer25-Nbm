package com.example.project_application.geofencing

import android.Manifest
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import androidx.core.content.edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeofenceScreen(geofenceManager: GeofenceManager) {
    val context = LocalContext.current
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var radius by remember { mutableStateOf("200") }
    var locationName by remember { mutableStateOf("") }
    var receiverType by remember { mutableStateOf("Gmail") }
    var receiverDetails by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf<GeofenceData?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var geofences by remember { mutableStateOf(listOf<GeofenceData>()) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(23.8103, 90.4125), 16f) // Dhaka, Bangladesh
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            cameraPositionState.move(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(LatLng(23.8103, 90.4125), 16f))
        } else {
            Toast.makeText(context, "Location permissions required", Toast.LENGTH_LONG).show()
        }
    }

    // Load existing geofences
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("geofence_prefs", Context.MODE_PRIVATE)
        geofences = prefs.all.mapNotNull { entry ->
            Json.decodeFromString<GeofenceData>(entry.value as String)
        }
    }

    // Request permissions on launch
    LaunchedEffect(Unit) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        locationPermissionLauncher.launch(permissions)
    }

    // Add Geofence Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Geofence") },
            text = {
                Column {
                    TextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { Text("Location Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = radius,
                        onValueChange = { radius = it.filter { char -> char.isDigit() } }, // Only allow digits
                        label = { Text("Radius (meters)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Receiver Type:", modifier = Modifier.weight(1f))
                        Box {
                            Text(
                                text = receiverType,
                                modifier = Modifier
                                    .clickable { dropdownExpanded = true }
                                    .padding(8.dp)
                            )
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Gmail") },
                                    onClick = {
                                        receiverType = "Gmail"
                                        dropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Telegram") },
                                    onClick = {
                                        receiverType = "Telegram"
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = receiverDetails,
                        onValueChange = { receiverDetails = it },
                        label = { Text("Receiver Details (Email or Telegram ID)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedLocation == null || locationName.isEmpty() || radius.isEmpty() || receiverDetails.isEmpty() || message.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        val radiusValue = radius.toFloatOrNull()
                        if (radiusValue == null || radiusValue <= 0) {
                            Toast.makeText(context, "Invalid radius. Use a positive number.", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        val hasBackgroundLocationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        } else {
                            true
                        }
                        if (!hasLocationPermission || !hasBackgroundLocationPermission) {
                            Toast.makeText(context, "Location permissions required", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }

                        val geofenceData = GeofenceData(
                            id = UUID.randomUUID().toString(),
                            name = locationName,
                            latitude = selectedLocation!!.latitude,
                            longitude = selectedLocation!!.longitude,
                            radius = radiusValue,
                            receiverType = receiverType,
                            receiver = receiverDetails,
                            message = message
                        )
                        geofenceManager.scope.launch {
                            val success = geofenceManager.addGeofence(geofenceData)
                            withContext(Dispatchers.Main) {
                                if (success) {
                                    context.getSharedPreferences("geofence_prefs", Context.MODE_PRIVATE)
                                        .edit {
                                            putString("geofence_${geofenceData.id}", Json.encodeToString<GeofenceData>(geofenceData))
                                        }
                                    geofences = geofences + geofenceData
                                    showAddDialog = false
                                    Toast.makeText(context, "Geofence added", Toast.LENGTH_SHORT).show()
                                    // Reset form
                                    selectedLocation = null
                                    locationName = ""
                                    radius = "200"
                                    receiverType = "Gmail"
                                    receiverDetails = ""
                                    message = ""
                                } else {
                                    Toast.makeText(context, "Failed to add geofence", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Remove Geofence Dialog
    if (showRemoveDialog != null) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = null },
            title = { Text("Remove Geofence") },
            text = { Text("Are you sure you want to remove ${showRemoveDialog!!.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        geofenceManager.scope.launch {
                            val success = geofenceManager.removeGeofence(showRemoveDialog!!.id)
                            withContext(Dispatchers.Main) {
                                if (success) {
                                    context.getSharedPreferences("geofence_prefs", Context.MODE_PRIVATE)
                                        .edit {
                                            remove("geofence_${showRemoveDialog!!.id}")
                                        }
                                    geofences = geofences.filter { it.id != showRemoveDialog!!.id }
                                    Toast.makeText(context, "Geofence removed", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to remove geofence", Toast.LENGTH_SHORT).show()
                                }
                                showRemoveDialog = null
                            }
                        }
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = null }) {
                    Text("No")
                }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Geofence Workflow") }) },
        floatingActionButton = {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("+ Add Geofence")
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = true),
                    onMapLongClick = { latLng ->
                        selectedLocation = latLng
                        cameraPositionState.move(com.google.android.gms.maps.CameraUpdateFactory.newLatLng(latLng))
                    }
                ) {
                    selectedLocation?.let { latLng ->
                        Marker(
                            state = com.google.maps.android.compose.MarkerState(position = latLng),
                            title = locationName.takeIf { it.isNotEmpty() } ?: "Selected Location"
                        )
                        Circle(
                            center = latLng,
                            radius = radius.toDoubleOrNull() ?: 200.0,
                            strokeColor = androidx.compose.ui.graphics.Color.Red,
                            fillColor = androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.25f),
                            strokeWidth = 4f
                        )
                    }
                    geofences.forEach { geofence ->
                        Marker(
                            state = com.google.maps.android.compose.MarkerState(position = LatLng(geofence.latitude, geofence.longitude)),
                            title = geofence.name
                        )
                        Circle(
                            center = LatLng(geofence.latitude, geofence.longitude),
                            radius = geofence.radius.toDouble(),
                            strokeColor = androidx.compose.ui.graphics.Color.Blue,
                            fillColor = androidx.compose.ui.graphics.Color.Blue.copy(alpha = 0.25f),
                            strokeWidth = 4f
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(geofences) { geofence ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRemoveDialog = geofence },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(geofence.name, modifier = Modifier.weight(1f))
                        Text("Radius: ${geofence.radius}m", modifier = Modifier.padding(end = 8.dp))
                        Button(
                            onClick = { showRemoveDialog = geofence },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }
        }
    }
}

private fun Context.checkSelfPermission(permission: String): Int {
    return ContextCompat.checkSelfPermission(this, permission)
}