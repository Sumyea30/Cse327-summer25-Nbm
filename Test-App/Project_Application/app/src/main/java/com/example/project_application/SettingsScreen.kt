package com.example.project_application

import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.gmail.GmailScopes
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, credential: GoogleAccountCredential?) {
    val context = LocalContext.current
    var showDownloadScreen by remember { mutableStateOf(false) }
    var signedInAccount by remember { mutableStateOf(GoogleSignIn.getLastSignedInAccount(context)?.email ?: "Not signed in") }

    // Device Info
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    val totalRam = memoryInfo.totalMem / (1024 * 1024) // MB
    val availableRam = memoryInfo.availMem / (1024 * 1024) // MB
    val ramUsage = ((totalRam - availableRam) * 100 / totalRam).toInt()
    val cpuInfo = getCpuUsage()
    val appVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName
    val buildNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toString()
    } else {
        context.packageManager.getPackageInfo(context.packageName, 0).versionCode.toString()
    }

    if (showDownloadScreen) {
        DownloadScreen(
            onDownloadLater = { showDownloadScreen = false },
            onDownloadComplete = {
                showDownloadScreen = false
                Toast.makeText(context, "Download completed, returning to settings", Toast.LENGTH_SHORT).show()
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("App Settings") })
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Google Account
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Google Account: $signedInAccount", modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            if (credential != null) {
                                // Sign out
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()
                                    .build()
                                val client = GoogleSignIn.getClient(context, gso)
                                client.signOut()
                                CredentialHolder.credential = null
                                signedInAccount = "Not signed in"
                                Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                            } else {
                                // Trigger sign-in
                                navController.navigate("login")
                            }
                        }
                    ) {
                        Text(if (credential != null) "Sign Out" else "Sign In")
                    }
                }

                // Device Info
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Device Info", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("RAM Usage: $ramUsage% ($availableRam MB / $totalRam MB)")
                        Text("CPU Usage: $cpuInfo")
                    }
                }

                // App Info
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("App Info", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Version: $appVersion")
                        Text("Build Number: $buildNumber")
                    }
                }

                // Download Models Button
                Button(
                    onClick = { showDownloadScreen = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Download Missing Models")
                }
            }
        }
    }
}

@Composable
fun DownloadScreen(
    onDownloadLater: () -> Unit,
    onDownloadComplete: () -> Unit
) {
    val context = LocalContext.current

    val models = listOf(
        "gemma3-1b-it-int4.task" to "https://drive.google.com/uc?export=download&id=1ii8hB1PfdR5rFZF7wnWpG_tl8Jhtm_da",
        "gemma-3n-E2B-it-int4.task" to "https://drive.google.com/uc?export=download&id=1jssXlJs_N7-rbQ3jf_gACA3KVGJMnA4K"
    )

    val allExist = models.all { (name, _) ->
        context.getFileStreamPath(name).exists()
    }

    LaunchedEffect(Unit) {
        if (allExist) {
            Toast.makeText(context, "All models already downloaded", Toast.LENGTH_SHORT).show()
            onDownloadComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ADB Installed LLMs, Please Skip",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp),
            color = Color.Red,
            fontWeight = FontWeight.Bold
        )
        Image(
            painter = painterResource(id = R.drawable.download_help),
            contentDescription = "Download Instruction",
            modifier = Modifier
                .height(180.dp)
                .padding(16.dp)
        )

        Text(
            text = "To continue, tap each link below.\nIn Drive, choose your account → tap 'Download Anyway' if prompted.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Tip: If you see a warning from Google Drive, tap 'Download anyway'.\n" +
                    "You may need to tap the 3-dot menu ⋮ and choose 'Download'.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp),
            color = Color.Red,
            fontWeight = FontWeight.Bold
        )

        models.forEach { (name, url) ->
            Button(
                onClick = {
                    openInBrowser(context, url)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text("Download $name")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onDownloadLater,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Skip / Download Later")
        }
    }
}

fun openInBrowser(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        setPackage("com.android.chrome")
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(fallbackIntent)
    }
}

fun getCpuUsage(): String {
    return try {
        val statFile = File("/proc/stat")
        val stats = statFile.readText().split("\n")
        val cpuLine = stats.firstOrNull { it.startsWith("cpu ") }?.split("\\s+".toRegex()) ?: return "N/A"
        val total = cpuLine.drop(1).map { it.toLongOrNull() ?: 0L }.sum()
        val idle = cpuLine.getOrNull(4)?.toLongOrNull() ?: 0L
        val usage = ((total - idle) * 100 / total).toInt()
        "$usage%"
    } catch (e: Exception) {
        "N/A"
    }
}