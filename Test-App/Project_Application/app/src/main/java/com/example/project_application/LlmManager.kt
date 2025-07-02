package com.example.project_application

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.genai.llminference.GraphOptions
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.scale

object LlmManager {
    private var llm: LlmInference? = null
    private var session: LlmInferenceSession? = null

    // M1 – Text Only
    fun initM1(context: Context, modelFileName: String): Boolean {
        return try {
            val modelPath = "/data/local/tmp/gemma3-1b-it-int4.task"  // fixed location
           // val modelPath = "${context.filesDir}/gemma3-1b-it-int4.task"

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(512)
                .setPreferredBackend(LlmInference.Backend.GPU)
                .build()

            llm = LlmInference.createFromOptions(context, options)
            session = LlmInferenceSession.createFromOptions(
                llm!!,
                LlmInferenceSession.LlmInferenceSessionOptions.builder()
                    .setTopK(40)
                    .setTopP(0.95f)
                    .setTemperature(0.7f)
                    .build()
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun runM1Inference(prompt: String, onResult: (String, Boolean) -> Unit) {
        session?.addQueryChunk(prompt)
        session?.generateResponseAsync { partial, done ->
            onResult(partial, done)
        }
    }

    //  M2 – Multimodal (Text + Image)
    fun initM2(context: Context, modelFileName: String): Boolean {
        return try {
            val modelPath = "/data/local/tmp/gemma-3n-E2B-it-int4.task"
           // val modelPath = "${context.filesDir}/gemma-3n-E2B-it-int4.task"

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(128)
                .setMaxNumImages(1)
                .setPreferredBackend(LlmInference.Backend.CPU)
                .build()

            llm = LlmInference.createFromOptions(context, options)

            session = LlmInferenceSession.createFromOptions(
                llm!!,
                LlmInferenceSession.LlmInferenceSessionOptions.builder()
                    .setTopK(40)
                    .setTopP(0.95f)
                    .setTemperature(0.8f)
                    .setGraphOptions(
                        GraphOptions.builder()
                            .setEnableVisionModality(true)
                            .build()
                    )
                    .build()
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun runM2Inference(
        context: Context,
        prompt: String,
        image: Bitmap,
        onResult: (String, Boolean) -> Unit
    ) {
        try {
            // Fallback if BitmapImageBuilder fails:
            val scaledBitmap = image.scale(224, 224)
            val mpImage = BitmapImageBuilder(scaledBitmap).build()


            session?.addQueryChunk(prompt)
            session?.addImage(mpImage)
            session?.generateResponseAsync { partial, done ->
                onResult(partial, done)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult("⚠️ Failed to process image.", true)
        }
    }

    fun reset() {
        session?.close()
        session = null
    }

    fun cleanup() {
        session?.close()
        llm?.close()
        session = null
        llm = null
    }
}
