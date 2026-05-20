// port-lint: source error/invalid_format_description.rs
package io.github.kotlinmania.time.error

import io.github.kotlinmania.time.Error

/**
 * Invalid format description.
 */

/** The format description provided was not valid. */
sealed class InvalidFormatDescription(message: String) : IllegalArgumentException(message) {
    abstract fun fmt(): String

    class UnclosedOpeningBracket(
        /** The zero-based index of the opening bracket. */
        val index: Int,
    ) : InvalidFormatDescription("unclosed opening bracket at byte index $index") {
        override fun fmt(): String = message ?: "unclosed opening bracket at byte index $index"
    }

    class InvalidComponentName(
        /** The name of the invalid component name. */
        val name: String,
        /** The zero-based index the component name starts at. */
        val index: Int,
    ) : InvalidFormatDescription("invalid component name `$name` at byte index $index") {
        override fun fmt(): String = message ?: "invalid component name `$name` at byte index $index"
    }

    class InvalidModifier(
        /** The value of the invalid modifier. */
        val value: String,
        /** The zero-based index the modifier starts at. */
        val index: Int,
    ) : InvalidFormatDescription("invalid modifier `$value` at byte index $index") {
        override fun fmt(): String = message ?: "invalid modifier `$value` at byte index $index"
    }

    class MissingComponentName(
        /** The zero-based index where the component name should start. */
        val index: Int,
    ) : InvalidFormatDescription("missing component name at byte index $index") {
        override fun fmt(): String = message ?: "missing component name at byte index $index"
    }

    class MissingRequiredModifier(
        /** The name of the modifier that is missing. */
        val name: String,
        /** The zero-based index of the component. */
        val index: Int,
    ) : InvalidFormatDescription(
        "missing required modifier `$name` for component at byte index $index",
    ) {
        override fun fmt(): String =
            message ?: "missing required modifier `$name` for component at byte index $index"
    }

    class Expected(
        /** What was expected to be present, but was not. */
        val what: String,
        /** The zero-based index the item was expected to be found at. */
        val index: Int,
    ) : InvalidFormatDescription("expected $what at byte index $index") {
        override fun fmt(): String = message ?: "expected $what at byte index $index"
    }

    class NotSupported(
        /** The behavior that is not supported. */
        val what: String,
        /** The context in which the behavior is not supported. */
        val context: String,
        /** The zero-based index the error occurred at. */
        val index: Int,
    ) : InvalidFormatDescription(formatMessage(what, context, index)) {
        override fun fmt(): String = message ?: formatMessage(what, context, index)
    }

    companion object {
        fun from(error: InvalidFormatDescription): Error = Error.InvalidFormatDescription(error)

        fun tryFrom(error: Error): Result<InvalidFormatDescription> =
            when (error) {
                is Error.InvalidFormatDescription -> Result.success(error.err)
                else -> Result.failure(DifferentVariant())
            }

        private fun formatMessage(
            what: String,
            context: String,
            index: Int,
        ): String =
            if (context.isEmpty()) {
                "$what is not supported at byte index $index"
            } else {
                "$what is not supported in $context at byte index $index"
            }
    }
}
