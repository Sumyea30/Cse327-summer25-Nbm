package com.example.project_application.core

interface WorkflowOutput<T> {
    fun sendMessages(subject: String, recipient: String, messages: List<T>)
}