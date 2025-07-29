package com.example.project_application

class SimpleForwardProcessor : WorkflowProcessor<String, String> {
    override fun process(input: List<String>): List<String> {
        return input.map { "Processed: $it" }
    }
}
