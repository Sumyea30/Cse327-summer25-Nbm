package com.example.project_application.core

interface WorkflowInput<T> {
    fun fetch(): List<T>
}