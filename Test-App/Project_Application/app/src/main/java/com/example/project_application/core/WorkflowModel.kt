package com.example.project_application.core

data class WorkflowModel(
    val id: Int,
    val source: String,
    val destination: String,
    val receiver: String,
    val type: String,
    val filterField: String?,
    val filterValue: String?,
    val schedule: String,
    val timestamp: Long
)
