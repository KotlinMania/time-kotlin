// port-lint: source error/different_variant.rs
package io.github.kotlinmania.time.error

/**
 * Different variant error.
 */

/**
 * An error type indicating that a conversion call failed because the original
 * value was of a different variant.
 */
object DifferentVariant : IllegalArgumentException("value was of a different variant than required") {
    operator fun invoke(): DifferentVariant = this

    override fun equals(other: Any?): Boolean = other is DifferentVariant

    override fun hashCode(): Int = DifferentVariant::class.hashCode()

    fun fmt(): String = message ?: "value was of a different variant than required"

    fun from(error: DifferentVariant): StdError = StdError.DifferentVariant(error)

    fun tryFrom(error: StdError): Result<DifferentVariant> =
        when (error) {
            is StdError.DifferentVariant -> Result.success(error.err)
            else -> Result.failure(DifferentVariant())
        }
}

