package com.example.project_application.instant_check

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.camera.core.AspectRatio
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.example.project_application.image_workflow.sendEmail
import com.example.project_application.sendTelegram
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CameraScreen(navController: NavController, credential: GoogleAccountCredential) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(context.checkSelfPermission(Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Camera permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Camera permission required for text scanning", Toast.LENGTH_LONG).show()
        }
    }

    if (hasCameraPermission) {
        CameraContent(credential)
    } else {
        NoPermissionScreen { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraContent(credential: GoogleAccountCredential) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    var detectedText by remember { mutableStateOf("No text detected yet...") }
    var showShareDialog by remember { mutableStateOf(false) }

    fun onTextUpdated(updatedText: String) {
        detectedText = updatedText
    }

    if (showShareDialog) {
        ShareDialog(
            text = detectedText,
            credential = credential,
            onDismiss = { showShareDialog = false },
            context = context
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Text Scanner") }) },
        floatingActionButton = {
            Button(onClick = { showShareDialog = true }) {
                Text("Share Text")
            }
        }
    ) { paddingValues: PaddingValues ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(Color.BLACK)
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_START
                    }.also { previewView ->
                        startTextRecognition(
                            context = ctx,
                            cameraController = cameraController,
                            lifecycleOwner = lifecycleOwner,
                            previewView = previewView,
                            onDetectedTextUpdated = ::onTextUpdated
                        )
                    }
                }
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Color.White)
                    .padding(16.dp),
                text = detectedText
            )
        }
    }
}

@Composable
private fun NoPermissionScreen(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = "Please grant the camera permission to scan text."
            )
            Button(onClick = onRequestPermission) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Camera"
                )
                Text("Grant permission")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ShareDialog(
    text: String,
    credential: GoogleAccountCredential,
    onDismiss: () -> Unit,
    context: Context
) {
    val prefs = context.getSharedPreferences("workflow_prefs", Context.MODE_PRIVATE)
    val sonEmail = prefs.getString("son_email", "") ?: ""
    val doctorEmail = prefs.getString("doctor_email", "") ?: ""
    val telegramChatId = prefs.getString("telegram_chat_id", "") ?: ""

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Detected Text") },
        text = { Text("Choose where to send the scanned text:") },
        confirmButton = {
            if (sonEmail.isNotEmpty()) {
                TextButton(onClick = {
                    kotlinx.coroutines.runBlocking {
                        sendEmail(context, credential, sonEmail, "Scanned Text", text)
                        sendNotification(context, "Text shared to Grandpa ($sonEmail)")
                    }
                    onDismiss()
                }) {
                    Text("Send to Grandpa ($sonEmail)")
                }
            }
            if (doctorEmail.isNotEmpty()) {
                TextButton(onClick = {
                    kotlinx.coroutines.runBlocking {
                        sendEmail(context, credential, doctorEmail, "Scanned Text", text)
                        sendNotification(context, "Text shared to Doctor ($doctorEmail)")
                    }
                    onDismiss()
                }) {
                    Text("Send to Doctor ($doctorEmail)")
                }
            }
            if (telegramChatId.isNotEmpty()) {
                TextButton(onClick = {
                    kotlinx.coroutines.runBlocking {
                        sendTelegram(context, text, null)
                        sendNotification(context, "Text shared to Telegram")
                    }
                    onDismiss()
                }) {
                    Text("Send to Telegram")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun sendNotification(context: Context, message: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "workflow_channel",
            "Workflow Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for Smart Image Workflow and Text Sharing"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val notification = Notification.Builder(context, "workflow_channel")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Text Share")
        .setContentText(message)
        .setAutoCancel(true)
        .build()
    notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
}

private fun startTextRecognition(
    context: Context,
    cameraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onDetectedTextUpdated: (String) -> Unit
) {
    cameraController.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
    cameraController.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(context),
        TextRecognitionAnalyzer(onDetectedTextUpdated = onDetectedTextUpdated)
    )
    cameraController.bindToLifecycle(lifecycleOwner)
    previewView.controller = cameraController
}

class TextRecognitionAnalyzer(
    private val onDetectedTextUpdated: (String) -> Unit
) : ImageAnalysis.Analyzer {

    companion object {
        const val THROTTLE_TIMEOUT_MS = 1_000L
    }

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        scope.launch {
            val mediaImage = imageProxy.image ?: run { imageProxy.close(); return@launch }
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            suspendCoroutine { continuation ->
                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText: Text ->
                        val detectedText = visionText.text
                        if (detectedText.isNotBlank()) {
                            onDetectedTextUpdated(detectedText)
                        }
                    }
                    .addOnCompleteListener {
                        continuation.resume(Unit)
                    }
            }

            delay(THROTTLE_TIMEOUT_MS)
        }.invokeOnCompletion { exception ->
            exception?.printStackTrace()
            imageProxy.close()
        }
    }
}