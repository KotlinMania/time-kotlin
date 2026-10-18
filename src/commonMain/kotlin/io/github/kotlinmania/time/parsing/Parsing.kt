package io.github.kotlinmania.time.parsing

/** An item that has been parsed. Represented as a `(remaining, value)` pair. */
internal class ParsedItem<out T>(
    val remaining: ByteArray,
    val value: T,
) {
    operator fun component1(): ByteArray = remaining

    operator fun component2(): T = value

    /** Map the value to a new value, preserving the remaining input. */
    inline fun <U> map(f: (T) -> U): ParsedItem<U> = ParsedItem(remaining, f(value))

    /** Map the value to a new, optional value, preserving the remaining input. */
    inline fun <U : Any> flatMap(f: (T) -> U?): ParsedItem<U>? {
        val mapped = f(value) ?: return null
        return ParsedItem(remaining, mapped)
    }

    /** Consume the stored value with the provided function. The remaining input is returned. */
    inline fun consumeValue(f: (T) -> Unit?): ByteArray? {
        f(value) ?: return null
        return remaining
    }

    /** Discard the stored value, returning the remaining input. */
    fun discardValue(): ByteArray = remaining

    /**
     * Filter the value with the provided function. If the function returns `false`, the value
     * is discarded and `null` is returned. Otherwise, the value is preserved and `this` is
     * returned.
     */
    inline fun filter(f: (T) -> Boolean): ParsedItem<T>? = if (f(value)) this else null

    /** Discard the unit value, returning the remaining input. */
    fun intoInner(): ByteArray = remaining
}
