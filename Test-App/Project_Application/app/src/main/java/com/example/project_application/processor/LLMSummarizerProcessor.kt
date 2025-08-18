package com.example.project_application.processor

import android.content.Context
import com.example.project_application.LlmManager
import com.example.project_application.core.WorkflowProcessor
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LLMSummarizerProcessor(
    private val context: Context,
    private val modelFileName: String = "gemma3-1b-it-int4.task"
) : WorkflowProcessor<String, String> {

    private var isInitialized = false

    private fun ensureInit(): Boolean {
        if (!isInitialized) {
            isInitialized = LlmManager.initM1(context, modelFileName)
        }
        return isInitialized
    }

    override fun process(data: List<String>): List<String> {
        if (!ensureInit()) {
            return data.map { "Failed to initialize LLM model." }
        }

        return data.map { input ->
            runBlocking {
                runTextInferenceBlocking(input)
            }
        }
    }

    private suspend fun runTextInferenceBlocking(prompt: String): String =
        suspendCancellableCoroutine { cont ->
            val output = StringBuilder()

            LlmManager.runM1Inference(prompt) { partial, done ->
                output.append(partial)
                if (done) {
                    cont.resume(output.toString())
                }
            }
        }
}
