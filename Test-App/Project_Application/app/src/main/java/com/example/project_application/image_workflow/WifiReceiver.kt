package com.example.project_application.image_workflow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi

class WifiReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        val ssid = info.ssid.replace("\"", "") // Remove quotes
        val prefs = context.getSharedPreferences("workflow_prefs", Context.MODE_PRIVATE)
        if (ssid == prefs.getString("home_wifi", "")) {
            Toast.makeText(context, "Home detected, processing data...", Toast.LENGTH_SHORT).show()
        }
    }
}