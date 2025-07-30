package com.example.project_application

interface WorkflowInput<T> {
    fun fetch(): List<T>
}
