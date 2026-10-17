// port-lint: source error/parse_from_description.rs
package io.github.kotlinmania.time.error

import io.github.kotlinmania.time.StdError

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

        override fun equals(other: Any?): Boolean = other is InvalidLiteral

        override fun hashCode(): Int = 1
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
    class UnexpectedTrailingCharacters : ParseFromDescription("unexpected trailing characters; the end of input was expected") {
        override fun fmt(): String =
            message ?: "unexpected trailing characters; the end of input was expected"

        override fun equals(other: Any?): Boolean = other is UnexpectedTrailingCharacters

        override fun hashCode(): Int = 2
    }

    companion object {
        fun from(error: ParseFromDescription): StdError = StdError.ParseFromDescription(error)

        fun tryFrom(error: StdError): Result<ParseFromDescription> =
            when (error) {
                is StdError.ParseFromDescription -> Result.success(error.err)
                else -> Result.failure(DifferentVariant())
            }
    }
}
