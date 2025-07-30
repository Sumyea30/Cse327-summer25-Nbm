package com.example.project_application

abstract class Workflow<I, O>(
    private val input: WorkflowInput<I>,
    private val processor: WorkflowProcessor<I, O>,
    private val output: WorkflowOutput<O>
) {
    open fun run(subject: String = "", recipient: String = "") {
        val data = input.fetch()
        val processed = processor.process(data)
        output.sendMessages(subject, recipient, processed)
    }
}

