package com.example.project_application

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.project_application.ui.theme.Project_ApplicationTheme
import com.google.firebase.FirebaseApp
import java.io.File
import android.content.Context.RECEIVER_NOT_EXPORTED
import androidx.annotation.RequiresApi

class MainActivity : ComponentActivity() {

    private val modelUrl = "https://drive.google.com/uc?export=download&id=1ii8hB1PfdR5rFZF7wnWpG_tl8Jhtm_da"
    private val modelFileName = "gemma3-1b.task"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        registerReceiver(
            onDownloadComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            RECEIVER_NOT_EXPORTED
        )


        setContent {
            Project_ApplicationTheme {
                var currentScreen by remember { mutableStateOf("login") }

                when (currentScreen) {
                    "login" -> LoginScreen {
                        Toast.makeText(this, "Login Success!", Toast.LENGTH_SHORT).show()
                        currentScreen = "permissions"
                    }

                    "permissions" -> PermissionScreen {
                        currentScreen = "onboarding"
                    }

                    "onboarding" -> OnboardingScreen {
                        currentScreen = "download"
                    }

                    "download" -> DownloadScreen(
                        onDownloadLater = { currentScreen = "home" },
                        onDownloadComplete = { currentScreen = "home" }
                    )

                    "home" -> HomeScreen()
                }
            }
        }
    }

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Toast.makeText(applicationContext, "Model download completed!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }
}
