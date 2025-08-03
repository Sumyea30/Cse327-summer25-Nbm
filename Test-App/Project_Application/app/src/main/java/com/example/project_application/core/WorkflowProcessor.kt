package com.example.project_application.core

interface WorkflowProcessor<I, O> {
    fun process(data: List<I>): List<O>
}