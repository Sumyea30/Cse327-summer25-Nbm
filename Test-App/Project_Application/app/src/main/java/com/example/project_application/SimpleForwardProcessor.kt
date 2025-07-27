class SimpleForwardProcessor<I, O> {
    fun process(input: I): O {
        // processing logic (pass-through or format)

        return input as O
    }
}
