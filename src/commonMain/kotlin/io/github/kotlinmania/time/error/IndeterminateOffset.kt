// port-lint: source error/indeterminate_offset.rs
package io.github.kotlinmania.time.error

import io.github.kotlinmania.time.Error

/**
 * Indeterminate offset.
 */

/** The system's UTC offset could not be determined at the given datetime. */
class IndeterminateOffset : IllegalArgumentException("The system's UTC offset could not be determined") {
    override fun equals(other: Any?): Boolean = other is IndeterminateOffset

    override fun hashCode(): Int = IndeterminateOffset::class.hashCode()

    fun fmt(): String = message ?: "The system's UTC offset could not be determined"

    companion object {
        fun from(error: IndeterminateOffset): Error = Error.IndeterminateOffset(error)

        fun tryFrom(error: Error): Result<IndeterminateOffset> =
            when (error) {
                is Error.IndeterminateOffset -> Result.success(error.err)
                else -> Result.failure(DifferentVariant())
            }
    }
}
