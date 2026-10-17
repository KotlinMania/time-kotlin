// port-lint: source parsing/shim.rs
package io.github.kotlinmania.time.parsing

/** Trait for integer types parsed from ASCII digits. */
internal interface ParsedInteger<T> {
    /** The maximum number of digits that this type can have. */
    val maxNumDigits: Int

    /** The zero value for this type. */
    val zero: T

    /** Push a digit onto the end of this integer, assuming no overflow. */
    fun pushDigit(current: T, digit: UByte): T

    /** Push a digit onto the end of this integer, returning `null` on overflow. */
    fun checkedPushDigit(current: T, digit: UByte): T?
}

internal object UByteInteger : ParsedInteger<UByte> {
    override val maxNumDigits: Int = 3
    override val zero: UByte = 0u

    override fun pushDigit(current: UByte, digit: UByte): UByte =
        ((current.toUInt() * 10u) + digit.toUInt()).toUByte()

    override fun checkedPushDigit(current: UByte, digit: UByte): UByte? {
        val next = (current.toUInt() * 10u) + digit.toUInt()
        return if (next <= UByte.MAX_VALUE.toUInt()) next.toUByte() else null
    }
}

internal object UShortInteger : ParsedInteger<UShort> {
    override val maxNumDigits: Int = 5
    override val zero: UShort = 0u

    override fun pushDigit(current: UShort, digit: UByte): UShort =
        ((current.toUInt() * 10u) + digit.toUInt()).toUShort()

    override fun checkedPushDigit(current: UShort, digit: UByte): UShort? {
        val next = (current.toUInt() * 10u) + digit.toUInt()
        return if (next <= UShort.MAX_VALUE.toUInt()) next.toUShort() else null
    }
}

internal object UIntInteger : ParsedInteger<UInt> {
    override val maxNumDigits: Int = 10
    override val zero: UInt = 0u

    override fun pushDigit(current: UInt, digit: UByte): UInt =
        (current * 10u) + digit.toUInt()

    override fun checkedPushDigit(current: UInt, digit: UByte): UInt? {
        val next = (current.toULong() * 10uL) + digit.toULong()
        return if (next <= UInt.MAX_VALUE.toULong()) next.toUInt() else null
    }
}
