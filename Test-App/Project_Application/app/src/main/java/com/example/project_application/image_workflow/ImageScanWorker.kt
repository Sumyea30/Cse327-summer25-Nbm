package com.example.project_application.image_workflow

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageScanWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val prefs = applicationContext.getSharedPreferences("workflow_prefs", Context.MODE_PRIVATE)
        val lastScanTime = prefs.getLong("last_scan", 0L)
        val currentTime = System.currentTimeMillis()

        val images = mutableListOf<Uri>()
        val cursor = applicationContext.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED),
            "${MediaStore.Images.Media.DATE_ADDED} > ?",
            arrayOf((lastScanTime / 1000).toString()),
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                images.add(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString()))
            }
        }

        images.forEach { uri ->
            val image = InputImage.fromFilePath(applicationContext, uri)
            processImage(image, uri)
        }

        prefs.edit().putLong("last_scan", currentTime).apply()
        Result.success()
    }

    private suspend fun processImage(inputImage: InputImage, uri: Uri) {
        // Image labeling for "person" (son's pic)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        labeler.process(inputImage).addOnSuccessListener { labels ->
            val type = when {
                labels.any { it.text.contains("person", ignoreCase = true) && it.confidence > 0.7f } -> "son_pic"
                else -> "unknown"
            }
            if (type != "unknown") {
                storeForLater(applicationContext, type, uri.toString())
            }
        }

        // Text recognition for receipts/reports
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(inputImage).addOnSuccessListener { visionText ->
            val text = visionText.text.lowercase()
            when {
                text.contains("receipt") || text.matches(Regex(".*\\d+\\.\\d{2}.*")) -> {
                    val amount = extractAmount(text)
                    storeExpense(applicationContext, amount)
                    storeForLater(applicationContext, "receipt", uri.toString())
                }
                text.contains("hospital") -> {
                    storeForLater(applicationContext, "hospital_report", uri.toString())
                }
            }
        }
    }
}