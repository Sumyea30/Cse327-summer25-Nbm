package com.example.project_application

class WorkflowManager(
    private val workflows: List<Workflow<*, *>>
) {
    fun runAll(subject: String, recipient: String) {
        workflows.forEach {
            try {
                it.run(subject, recipient)
            } catch (e: Exception) {
                println("Workflow failed: ${e.message}")
            }
        }
    }
}

