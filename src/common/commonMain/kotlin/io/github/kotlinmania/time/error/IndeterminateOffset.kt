// port-lint: source error/indeterminate_offset.rs
package io.github.kotlinmania.time.error

/**
 * Indeterminate offset.
 */

/** The system's UTC offset could not be determined at the given datetime. */
object IndeterminateOffset : IllegalArgumentException("The system's UTC offset could not be determined") {
    operator fun invoke(): IndeterminateOffset = this

    override fun equals(other: Any?): Boolean = other is IndeterminateOffset

    override fun hashCode(): Int = IndeterminateOffset::class.hashCode()

    fun fmt(): String = message ?: "The system's UTC offset could not be determined"

    fun from(error: IndeterminateOffset): StdError = StdError.IndeterminateOffset(error)

    fun tryFrom(error: StdError): Result<IndeterminateOffset> =
        when (error) {
            is StdError.IndeterminateOffset -> Result.success(error.err)
            else -> Result.failure(DifferentVariant())
        }
}
