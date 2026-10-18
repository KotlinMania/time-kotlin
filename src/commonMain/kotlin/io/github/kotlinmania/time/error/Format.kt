// port-lint: source error/format.rs
package io.github.kotlinmania.time.error

import io.github.kotlinmania.serde.SerdeError

/**
 * Error formatting a struct.
 */

/** An error occurred when formatting. */
sealed class Format(
    message: String,
) : IllegalArgumentException(message) {
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

        override fun equals(other: Any?): Boolean = other is InsufficientTypeInformation

        override fun hashCode(): Int = 1
    }

    /**
     * The component named has a value that cannot be formatted into the requested format.
     *
     * This variant is only returned when using well-known formats.
     */
    class InvalidComponent(
        val component: String,
    ) : Format("The $component component cannot be formatted into the requested format.") {
        override fun fmt(): String =
            message ?: "The $component component cannot be formatted into the requested format."

        override fun equals(other: Any?): Boolean =
            other is InvalidComponent && component == other.component

        override fun hashCode(): Int = component.hashCode()
    }

    /** A component provided was out of range. */
    class ComponentRange(
        val error: io.github.kotlinmania.time.error.ComponentRange,
    ) : Format(error.fmt()) {
        val err: io.github.kotlinmania.time.error.ComponentRange
            get() = error

        override fun fmt(): String = error.fmt()

        override fun source(): Throwable = error

        override fun equals(other: Any?): Boolean =
            other is ComponentRange && error == other.error

        override fun hashCode(): Int = error.hashCode()
    }

    /** A value of `Throwable` was returned internally. */
    class StdIo(
        val error: Throwable,
    ) : Format(error.message ?: error.toString()) {
        val err: Throwable
            get() = error

        override fun fmt(): String = message ?: error.toString()

        override fun source(): Throwable = error

        override fun equals(other: Any?): Boolean =
            other is StdIo && error == other.error

        override fun hashCode(): Int = error.hashCode()
    }

    /** Obtain an error type for the serializer. */
    fun intoInvalidSerdeValue(): SerdeError = SerdeError.custom(fmt())

    companion object {
        fun from(error: io.github.kotlinmania.time.error.ComponentRange): Format =
            ComponentRange(error)

        fun from(error: Throwable): Format = StdIo(error)

        fun from(error: Format): StdError = StdError.Format(error)

        fun tryFrom(error: Format): Result<io.github.kotlinmania.time.error.ComponentRange> =
            when (error) {
                is ComponentRange -> Result.success(error.error)
                else -> Result.failure(DifferentVariant())
            }

        fun tryFrom(error: StdError): Result<Format> =
            when (error) {
                is StdError.Format -> Result.success(error.err)
                else -> Result.failure(DifferentVariant())
            }

        fun tryThrowableFrom(error: Format): Result<Throwable> =
            when (error) {
                is StdIo -> Result.success(error.error)
                else -> Result.failure(DifferentVariant())
            }
    }
}
