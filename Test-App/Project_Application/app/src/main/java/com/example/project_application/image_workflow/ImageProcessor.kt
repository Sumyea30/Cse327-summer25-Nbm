package com.example.project_application.image_workflow

import android.content.Context
import android.net.Uri
import android.graphics.Rect
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

class ImageProcessor(private val context: Context, private val credential: GoogleAccountCredential?) {

    // Configure face detection
    private val faceDetector: FaceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE) // or FAST
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        FaceDetection.getClient(options)
    }

    // Configure text recognition (Latin script)
    private val textRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun processWorkflow(
        type: String,
        delivery: String,
        receiver: String,
        message: String,
        wifiSsid: String,
        cycle: String,
        keyword: String?,
        sampleUri: Uri?
    ): Pair<Boolean, Float?> = withContext(Dispatchers.IO) {
        // Check and request storage permission if needed
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.w("ImageProcessor", "READ_EXTERNAL_STORAGE permission not granted, requesting...")
            if (context is ComponentActivity) {
                ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            } else {
                Log.e("ImageProcessor", "Context is not an Activity, cannot request permission")
                return@withContext Pair(false, null)
            }
            // Re-check after request (simplified, actual handling should be in Activity)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.e("ImageProcessor", "Permission still not granted after request")
                return@withContext Pair(false, null)
            }
        }

        val imageUris = getLastImages(context, 10) // Get last 10 images
        var success = false
        var maxAccuracy: Float? = null

        imageUris.forEach { uri ->
            try {
                val inputImage = InputImage.fromFilePath(context, uri)
                when (type) {
                    "Document" -> {
                        val result = textRecognizer.process(inputImage).await()
                        val text = result.text
                        val matchesKeyword = keyword?.let { text.contains(it, ignoreCase = true) } ?: true
                        if (matchesKeyword) {
                            success = sendImage(delivery, receiver, message, arrayOf(uri.toString()))
                            maxAccuracy = 1.0f // Full accuracy if keyword matches
                        }
                    }
                    "Character (face)" -> {
                        if (sampleUri != null) {
                            val sampleInputImage = InputImage.fromFilePath(context, sampleUri)
                            val sampleFaces = faceDetector.process(sampleInputImage).await()
                            val newFaces = faceDetector.process(inputImage).await()
                            if (sampleFaces.isNotEmpty() && newFaces.isNotEmpty()) {
                                val accuracy = compareFaces(sampleFaces.first(), newFaces.first())
                                if (accuracy >= 0.7f) {
                                    success = sendImage(delivery, receiver, message, arrayOf(uri.toString()))
                                }
                                if (accuracy > (maxAccuracy ?: 0f)) maxAccuracy = accuracy
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("ImageProcessor", "Failed to process Uri: $uri, Error: ${e.message}")
            } catch (e: Exception) {
                Log.e("ImageProcessor", "Unexpected error processing Uri: $uri, Error: ${e.message}")
            }
        }
        Pair(success, maxAccuracy)
    }

    private suspend fun sendImage(delivery: String, receiver: String, message: String, attachmentUris: Array<String>): Boolean {
        return if (delivery == "Gmail" && credential != null && credential.selectedAccount != null) {
            GmailSender.sendEmail(context, credential, receiver, "Workflow Result", message, attachmentUris)
        } else {
            false // Placeholder for Telegram
        }
    }

    private fun compareFaces(sampleFace: com.google.mlkit.vision.face.Face, newFace: com.google.mlkit.vision.face.Face): Float {
        val sampleBox = sampleFace.boundingBox
        val newBox = newFace.boundingBox
        val intersection = Rect(sampleBox)
        val intersected = intersection.intersect(newBox)
        val intersectionArea = if (intersected) intersection.width().toFloat() * intersection.height().toFloat() else 0f
        val sampleArea = sampleBox.width().toFloat() * sampleBox.height().toFloat()
        val newArea = newBox.width().toFloat() * newBox.height().toFloat()
        val unionArea = sampleArea + newArea - intersectionArea

        return if (unionArea == 0f) 0f else intersectionArea / unionArea
    }

    private fun getLastImages(context: Context, count: Int): List<Uri> {
        val uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor = context.contentResolver.query(uri, null, null, null, "${android.provider.MediaStore.Images.Media.DATE_ADDED} DESC")
        val uris = mutableListOf<Uri>()
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media._ID)
            while (it.moveToNext() && uris.size < count) {
                val id = it.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(uri, id.toString())
                // Validate Uri by checking if it can be opened
                val inputStream = context.contentResolver.openInputStream(contentUri)
                if (inputStream != null) {
                    inputStream.close()
                    uris.add(contentUri)
                } else {
                    Log.w("ImageProcessor", "Uri inaccessible: $contentUri")
                }
            }
        } ?: Log.w("ImageProcessor", "Cursor is null, no images found")
        return uris
    }
}