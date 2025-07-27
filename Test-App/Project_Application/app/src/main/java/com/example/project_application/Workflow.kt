package com.example.project_application

import com.example.project_application.workflow.WorkflowData

abstract class Workflow<I, O> {
    abstract suspend fun run(data: WorkflowData<I, O>): WorkflowData<I, O>
}
