// port-lint: source error/different_variant.rs
package io.github.kotlinmania.time.error

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
}
