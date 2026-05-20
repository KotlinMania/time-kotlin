// port-lint: source error/format.rs
package io.github.kotlinmania.time.error

import io.github.kotlinmania.time.Error

/**
 * Error formatting a struct.
 */

/** An error occurred when formatting. */
sealed class Format(message: String) : IllegalArgumentException(message) {
    abstract fun fmt(): String

    open fun source(): Throwable? = null

    /** The type being formatted does not contain sufficient information to format a component. */
    class InsufficientTypeInformation :
        Format(
            "The type being formatted does not contain sufficient information to format a component.",
        ) {
        override fun fmt(): String =
            message
                ?: "The type being formatted does not contain sufficient information to format a component."
    }

    /**
     * The component named has a value that cannot be formatted into the requested format.
     *
     * This variant is only returned when using well-known formats.
     */
    class InvalidComponent(val component: String) :
        Format("The $component component cannot be formatted into the requested format.") {
        override fun fmt(): String =
            message ?: "The $component component cannot be formatted into the requested format."
    }

    /** A component provided was out of range. */
    class ComponentRange(val error: io.github.kotlinmania.time.error.ComponentRange) :
        Format(error.fmt()) {
        override fun fmt(): String = error.fmt()
        override fun source(): Throwable = error
    }

    /** A value of `Throwable` was returned internally. */
    class StdIo(val error: Throwable) : Format(error.message ?: error.toString()) {
        override fun fmt(): String = message ?: error.toString()
        override fun source(): Throwable = error
    }

    companion object {
        fun from(error: io.github.kotlinmania.time.error.ComponentRange): Format =
            ComponentRange(error)

        fun from(error: Throwable): Format = StdIo(error)

        fun from(error: Format): Error = Error.Format(error)

        fun tryFrom(error: Format): Result<io.github.kotlinmania.time.error.ComponentRange> =
            when (error) {
                is ComponentRange -> Result.success(error.error)
                else -> Result.failure(DifferentVariant())
            }

        fun tryFrom(error: Error): Result<Format> =
            when (error) {
                is Error.Format -> Result.success(error.err)
                else -> Result.failure(DifferentVariant())
            }

        fun tryThrowableFrom(error: Format): Result<Throwable> =
            when (error) {
                is StdIo -> Result.success(error.error)
                else -> Result.failure(DifferentVariant())
            }
    }
}
