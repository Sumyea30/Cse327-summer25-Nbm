package com.example.project_application.ui.workflow

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.project_application.core.TriggeredWorkflow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential

// Replace with your actual package BuildConfig import if needed
import com.example.project_application.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkflowScreen(
    navController: NavController,
    credential: GoogleAccountCredential? // Pass the signed-in credential from your Activity
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Form state
    var source by remember { mutableStateOf("Gmail") }
    var destination by remember { mutableStateOf("Telegram") }
    var receiver by remember { mutableStateOf("") }
    var workflowType by remember { mutableStateOf("Message Forwarding") }
    var filterField by remember { mutableStateOf("From") }
    var filterValue by remember { mutableStateOf("") }
    var schedule by remember { mutableStateOf("Right now") }
    var isLoading by remember { mutableStateOf(false) }

    val sourceOptions = listOf("Gmail", "Telegram")
    val destinationOptions = listOf("Gmail", "Telegram")
    val workflowTypes = listOf("Message Forwarding", "Mail Summary (Gmail → Telegram)")
    val filterFields = listOf("From", "Subject Contains", "Has Attachment")
    val scheduleOptions = listOf("Right now", "Select Time")

    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text("Create Workflow") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(8.dp))

            // Source dropdown
            DropdownField("Source", source, sourceOptions) { source = it }
            Spacer(Modifier.height(12.dp))

            // Destination dropdown
            DropdownField("Destination", destination, destinationOptions) { destination = it }
            Spacer(Modifier.height(12.dp))

            // Receiver input
            OutlinedTextField(
                value = receiver,
                onValueChange = { receiver = it },
                label = { Text("Receiver's Gmail/Telegram ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Spacer(Modifier.height(12.dp))

            // Workflow type
            DropdownField("Type of Workflow", workflowType, workflowTypes) { workflowType = it }
            Spacer(Modifier.height(12.dp))

            // Filter (only if Gmail source)
            if (source == "Gmail") {
                DropdownField("Filter Field", filterField, filterFields) { filterField = it }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = filterValue,
                    onValueChange = { filterValue = it },
                    label = { Text("Filter Value") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }

            // Scheduling
            DropdownField("Schedule", schedule, scheduleOptions) { schedule = it }
            Spacer(Modifier.height(20.dp))

            // Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        // Trigger workflow action
                        scope.launch {
                            // Basic validation
                            if (source == "Gmail" && destination == "Telegram" && receiver.isBlank()) {
                                snackbarHostState.showSnackbar("Receiver (chat id) is required for Telegram destination")
                                return@launch
                            }

                            if (credential == null || credential.selectedAccountName == null) {
                                snackbarHostState.showSnackbar("Please sign in with Google to use Gmail features.")
                                return@launch
                            }

                            isLoading = true
                            try {
                                // Show immediate feedback
                                TriggeredWorkflow.trigger(context, "Starting workflow...")

                                // Run the workflow according to selections
                                if (source == "Gmail" && destination == "Telegram") {
                                    val gmailService = getGmailService(credential)
                                    val emails = withContext(Dispatchers.IO) {
                                        fetchGmailMessages(
                                            gmailService,
                                            filterField = filterField,
                                            filterValue = filterValue
                                        )
                                    }

                                    // If no emails, inform user
                                    if (emails.isEmpty()) {
                                        snackbarHostState.showSnackbar("No emails matched the filter.")
                                    } else {
                                        // Send each email snippet to Telegram
                                        withContext(Dispatchers.IO) {
                                            val botToken = BuildConfig.TELEGRAM_BOT_TOKEN
                                            for (msg in emails) {
                                                // Optionally you can add subject prefix etc.
                                                sendTelegramMessage(botToken, receiver, msg)
                                            }
                                        }
                                        snackbarHostState.showSnackbar("Workflow completed: ${emails.size} messages sent")
                                    }
                                } else if (source == "Telegram" && destination == "Gmail") {
                                    // Placeholder: Telegram -> Gmail flow would require collecting Telegram messages (polling/webhook)
                                    // We'll show a snackbar for now
                                    snackbarHostState.showSnackbar("Telegram → Gmail flow not implemented in this UI")
                                } else {
                                    snackbarHostState.showSnackbar("Selected source/destination combination not supported yet")
                                }

                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error: ${e.message ?: "unknown"}")
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Processing...")
                    } else {
                        Text("Trigger Workflow")
                    }
                }

                OutlinedButton(
                    onClick = { navController.navigate("workflowHistory") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Check Other Workflows")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallTopAppBar(title: @Composable () -> Unit) {
    TopAppBar(title = title)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/* -------------------------
   Helper functions
   ------------------------- */

fun getGmailService(credential: GoogleAccountCredential): Gmail {
    return Gmail.Builder(
        NetHttpTransport(),
        GsonFactory.getDefaultInstance(),
        credential
    ).setApplicationName("Project Application")
        .build()
}

/**
 * Fetches up to 5 messages (snippets) matching the provided filter.
 */
fun fetchGmailMessages(
    gmail: Gmail,
    userId: String = "me",
    filterField: String = "",
    filterValue: String = ""
): List<String> {
    val query = when (filterField) {
        "From" -> "from:${filterValue}"
        "Subject Contains" -> "subject:${filterValue}"
        "Has Attachment" -> "has:attachment"
        else -> ""
    }

    val result = gmail.users().messages().list(userId).apply {
        if (query.isNotEmpty()) q = query
        maxResults = 5
    }.execute()

    val messages = mutableListOf<String>()
    result.messages?.forEach { msg ->
        // Get full message and return snippet for simplicity (fast)
        val fullMessage = gmail.users().messages().get(userId, msg.id).setFormat("full").execute()
        val snippet = fullMessage.snippet ?: ""
        messages.add(snippet)
    }
    return messages
}

fun sendTelegramMessage(botToken: String, chatId: String, text: String) {
    // Using form POST to avoid URL length / encoding issues
    val url = "https://api.telegram.org/bot$botToken/sendMessage"
    val client = OkHttpClient()
    val body = FormBody.Builder()
        .add("chat_id", chatId)
        .add("text", text)
        .build()
    val request = Request.Builder()
        .url(url)
        .post(body)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw Exception("Telegram send failed: ${response.code}")
    }
}
