// port-lint: source error/parse_from_description.rs
package io.github.kotlinmania.time.error

/**
 * Error parsing an input into a `Parsed` struct.
 */

/** An error that occurred while parsing the input into a `Parsed` struct. */
sealed class ParseFromDescription(
    message: String,
) : IllegalArgumentException(message) {
    abstract fun fmt(): String

    open fun source(): Throwable? = null

    /** A string literal was not what was expected. */
    object InvalidLiteral : ParseFromDescription("a character literal was not valid") {
        operator fun invoke(): InvalidLiteral = this

        override fun fmt(): String = message ?: "a character literal was not valid"
    }

    /** A dynamic component was not valid. */
    class InvalidComponent(
        val name: String,
    ) : ParseFromDescription("the '$name' component could not be parsed") {
        override fun fmt(): String = message ?: "the '$name' component could not be parsed"

        override fun equals(other: Any?): Boolean =
            other is InvalidComponent && name == other.name

        override fun hashCode(): Int = name.hashCode()
    }

    /** The input was expected to have ended, but there are characters that remain. */
    object UnexpectedTrailingCharacters : ParseFromDescription("unexpected trailing characters; the end of input was expected") {
        operator fun invoke(): UnexpectedTrailingCharacters = this

        override fun fmt(): String =
            message ?: "unexpected trailing characters; the end of input was expected"
    }

    companion object {
        fun from(error: ParseFromDescription): StdError = StdError.ParseFromDescription(error)

        fun tryFrom(error: Parse): Result<ParseFromDescription> =
            Parse.tryParseFromDescription(error)

        fun tryFrom(error: StdError): Result<ParseFromDescription> =
            when (error) {
                is StdError.ParseFromDescription -> Result.success(error.err)
                else -> Result.failure(DifferentVariant())
            }
    }
}
