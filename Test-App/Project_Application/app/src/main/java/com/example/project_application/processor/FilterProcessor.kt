package com.example.project_application.processor

import com.example.project_application.core.WorkflowProcessor

class FilterProcessor(
    private val keyword: String
) : WorkflowProcessor<String, Boolean> {

    override fun process(data: List<String>): List<Boolean> {
        return data.map { input ->
            input.contains(keyword, ignoreCase = true)
        }
    }
}
