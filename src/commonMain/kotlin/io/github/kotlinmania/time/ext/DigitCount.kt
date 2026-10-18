// port-lint: source ext/digit_count.rs
package io.github.kotlinmania.time.ext

/**
 * Indicates the formatted width of the value can be determined.
 *
 * This should not be implemented for signed integers. This forces the caller
 * to write the sign if desired.
 */
internal interface DigitCount {
    /** The number of digits in the stringified value. */
    fun numDigits(): UByte
}

/** The number of digits in the stringified value. */
internal fun UByte.numDigits(): UByte = digitCount(toUInt())

/** The number of digits in the stringified value. */
internal fun UShort.numDigits(): UByte = digitCount(toUInt())

/** The number of digits in the stringified value. */
internal fun UInt.numDigits(): UByte = digitCount(this)

private fun digitCount(input: UInt): UByte {
    var value = input
    var digits: UByte = 1u
    while (value >= 10u) {
        value /= 10u
        digits = (digits + 1u).toUByte()
    }
    return digits
}
