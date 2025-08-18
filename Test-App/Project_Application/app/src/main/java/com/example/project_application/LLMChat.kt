package com.example.project_application

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.graphics.ImageDecoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.History


@Composable
fun LlmChatScreen(
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Chat states
    var chatInput by remember { mutableStateOf(TextFieldValue("")) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val chatHistory = remember { mutableStateListOf<String>() }
    var isShowingHistory by remember { mutableStateOf(false) }

    // Speech to text launcher placeholder (you can integrate your STT here)
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { /* handle speech to text result here */ }
    )

    // Media picker launcher for images/docs
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            selectedUri = uri
        }
    )

    // Dialog to choose model, but here we do automatic decision
    var showModelDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Logo - replace with your drawable logo resource
                        Icon(
                            painter = painterResource(id = R.drawable.logo2),
                            contentDescription = "Logo",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Talk with Freely")
                    }
                },
                actions = {
                    IconButton(onClick = { isShowingHistory = true }) {
                        Icon(Icons.Default.History, contentDescription = "Chat History")
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
                        // Auto decide model:
                        if (selectedUri == null) {
                            // M1 - text only
                            sendMessageM1(
                                context,
                                chatInput.text.trim(),
                                chatHistory,
                                onClearInput = { chatInput = TextFieldValue("") }
                            )
                        } else {
                            // M2 - text + attachment
                            sendMessageM2(
                                context,
                                chatInput.text.trim(),
                                selectedUri,
                                chatHistory,
                                onClear = {
                                    chatInput = TextFieldValue("")
                                    selectedUri = null
                                }
                            )
                        }
                    }
                },
                onMediaClick = {
                    mediaPickerLauncher.launch(arrayOf("image/*", "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                },
                onSpeechClick = {
                    // Launch your speech-to-text intent here
                    // speechLauncher.launch(...)
                }
            )
        }
    ) { padding ->
        if (isShowingHistory) {
            ChatHistoryView(
                chatHistory = chatHistory,
                onBack = { isShowingHistory = false }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Greeting message
                Text(
                    text = "Hello, I'm Freely",
                    fontSize = 22.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Example conversation about cooking biriyani in a message bubble
                MessageBubble("Example: How to cook delicious biriyani?", isUser = false)

                Spacer(modifier = Modifier.height(12.dp))

                // Chat messages scrollable
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(chatHistory) { index, message ->
                        val isUser = message.startsWith("You:")
                        MessageBubble(message, isUser = isUser)
                    }
                }

                // Show selected file if any
                selectedUri?.let {
                    Text(
                        text = "📎 Selected file: ${it.lastPathSegment}",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomBar(
    value: TextFieldValue,
    onInputChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit,
    onMediaClick: () -> Unit,
    onSpeechClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSpeechClick) {
            Icon(Icons.Default.Mic, contentDescription = "Speech to Text")
        }
        IconButton(onClick = onMediaClick) {
            Icon(Icons.Default.AttachFile, contentDescription = "Add Attachment")
        }
        TextField(
            value = value,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Write Something...") },
            singleLine = true
        )
        Button(onClick = onSend) {
            Text("Talk with Freely")
        }
    }
}

@Composable
fun MessageBubble(message: String, isUser: Boolean) {
    val backgroundColor = if (isUser) MaterialTheme.colors.primary.copy(alpha = 0.1f)
    else MaterialTheme.colors.surface.copy(alpha = 0.15f)

    val alignment = if (isUser) Alignment.End else Alignment.Start
    val horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = horizontalArrangement
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor, shape = MaterialTheme.shapes.medium)
                .padding(12.dp)
                .widthIn(max = 320.dp)
        ) {
            Text(text = message, fontSize = 15.sp)
        }
    }
}

@Composable
fun ChatHistoryView(
    chatHistory: List<String>,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Chat History", fontSize = 22.sp, modifier = Modifier.padding(bottom = 12.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(chatHistory) { index, _ ->
                Text(
                    text = "Chat $index",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Could load specific chat if you save multiple chats */ }
                        .padding(vertical = 8.dp)
                )
                Divider()
            }
        }
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Back to Homepage")
        }
    }
}

fun sendMessageM1(
    context: android.content.Context,
    inputText: String,
    chatHistory: MutableList<String>,
    onClearInput: () -> Unit
) {
    if (inputText.isEmpty()) return

    // Load model M1
    val loaded = LlmManager.initM1(context, "gemma3-1b-it-int4.task")
    if (!loaded) {
        chatHistory.add("❌ Failed to load M1 model.")
        return
    }

    chatHistory.add("You: $inputText")
    onClearInput()

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
}

fun sendMessageM2(
    context: android.content.Context,
    inputText: String,
    selectedUri: Uri?,
    chatHistory: MutableList<String>,
    onClear: () -> Unit
) {
    if (inputText.isEmpty() || selectedUri == null) {
        chatHistory.add("❗ Input text or attachment missing.")
        return
    }

    val bitmap = uriToBitmap(context, selectedUri)
    if (bitmap == null) {
        chatHistory.add("⚠️ Failed to load image/document.")
        return
    }

    val loaded = LlmManager.initM2(context, "gemma-3n-E2B-it-int4.task")
    if (!loaded) {
        chatHistory.add("❌ Failed to load M2 model.")
        return
    }

    chatHistory.add("You: $inputText")
    onClear()

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
}

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
