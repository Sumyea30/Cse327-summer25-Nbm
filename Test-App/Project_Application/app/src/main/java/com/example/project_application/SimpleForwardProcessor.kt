package com.example.project_application

class SimpleForwardProcessor : WorkflowProcessor<String, String> {
    override fun process(data: List<String>): List<String> {
        return data // just forward
    }
}
