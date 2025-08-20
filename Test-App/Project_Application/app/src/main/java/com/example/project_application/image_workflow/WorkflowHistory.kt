package com.example.project_application.image_workflow

import android.content.Context
import android.net.Uri
import androidx.core.content.edit

class WorkflowHistory(private val context: Context) {

    private val prefs = context.getSharedPreferences("workflow_history", Context.MODE_PRIVATE)

    fun saveWorkflow(workflow: WorkflowData) {
        val workflows = prefs.getStringSet("workflows", emptySet<String>())?.toMutableSet() ?: mutableSetOf()
        workflows.add(serializeWorkflow(workflow))
        prefs.edit { putStringSet("workflows", workflows) }
    }

    fun getAllWorkflows(): List<WorkflowData> {
        return prefs.getStringSet("workflows", emptySet<String>())?.mapNotNull { data: String -> deserializeWorkflow(data) } ?: emptyList()
    }

    private fun serializeWorkflow(workflow: WorkflowData): String {
        return "${workflow.id}|${workflow.type}|${workflow.delivery}|${workflow.receiver}|${workflow.message}|${workflow.wifiSsid}|${workflow.cycle}|${workflow.keyword ?: ""}|${workflow.sampleUri?.toString() ?: ""}"
    }

    private fun deserializeWorkflow(data: String): WorkflowData? {
        val parts = data.split("|")
        if (parts.size < 9) return null
        return WorkflowData(
            id = parts[0],
            type = parts[1],
            delivery = parts[2],
            receiver = parts[3],
            message = parts[4],
            wifiSsid = parts[5],
            cycle = parts[6],
            keyword = if (parts[7].isNotEmpty()) parts[7] else null,
            sampleUri = if (parts[8].isNotEmpty()) Uri.parse(parts[8]) else null
        )
    }
}