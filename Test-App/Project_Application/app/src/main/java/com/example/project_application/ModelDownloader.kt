package com.example.project_application

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import java.io.File

object ModelDownloader {

    private val MODEL_FILES = listOf(
        "gemma3-1b-it-int4.task" to "https://drive.google.com/uc?export=download&id=1ii8hB1PfdR5rFZF7wnWpG_tl8Jhtm_da",
        "gemma-3n-E2B-it-int4.task" to "https://drive.google.com/uc?export=download&id=1jssXlJs_N7-rbQ3jf_gACA3KVGJMnA4K"
    )



    fun areAllModelsPresent(context: Context): Boolean {
        return MODEL_FILES.all { (name, _) ->
            File(context.filesDir, name).exists()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun downloadAllModels(
        context: Context,
        onComplete: () -> Unit,
        onFail: (String) -> Unit
    ) {
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadIdToFileMap = mutableMapOf<Long, String>()
        val downloadedFiles = mutableSetOf<String>()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                val fileName = downloadIdToFileMap[id]

                if (id != null && fileName != null) {
                    val downloadedFile = File(
                        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        fileName
                    )

                    if (downloadedFile.exists()) {
                        val finalDestination = File(context.filesDir, fileName)
                        downloadedFile.copyTo(finalDestination, overwrite = true)
                        downloadedFile.delete() // optional cleanup
                        downloadedFiles.add(fileName)

                        if (downloadedFiles.size == MODEL_FILES.size) {
                            context.unregisterReceiver(this)
                            onComplete()
                        }
                    } else {
                        context.unregisterReceiver(this)
                        onFail("Failed to download $fileName.")
                    }
                }
            }
        }

        context.registerReceiver(
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            Context.RECEIVER_NOT_EXPORTED
        )

        for ((fileName, url) in MODEL_FILES) {
            val finalFile = File(context.filesDir, fileName)
            if (finalFile.exists()) {
                downloadedFiles.add(fileName)
                if (downloadedFiles.size == MODEL_FILES.size) {
                    context.unregisterReceiver(receiver)
                    onComplete()
                }
                continue
            }

            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Downloading $fileName")
                .setDescription("Preparing $fileName for offline use.")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)

            val downloadId = manager.enqueue(request)
            downloadIdToFileMap[downloadId] = fileName
        }

    }
}
