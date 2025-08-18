// TriggeredWorkflow.kt
package com.example.project_application.core

import android.content.Context
import android.widget.Toast

object TriggeredWorkflow {
    fun trigger(context: Context, message: String) {
        Toast.makeText(context, "Triggered: $message", Toast.LENGTH_LONG).show()
        // Add your actual logic here
    }
}
