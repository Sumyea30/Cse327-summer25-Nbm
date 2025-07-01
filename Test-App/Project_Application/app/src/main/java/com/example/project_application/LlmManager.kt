package com.example.project_application

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.genai.llminference.VisionModelOptions.Builder
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.google.mediapipe.tasks.genai.llminference.GraphOptions

object LlmManager {
    private var llm: LlmInference? = null
    private var session: LlmInferenceSession? = null

    // For M1 - Text Only
    fun initM1(context: Context, modelFileName: String): Boolean {
        return try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath("${context.filesDir}/$modelFileName")
                .setMaxTokens(256)
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

    fun runM1Inference(
        prompt: String,
        onResult: (String, Boolean) -> Unit
    ) {
        session?.addQueryChunk(prompt)
        session?.generateResponseAsync { partial, done ->
            onResult(partial, done)
        }
    }

    // For M2 - Image + Text
    fun initM2(context: Context, modelFileName: String): Boolean {
        return try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath("${context.filesDir}/$modelFileName")
                .setMaxTokens(512)
                .setMaxNumImages(1)
                .setPreferredBackend(LlmInference.Backend.GPU)
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
        prompt: String,
        image: Bitmap,
        onResult: (String, Boolean) -> Unit
    ) {
        session?.addQueryChunk(prompt)
        session?.addImage(BitmapImageBuilder(image).build())
        session?.generateResponseAsync { partial, done ->
            onResult(partial, done)
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
