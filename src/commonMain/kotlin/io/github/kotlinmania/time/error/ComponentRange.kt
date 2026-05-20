// port-lint: source error/component_range.rs
package io.github.kotlinmania.time.error

/**
 * Component range error.
 */

/**
 * An error type indicating that a component provided to a method was out of
 * range, causing a failure.
 *
 * `Long` is the narrowest type fitting all use cases. This eliminates the need
 * for a type parameter.
 */
class ComponentRange internal constructor(
    /** Name of the component. */
    private val componentName: String,
    /**
     * Whether an input with the same value could have succeeded if the values
     * of other components were different.
     */
    private val conditional: Boolean,
) : IllegalArgumentException("$componentName was not in range") {
    override fun equals(other: Any?): Boolean =
        other is ComponentRange &&
            other.componentName == componentName &&
            other.conditional == conditional

    override fun hashCode(): Int = 31 * componentName.hashCode() + conditional.hashCode()

    /** Obtain the name of the component whose value was out of range. */
    fun name(): String = componentName

    /**
     * Whether the value's permitted range is conditional, i.e. whether an input
     * with this value could have succeeded if the values of other components
     * were different.
     */
    fun isConditional(): Boolean = conditional

    fun fmt(): String = message ?: "$componentName was not in range"

    companion object {
        /** Create a new `ComponentRange` error that is not conditional. */
        fun unconditional(name: String): ComponentRange = ComponentRange(name, false)

        /** Create a new `ComponentRange` error that is conditional. */
        fun conditional(name: String): ComponentRange = ComponentRange(name, true)
    }
}
