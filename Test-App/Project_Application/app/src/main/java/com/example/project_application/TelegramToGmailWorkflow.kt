package com.example.project_application

class TelegramToGmailWorkflow(
    private val input: WorkflowInput<String>,
    private val processor: WorkflowProcessor<String, String>,
    private val output: GmailOutput
) : Workflow<String, String>(input, processor, object : WorkflowOutput<String> {
    override fun sendMessages(subject: String, recipient: String, messages: List<String>) {
        output.sendMessages(subject, recipient, messages)
    }
})
