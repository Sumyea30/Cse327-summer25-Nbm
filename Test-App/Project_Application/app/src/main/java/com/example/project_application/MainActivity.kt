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
import com.example.project_application.Config
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.gmail.GmailScopes
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import android.util.Log



class MainActivity : ComponentActivity() {

    private val modelUrl = "https://drive.google.com/uc?export=download&id=1ii8hB1PfdR5rFZF7wnWpG_tl8Jhtm_da"
    private val modelFileName = "gemma3-1b.task"

    private var downloadId: Long = 0L

    private lateinit var credential: GoogleAccountCredential

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        registerReceiver(
            onDownloadComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            Context.RECEIVER_NOT_EXPORTED
        )

        setContent {
            Project_ApplicationTheme {
                var currentScreen by remember { mutableStateOf("login") }

                when (currentScreen) {
                    "login" -> LoginScreen {
                        Toast.makeText(this@MainActivity, "Login Success!", Toast.LENGTH_SHORT).show()

                        // Initialize Gmail credential
                        credential = GoogleAccountCredential.usingOAuth2(
                            this@MainActivity,
                            listOf(GmailScopes.GMAIL_SEND, GmailScopes.GMAIL_READONLY)
                        ).apply {
                            selectedAccount = GoogleSignIn.getLastSignedInAccount(this@MainActivity)?.account
                        }

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

                    "home" -> {
                        var workflowStarted by remember { mutableStateOf(false) }

                        HomeScreen()

                        LaunchedEffect(Unit) {
                            if (!workflowStarted) {
                                workflowStarted = true
                                Toast.makeText(this@MainActivity, "Workflow started", Toast.LENGTH_SHORT).show()

                                try {
                                    runWorkflow()
                                    Toast.makeText(this@MainActivity, "Workflow completed", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("Workflow", "Error: ", e)
                                    Toast.makeText(this@MainActivity, "Workflow crashed: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }


                    }
                }
            }
        }
    }

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                Toast.makeText(this@MainActivity, "Model download completed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun runWorkflow() = withContext(Dispatchers.IO) {
        val gmailService = GmailServiceFactory.createGmailService(this@MainActivity, credential)
        val gmailInput = GmailInput(gmailService)
        val telegramInput = TelegramInput(Config.TELEGRAM_BOT_TOKEN)
        val processor = SimpleForwardProcessor()

        val gmailOutput = GmailOutput(gmailService, Config.GMAIL_USER_EMAIL)
        val telegramOutput = TelegramOutput(Config.TELEGRAM_BOT_TOKEN, Config.TELEGRAM_CHAT_ID)

        val gmailToTelegram = GmailToTelegramWorkflow(gmailInput, processor, telegramOutput)
        val telegramToGmail = TelegramToGmailWorkflow(telegramInput, processor, gmailOutput)

        val manager = WorkflowManager(listOf(gmailToTelegram, telegramToGmail))
        manager.runAll("Freely", "ssadman552@gmail.com")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }
}