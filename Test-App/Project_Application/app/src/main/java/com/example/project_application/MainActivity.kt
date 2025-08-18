package com.example.project_application

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.project_application.core.WorkflowModel
import com.example.project_application.geofencing.GeofenceManager
import com.example.project_application.geofencing.GeofenceScreen
import com.example.project_application.image_workflow.ImageScanWorker
import com.example.project_application.image_workflow.SettingsScreen
import com.example.project_application.image_workflow.WifiReceiver
import com.example.project_application.instant_check.CameraScreen
import com.example.project_application.ui.theme.Project_ApplicationTheme
import com.example.project_application.ui.workflow.CreateWorkflowScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.gmail.GmailScopes
import com.google.firebase.FirebaseApp
import java.util.concurrent.TimeUnit

object CredentialHolder {
    var credential: GoogleAccountCredential? = null
}

class MainActivity : ComponentActivity() {

    private val modelUrl = "https://drive.google.com/uc?export=download&id=1ii8hB1PfdR5rFZF7wnWpG_tl8Jhtm_da"
    private val modelFileName = "gemma3-1b.task"
    private var downloadId: Long = 0L

    private lateinit var geofenceManager: GeofenceManager
    private lateinit var wifiReceiver: WifiReceiver

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                Toast.makeText(this@MainActivity, "Model download completed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val workflowsList = listOf(
        WorkflowModel(1, "src", "dest", "recv", "Type A", null, null, "daily", System.currentTimeMillis()),
        WorkflowModel(2, "src2", "dest2", "recv2", "Type B", null, null, "weekly", System.currentTimeMillis())
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        geofenceManager = GeofenceManager(this)
        wifiReceiver = WifiReceiver()

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "workflow_channel",
                "Workflow Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for Smart Image Workflow and Text Sharing"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Register receivers
        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), RECEIVER_NOT_EXPORTED)
        registerReceiver(wifiReceiver, IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION), RECEIVER_NOT_EXPORTED)

        setContent {
            Project_ApplicationTheme {
                val navController = rememberNavController()
                val workflows = remember { mutableStateListOf<WorkflowModel>().apply { addAll(workflowsList) } }
                var credential by remember { mutableStateOf<GoogleAccountCredential?>(null) }
                val context = LocalContext.current

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        AuthScreen(
                            onLoginSuccess = {
                                Toast.makeText(context, "Login Success!", Toast.LENGTH_SHORT).show()
                                credential = GoogleAccountCredential.usingOAuth2(
                                    context,
                                    listOf(GmailScopes.GMAIL_SEND, GmailScopes.GMAIL_READONLY)
                                ).apply {
                                    selectedAccount = GoogleSignIn.getLastSignedInAccount(context)?.account
                                }
                                CredentialHolder.credential = credential
                                navController.navigate("permissions") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("permissions") {
                        PermissionScreen {
                            navController.navigate("onboarding") {
                                popUpTo("permissions") { inclusive = true }
                            }
                        }
                    }

                    composable("onboarding") {
                        OnboardingScreen {
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    }

                    composable("home") {
                        HomeScreen(
                            onCreateWorkflow = { navController.navigate("createWorkflow") },
                            onZoneWorkflow = { navController.navigate("geofence") },
                            onCreateImageWorkflow = { navController.navigate("imageWorkflow") },
                            onCamera = { navController.navigate("camera") },
                            onCreateTasks = { navController.navigate("createTasks") },
                            onSettings = { navController.navigate("settings") },
                            onWorkflowHistory = { navController.navigate("workflowHistory") },
                            onTalkWithFreely = { navController.navigate("llmchat") }
                        )
                    }
                    composable("createTasks") {
                        CreateTasksScreen(navController = navController)
                    }
                    composable("settings") {
                        SettingsScreen(navController = navController, credential = credential)
                    }
                    composable("geofence") {
                        GeofenceScreen(geofenceManager)
                    }

                    composable("createWorkflow") {
                        if (credential != null) {
                            CreateWorkflowScreen(navController = navController, credential = credential!!)
                        } else {
                            LaunchedEffect(Unit) {
                                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        }
                    }

                    composable("imageWorkflow") {
                        if (credential != null) {
                            SettingsScreen(navController = navController, credential = credential!!)
                        } else {
                            LaunchedEffect(Unit) {
                                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        }
                    }

                    composable("workflowHistory") {
                        WorkflowHistoryScreen(
                            navController = navController,
                            workflows = workflows,
                            onClearHistory = { workflows.clear() }
                        )
                    }

                    composable("llmchat") {
                        LlmChatScreen(onBackToHome = { navController.popBackStack() })
                    }

                    composable("camera") {
                        if (credential != null) {
                            CameraScreen(
                                navController = navController,
                                credential = credential!!
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }

        // Enqueue WorkManager for image scanning
        enqueueImageScanWorker(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
        unregisterReceiver(wifiReceiver)
        CredentialHolder.credential = null
    }

    companion object {
        fun enqueueImageScanWorker(context: Context) {
            val prefs = context.getSharedPreferences("workflow_prefs", Context.MODE_PRIVATE)
            val intervalMinutes = prefs.getInt("interval_minutes", 60).toLong()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<ImageScanWorker>(intervalMinutes, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "image_scan",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}