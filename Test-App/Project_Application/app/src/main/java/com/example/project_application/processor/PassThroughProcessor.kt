package com.example.project_application.processor

import com.example.project_application.core.WorkflowProcessor

class PassThroughProcessor : WorkflowProcessor<String, String> {
    override fun process(data: List<String>): List<String> {
        return data // just forward
    }
}