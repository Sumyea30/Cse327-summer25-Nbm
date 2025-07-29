package com.example.project_application

class WorkflowManager(private val workflows: List<Workflow<*, *>>) {
    fun runAll() {
        workflows.forEach {
            try {
                it.run()
            } catch (e: Exception) {
                println("Workflow failed: ${e.message}")
            }
        }
    }
}
