// port-lint: source error/try_from_parsed.rs
package io.github.kotlinmania.time.error

import io.github.kotlinmania.time.Error

/**
 * Error converting a `Parsed` struct to another type.
 */

/** An error that occurred when converting a `Parsed` to another type. */
sealed class TryFromParsed(message: String) : IllegalArgumentException(message) {
    abstract fun fmt(): String

    open fun source(): Throwable? = null

    /** The `Parsed` did not include enough information to construct the type. */
    class InsufficientInformation :
        TryFromParsed("the `Parsed` struct did not include enough information to construct the type") {
        override fun fmt(): String =
            message ?: "the `Parsed` struct did not include enough information to construct the type"
    }

    /** Some component contained an invalid value for the type. */
    class ComponentRange(val error: io.github.kotlinmania.time.error.ComponentRange) :
        TryFromParsed(error.fmt()) {
        override fun fmt(): String = error.fmt()
        override fun source(): Throwable = error
    }

    companion object {
        fun from(error: io.github.kotlinmania.time.error.ComponentRange): TryFromParsed =
            ComponentRange(error)

        fun from(error: TryFromParsed): Error = Error.TryFromParsed(error)

        fun tryFrom(error: TryFromParsed): Result<io.github.kotlinmania.time.error.ComponentRange> =
            when (error) {
                is ComponentRange -> Result.success(error.error)
                else -> Result.failure(DifferentVariant())
            }

        fun tryFrom(error: Error): Result<TryFromParsed> =
            when (error) {
                is Error.TryFromParsed -> Result.success(error.err)
                else -> Result.failure(DifferentVariant())
            }
    }
}
