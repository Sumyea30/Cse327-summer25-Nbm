package com.example.project_application

interface WorkflowInput<T> {
    fun fetchLatestMessages(): List<T>
}