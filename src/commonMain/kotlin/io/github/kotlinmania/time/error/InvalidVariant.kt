// port-lint: source error/invalid_variant.rs
package io.github.kotlinmania.time.error

/**
 * Invalid variant error.
 */

/**
 * An error type indicating that a string parsing call failed because the value
 * was not a valid variant.
 */
class InvalidVariant : IllegalArgumentException("value was not a valid variant") {
    override fun equals(other: Any?): Boolean = other is InvalidVariant

    override fun hashCode(): Int = InvalidVariant::class.hashCode()

    fun fmt(): String = message ?: "value was not a valid variant"
}
