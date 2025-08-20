package com.example.project_application.image_workflow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.project_application.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.net.toUri // Kept for potential future String-to-Uri needs

class WifiReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        val ssid = info.ssid.replace("\"", "")
        val prefs = context.getSharedPreferences("workflow_prefs", Context.MODE_PRIVATE)
        val homeWifi = prefs.getString("home_wifi", "")

        if (ssid == homeWifi && homeWifi.isNotEmpty()) {
            Toast.makeText(context, "Home WiFi detected, triggering workflow...", Toast.LENGTH_SHORT).show()
            CoroutineScope(Dispatchers.IO).launch {
                val credential = MainActivity.credential
                if (credential != null) {
                    val imageProcessor = ImageProcessor(context, credential)
                    val workflows = WorkflowHistory(context).getAllWorkflows()
                    workflows.forEach { workflow ->
                        val (success, accuracy) = imageProcessor.processWorkflow(
                            type = workflow.type,
                            delivery = workflow.delivery,
                            receiver = workflow.receiver,
                            message = workflow.message,
                            wifiSsid = workflow.wifiSsid,
                            cycle = workflow.cycle,
                            keyword = workflow.keyword,
                            sampleUri = workflow.sampleUri // Removed .toUri()
                        )
                        launch(Dispatchers.Main) {
                            Toast.makeText(context, "Workflow ${workflow.id} completed. Accuracy: ${accuracy?.let { "%.2f".format(it * 100) } ?: "N/A"}%", Toast.LENGTH_LONG).show()
                            if (success) {
                                MainActivity.showNotification(context, "Workflow Success", "Images processed and sent!")
                            }
                        }
                    }
                }
            }
        }
    }
}