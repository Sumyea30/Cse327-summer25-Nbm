package com.example.project_application

abstract class Workflow<I, O>(
    private val input: WorkflowInput<I>,
    private val processor: WorkflowProcessor<I, O>,
    private val output: WorkflowOutput<O>
) {
    fun run(subject: String, recipient: String) {
        val inputs = input.fetchLatestMessages()
        val processed = processor.process(inputs)
        output.sendMessages(subject, recipient, processed)
    }
}
