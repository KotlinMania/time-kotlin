// port-lint: source error/different_variant.rs
package io.github.kotlinmania.time.error

import io.github.kotlinmania.time.StdError

/**
 * Different variant error.
 */

/**
 * An error type indicating that a conversion call failed because the original
 * value was of a different variant.
 */
class DifferentVariant : IllegalArgumentException("value was of a different variant than required") {
    override fun equals(other: Any?): Boolean = other is DifferentVariant

    override fun hashCode(): Int = DifferentVariant::class.hashCode()

    fun fmt(): String = message ?: "value was of a different variant than required"

    companion object {
        fun from(error: DifferentVariant): StdError = StdError.DifferentVariant(error)

        fun tryFrom(error: StdError): Result<DifferentVariant> =
            when (error) {
                is StdError.DifferentVariant -> Result.success(error.err)
                else -> Result.failure(DifferentVariant())
            }
    }
}
