package com.example.project_application

class GmailToTelegramWorkflow(
    input: WorkflowInput<String>,
    processor: WorkflowProcessor<String, String>,
    private val output: TelegramOutput
) : Workflow<String, String>(input, processor, object : WorkflowOutput<String> {
    override fun sendMessages(subject: String, recipient: String, messages: List<String>) {
        output.sendMessages(subject, recipient, messages)
    }

    }

)


