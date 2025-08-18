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

@Composable
fun SettingsScreen(navController: NavController, credential: GoogleAccountCredential) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("workflow_prefs", Context.MODE_PRIVATE) }

    val interval = remember { mutableStateOf(prefs.getInt("interval_minutes", 60).toString()) }
    val homeWifi = remember { mutableStateOf(prefs.getString("home_wifi", "") ?: "") }
    val sonEmail = remember { mutableStateOf(prefs.getString("son_email", "") ?: "") }
    val doctorEmail = remember { mutableStateOf(prefs.getString("doctor_email", "") ?: "") }
    val telegramChatId = remember { mutableStateOf(prefs.getString("telegram_chat_id", "") ?: "") }
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Settings Saved") },
            text = { Text("Your Smart Image Workflow settings have been saved successfully.") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    navController.popBackStack()
                }) {
                    Text("OK")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Smart Image Workflow Settings", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)

        TextField(
            value = interval.value,
            onValueChange = { interval.value = it },
            label = { Text("Scan Interval (minutes)") },
            modifier = Modifier.padding(top = 16.dp)
        )

        TextField(
            value = homeWifi.value,
            onValueChange = { homeWifi.value = it },
            label = { Text("Home WiFi SSID (e.g., MyWiFi)") },
            modifier = Modifier.padding(top = 8.dp)
        )

        TextField(
            value = sonEmail.value,
            onValueChange = { sonEmail.value = it },
            label = { Text("Pictures to Relative's Email") },
            modifier = Modifier.padding(top = 8.dp)
        )

        TextField(
            value = doctorEmail.value,
            onValueChange = { doctorEmail.value = it },
            label = { Text("Report receiver's Email (eg.Doctor,dietitian)") },
            modifier = Modifier.padding(top = 8.dp)
        )

        TextField(
            value = telegramChatId.value,
            onValueChange = { telegramChatId.value = it },
            label = { Text("Receipt Calculation Telegram Chat ID") },
            modifier = Modifier.padding(top = 8.dp)
        )

        Button(
            onClick = {
                prefs.edit()
                    .putInt("interval_minutes", interval.value.toIntOrNull() ?: 60)
                    .putString("home_wifi", homeWifi.value)
                    .putString("son_email", sonEmail.value)
                    .putString("doctor_email", doctorEmail.value)
                    .putString("telegram_chat_id", telegramChatId.value)
                    .apply()
                MainActivity.enqueueImageScanWorker(context) // Re-enqueue with new interval
                navController.popBackStack()
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Save Settings")
        }
    }
}