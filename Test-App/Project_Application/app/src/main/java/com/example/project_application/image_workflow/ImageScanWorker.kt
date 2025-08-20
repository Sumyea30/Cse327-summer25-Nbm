package com.example.project_application.image_workflow

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.project_application.MainActivity
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageScanWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val credential = MainActivity.credential
        if (credential == null) return Result.failure()

        val imageProcessor = ImageProcessor(applicationContext, credential)
        val workflow = WorkflowData(
            id = "scheduled_${System.currentTimeMillis()}",
            type = inputData.getString("type") ?: "Document",
            delivery = inputData.getString("delivery") ?: "Gmail",
            receiver = inputData.getString("receiver") ?: "",
            message = inputData.getString("message") ?: "",
            wifiSsid = inputData.getString("wifiSsid") ?: "",
            cycle = inputData.getString("cycle") ?: "Trigger right now",
            keyword = inputData.getString("keyword"),
            sampleUri = inputData.getString("sampleUri")?.let { Uri.parse(it) }
        )

        return withContext(Dispatchers.IO) {
            val (success, accuracy) = imageProcessor.processWorkflow(
                type = workflow.type,
                delivery = workflow.delivery,
                receiver = workflow.receiver,
                message = workflow.message,
                wifiSsid = workflow.wifiSsid,
                cycle = workflow.cycle,
                keyword = workflow.keyword,
                sampleUri = workflow.sampleUri
            )
            if (success) Result.success() else Result.retry()
        }
    }

    companion object {
        fun enqueue(context: Context, workflow: WorkflowData, credential: GoogleAccountCredential) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "image_scan_${workflow.id}",
                androidx.work.ExistingWorkPolicy.REPLACE,
                androidx.work.OneTimeWorkRequestBuilder<ImageScanWorker>()
                    .setInputData(
                        androidx.work.Data.Builder()
                            .putString("type", workflow.type)
                            .putString("delivery", workflow.delivery)
                            .putString("receiver", workflow.receiver)
                            .putString("message", workflow.message)
                            .putString("wifiSsid", workflow.wifiSsid)
                            .putString("cycle", workflow.cycle)
                            .putString("keyword", workflow.keyword)
                            .putString("sampleUri", workflow.sampleUri?.toString())
                            .build()
                    )
                    .build()
            )
        }
    }
}