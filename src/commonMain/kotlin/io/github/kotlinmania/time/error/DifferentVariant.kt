// port-lint: source error/different_variant.rs
package io.github.kotlinmania.time.error

import io.github.kotlinmania.time.Error

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
        fun from(error: DifferentVariant): Error = Error.DifferentVariant(error)

        fun tryFrom(error: Error): Result<DifferentVariant> =
            when (error) {
                is Error.DifferentVariant -> Result.success(error.err)
                else -> Result.failure(DifferentVariant())
            }
    }
}
