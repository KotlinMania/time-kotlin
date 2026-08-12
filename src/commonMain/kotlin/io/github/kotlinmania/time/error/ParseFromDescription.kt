// port-lint: source error/parse_from_description.rs
package io.github.kotlinmania.time.error

import io.github.kotlinmania.time.Error

/**
 * Error parsing an input into a `Parsed` struct.
 */

/** An error that occurred while parsing the input into a `Parsed` struct. */
sealed class ParseFromDescription(
    message: String,
) : IllegalArgumentException(message) {
    abstract fun fmt(): String

    /** A string literal was not what was expected. */
    class InvalidLiteral : ParseFromDescription("a character literal was not valid") {
        override fun fmt(): String = message ?: "a character literal was not valid"
    }

    /** A dynamic component was not valid. */
    class InvalidComponent(
        val name: String,
    ) : ParseFromDescription("the '$name' component could not be parsed") {
        override fun fmt(): String = message ?: "the '$name' component could not be parsed"
    }

    /** The input was expected to have ended, but there are characters that remain. */
    class UnexpectedTrailingCharacters : ParseFromDescription("unexpected trailing characters; the end of input was expected") {
        override fun fmt(): String =
            message ?: "unexpected trailing characters; the end of input was expected"
    }

    companion object {
        fun from(error: ParseFromDescription): Error = Error.ParseFromDescription(error)

        fun tryFrom(error: Error): Result<ParseFromDescription> =
            when (error) {
                is Error.ParseFromDescription -> Result.success(error.err)
                else -> Result.failure(DifferentVariant())
            }
    }
}
