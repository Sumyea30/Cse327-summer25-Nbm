package com.example.project_application

interface WorkflowProcessor<I, O> {
    fun process(input: List<I>): List<O>
}