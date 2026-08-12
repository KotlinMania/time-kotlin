// port-lint: source error/parse.rs
package io.github.kotlinmania.time.error

import io.github.kotlinmania.time.Error

/**
 * Error that occurred at some stage of parsing.
 */

/** An error that occurred at some stage of parsing. */
sealed class Parse(
    message: String,
) : IllegalArgumentException(message) {
    abstract fun fmt(): String

    open fun source(): Throwable? = null

    class TryFromParsed(
        val error: io.github.kotlinmania.time.error.TryFromParsed,
    ) : Parse(error.fmt()) {
        override fun fmt(): String = error.fmt()

        override fun source(): Throwable = error
    }

    class ParseFromDescription(
        val error: io.github.kotlinmania.time.error.ParseFromDescription,
    ) : Parse(error.fmt()) {
        override fun fmt(): String = error.fmt()

        override fun source(): Throwable = error
    }

    companion object {
        fun from(error: io.github.kotlinmania.time.error.TryFromParsed): Parse =
            TryFromParsed(error)

        fun from(error: io.github.kotlinmania.time.error.ParseFromDescription): Parse =
            ParseFromDescription(error)

        fun from(error: Parse): Error =
            when (error) {
                is TryFromParsed -> Error.TryFromParsed(error.error)
                is ParseFromDescription -> Error.ParseFromDescription(error.error)
            }

        fun tryFromParsed(error: Parse): Result<io.github.kotlinmania.time.error.TryFromParsed> =
            when (error) {
                is TryFromParsed -> Result.success(error.error)
                else -> Result.failure(DifferentVariant())
            }

        fun tryParseFromDescription(
            error: Parse,
        ): Result<io.github.kotlinmania.time.error.ParseFromDescription> =
            when (error) {
                is ParseFromDescription -> Result.success(error.error)
                else -> Result.failure(DifferentVariant())
            }

        fun tryFrom(error: Error): Result<Parse> =
            when (error) {
                is Error.ParseFromDescription -> Result.success(ParseFromDescription(error.err))
                is Error.TryFromParsed -> Result.success(TryFromParsed(error.err))
                else -> Result.failure(DifferentVariant())
            }
    }
}
