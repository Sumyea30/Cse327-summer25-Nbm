package com.example.project_application

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.core.content.ContextCompat.startActivity

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
            text = "ADB Installed LLMs,Please Skip",
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

// Opens link in Chrome if available, else default browser
fun openInBrowser(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        setPackage("com.android.chrome") // prefer Chrome
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Fallback to default browser
        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(fallbackIntent)
    }
}
