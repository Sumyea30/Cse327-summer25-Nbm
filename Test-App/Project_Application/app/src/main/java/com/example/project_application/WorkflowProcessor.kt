package com.example.project_application

interface WorkflowProcessor<I, O> {
    fun process(data: List<I>): List<O>
}
