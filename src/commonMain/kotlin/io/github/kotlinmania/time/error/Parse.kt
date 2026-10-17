// port-lint: source error/parse.rs
package io.github.kotlinmania.time.error

import io.github.kotlinmania.time.StdError

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

        fun from(error: Parse): StdError =
            when (error) {
                is TryFromParsed -> StdError.TryFromParsed(error.error)
                is ParseFromDescription -> StdError.ParseFromDescription(error.error)
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

        fun tryFrom(error: StdError): Result<Parse> =
            when (error) {
                is StdError.ParseFromDescription -> Result.success(ParseFromDescription(error.err))
                is StdError.TryFromParsed -> Result.success(TryFromParsed(error.err))
                else -> Result.failure(DifferentVariant())
            }
    }
}
