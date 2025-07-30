package com.example.project_application

class TelegramToGmailWorkflow(
    input: WorkflowInput<String>,
    processor: WorkflowProcessor<String, String>,
    output: WorkflowOutput<String>
) : Workflow<String, String>(input, processor, output)
