// port-lint: source error/conversion_range.rs
package io.github.kotlinmania.time.error

import io.github.kotlinmania.time.StdError

/**
 * Conversion range error.
 */

/**
 * An error type indicating that a conversion failed because the target type
 * could not store the initial value.
 */
class ConversionRange : IllegalArgumentException("Source value is out of range for the target type") {
    override fun equals(other: Any?): Boolean = other is ConversionRange

    override fun hashCode(): Int = ConversionRange::class.hashCode()

    fun fmt(): String = message ?: "Source value is out of range for the target type"

    companion object {
        fun from(error: ConversionRange): StdError = StdError.ConversionRange(error)

        fun tryFrom(error: StdError): Result<ConversionRange> =
            when (error) {
                is StdError.ConversionRange -> Result.success(error.err)
                else -> Result.failure(DifferentVariant())
            }
    }
}
