package com.example.project_application.image_workflow

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.project_application.MainActivity
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import androidx.core.content.edit

@Composable
fun SettingsScreen(navController: NavController, credential: GoogleAccountCredential) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("workflow_prefs", Context.MODE_PRIVATE) }

    var interval by remember { mutableStateOf(prefs.getInt("interval_minutes", 60).toString()) }
    var homeWifi by remember { mutableStateOf(prefs.getString("home_wifi", "") ?: "") }
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Settings Saved") },
            text = { Text("Settings saved. Workflow will use new interval.") },
            confirmButton = {
                Button(onClick = { showDialog = false; navController.popBackStack() }) {
                    Text("OK")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Image Workflow Settings", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)

        TextField(
            value = interval,
            onValueChange = { interval = it },
            label = { Text("Scan Interval (minutes)") },
            modifier = Modifier.padding(top = 16.dp)
        )
        TextField(
            value = homeWifi,
            onValueChange = { homeWifi = it },
            label = { Text("Home WiFi SSID") },
            modifier = Modifier.padding(top = 8.dp)
        )

        Button(
            onClick = {
                prefs.edit {
                    putInt("interval_minutes", interval.toIntOrNull() ?: 60)
                        .putString("home_wifi", homeWifi)
                }
                MainActivity.enqueueImageScanWorker(context) // Re-enqueue with new interval
                showDialog = true
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Save Settings")
        }
    }
}