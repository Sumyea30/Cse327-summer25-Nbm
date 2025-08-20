package com.example.project_application.image_workflow

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.LaunchedEffect
import com.example.project_application.MainActivity
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import android.util.Log

data class WorkflowData(
    val id: String,
    val type: String,
    val delivery: String,
    val receiver: String,
    val message: String,
    val wifiSsid: String,
    val cycle: String,
    val keyword: String? = null,
    val sampleUri: Uri? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageWorkflowScreen(
    navController: NavController,
    context: Context,
    imageProcessor: ImageProcessor,
    workflowHistory: WorkflowHistory
) {
    var type by remember { mutableStateOf("Document") }
    var delivery by remember { mutableStateOf("Gmail") }
    var receiver by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var wifiSsid by remember { mutableStateOf("") }
    var cycle by remember { mutableStateOf("Trigger right now") }
    var keyword by remember { mutableStateOf("") }
    var sampleUri by remember { mutableStateOf<Uri?>(null) }
    var triggerWorkflow by remember { mutableStateOf<WorkflowData?>(null) }
    var permissionGranted by remember { mutableStateOf(context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) }
    var resultText by remember { mutableStateOf("") }

    val typeOptions = listOf("Document", "Character (face)")
    val deliveryOptions = listOf("Gmail", "Telegram")
    val cycleOptions = listOf("Trigger right now", "1min", "10 min", "30 min", "1hrs", "2hrs")

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        sampleUri = uri
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        permissionGranted = isGranted
        if (isGranted) {
            triggerWorkflow?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    val (success, accuracy) = imageProcessor.processWorkflow(
                        type = it.type,
                        delivery = it.delivery,
                        receiver = it.receiver,
                        message = it.message,
                        wifiSsid = it.wifiSsid,
                        cycle = it.cycle,
                        keyword = it.keyword,
                        sampleUri = it.sampleUri
                    )
                    withContext(Dispatchers.Main) {
                        resultText = "Success: $success, Accuracy: $accuracy"
                        if (success && accuracy ?: 0f >= 0.7f) {
                            MainActivity.showNotification(context, "Workflow Success", "Images processed and sent!")
                        }
                    }
                }
            }
        } else {
            MainActivity.showNotification(context, "Permission Denied", "Storage permission required for image processing.")
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionGranted) {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    LaunchedEffect(triggerWorkflow) {
        if (permissionGranted) {
            triggerWorkflow?.let { workflow ->
                workflowHistory.saveWorkflow(workflow)
                if (workflow.cycle == "Trigger right now") {
                    val (success, accuracy) = withContext(Dispatchers.IO) {
                        imageProcessor.processWorkflow(
                            type = workflow.type,
                            delivery = workflow.delivery,
                            receiver = workflow.receiver,
                            message = workflow.message,
                            wifiSsid = workflow.wifiSsid,
                            cycle = workflow.cycle,
                            keyword = workflow.keyword,
                            sampleUri = workflow.sampleUri
                        )
                    }
                    withContext(Dispatchers.Main) {
                        resultText = "Success: $success, Accuracy: $accuracy"
                        if (success && accuracy ?: 0f >= 0.7f) {
                            MainActivity.showNotification(context, "Workflow Success", "Images processed and sent!")
                        }
                    }
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        var typeExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = it }
        ) {
            OutlinedTextField(
                value = type,
                onValueChange = { },
                label = { Text("Type") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) }
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                typeOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            type = option
                            typeExpanded = false
                        }
                    )
                }
            }
        }

        var deliveryExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = deliveryExpanded,
            onExpandedChange = { deliveryExpanded = it }
        ) {
            OutlinedTextField(
                value = delivery,
                onValueChange = { },
                label = { Text("Delivery") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deliveryExpanded) }
            )
            ExposedDropdownMenu(
                expanded = deliveryExpanded,
                onDismissRequest = { deliveryExpanded = false }
            ) {
                deliveryOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            delivery = option
                            deliveryExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = receiver,
            onValueChange = { receiver = it },
            label = { Text("Receiver") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = wifiSsid,
            onValueChange = { wifiSsid = it },
            label = { Text("WiFi SSID (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        var cycleExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = cycleExpanded,
            onExpandedChange = { cycleExpanded = it }
        ) {
            OutlinedTextField(
                value = cycle,
                onValueChange = { },
                label = { Text("Cycle") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cycleExpanded) }
            )
            ExposedDropdownMenu(
                expanded = cycleExpanded,
                onDismissRequest = { cycleExpanded = false }
            ) {
                cycleOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            cycle = option
                            cycleExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = keyword,
            onValueChange = { keyword = it },
            label = { Text("Keyword (Optional for Document)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Select Sample Image")
        }

        Button(onClick = {
            val workflow = WorkflowData(
                id = System.currentTimeMillis().toString(),
                type = type,
                delivery = delivery,
                receiver = receiver,
                message = message,
                wifiSsid = wifiSsid,
                cycle = cycle,
                keyword = if (type == "Document" && keyword.isNotEmpty()) keyword else null,
                sampleUri = if (type == "Character (face)") sampleUri else null
            )
            triggerWorkflow = workflow
        }) {
            Text("Trigger/Save")
        }

        Text(text = resultText, modifier = Modifier.padding(top = 16.dp))
    }
}