package com.example.project_application

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.graphics.ImageDecoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.DrawerValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var chatInput by remember { mutableStateOf(TextFieldValue("")) }
    val chatHistory = remember { mutableStateListOf("Hi there!", "Hello! I'm Freely.") }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    // M1/M2 selector dialog
    var showModelDialog by remember { mutableStateOf(false) }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            selectedUri = uri
        }
    )

    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Chat History", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                chatHistory.forEachIndexed { index, message ->
                    Text("Chat $index", modifier = Modifier.padding(vertical = 8.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                Divider()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { /* open settings */ }
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Settings")
                }
            }
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Freely") },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                },
                bottomBar = {
                    BottomBar(
                        value = chatInput,
                        onInputChange = { chatInput = it },
                        onSend = {
                            if (chatInput.text.isNotBlank() || selectedUri != null) {
                                showModelDialog = true
                            }
                        },
                        onMediaClick = {
                            mediaPickerLauncher.launch(
                                arrayOf(
                                    "image/*",
                                    "video/*",
                                    "application/pdf"
                                )
                            )
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            items(chatHistory) { message ->
                                Text(
                                    text = message,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    selectedUri?.let {
                        Text(
                            text = "📎 Selected file: ${it.lastPathSegment}",
                            fontSize = 12.sp,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                if (showModelDialog) {
                    AlertDialog(
                        onDismissRequest = { showModelDialog = false },
                        title = { Text("Choose Model") },
                        text = { Text("Do you want to use M1 (Text LLM) or M2 (Multimodal)?") },

                        // M1 – Text only
                        confirmButton = {
                            TextButton(onClick = {
                                val inputText = chatInput.text.trim()
                                if (inputText.isEmpty()) return@TextButton

                                val modelLoaded = LlmManager.initM1(context, "gemma3-1b-it-int4.task")
                                if (!modelLoaded) {
                                    chatHistory.add("❌ Failed to load M1 model.")
                                    return@TextButton
                                }

                                chatHistory.add("You: $inputText")
                                chatInput = TextFieldValue("")
                                selectedUri = null
                                showModelDialog = false

                                var lastIndex = -1
                                LlmManager.runM1Inference(inputText) { partial, done ->
                                    if (lastIndex == -1) {
                                        chatHistory.add("Freely (M1): $partial")
                                        lastIndex = chatHistory.lastIndex
                                    } else {
                                        chatHistory[lastIndex] += partial
                                    }
                                    if (done) {
                                        LlmManager.reset()
                                    }
                                }
                            }) {
                                Text("M1")
                            }
                        },

                        // M2 – Multimodal
                        dismissButton = {
                            TextButton(onClick = {
                                val inputText = chatInput.text.trim()
                                if (inputText.isEmpty() || selectedUri == null) {
                                    chatHistory.add("❗ Input text or image missing.")
                                    return@TextButton
                                }

                                val bitmap = uriToBitmap(context, selectedUri!!)
                                if (bitmap == null) {
                                    chatHistory.add("⚠️ Failed to load image.")
                                    return@TextButton
                                }

                                val modelLoaded = LlmManager.initM2(context, "gemma-3n-E2B-it-int4.task")
                                if (!modelLoaded) {
                                    chatHistory.add("❌ Failed to load M2 model.")
                                    return@TextButton
                                }

                                chatHistory.add("You: $inputText")
                                chatInput = TextFieldValue("")
                                selectedUri = null
                                showModelDialog = false

                                var lastIndex = -1
                                LlmManager.runM2Inference(context, inputText, bitmap) { partial, done ->
                                    if (lastIndex == -1) {
                                        chatHistory.add("Freely (M2): $partial")
                                        lastIndex = chatHistory.lastIndex
                                    } else {
                                        chatHistory[lastIndex] += partial
                                    }
                                    if (done) {
                                        LlmManager.reset()
                                    }
                                }
                            }) {
                                Text("M2")
                            }
                        }
                    )
                }
            }
        }
    )
}

// BottomBar Composable
@Composable
fun BottomBar(
    value: TextFieldValue,
    onInputChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit,
    onMediaClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMediaClick) {
            Icon(Icons.Default.Add, contentDescription = "Add Media")
        }

        TextField(
            value = value,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type something...") },
            singleLine = true
        )

        IconButton(onClick = onSend) {
            Icon(Icons.Default.Send, contentDescription = "Send")
        }
    }
}

// URI → Bitmap
fun uriToBitmap(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
