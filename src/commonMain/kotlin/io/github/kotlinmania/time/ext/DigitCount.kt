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
    fun numDigits(): Int
}

/** The number of digits in the stringified value. */
internal fun UByte.numDigits(): Int = digitCount(toUInt())

/** The number of digits in the stringified value. */
internal fun UShort.numDigits(): Int = digitCount(toUInt())

/** The number of digits in the stringified value. */
internal fun UInt.numDigits(): Int = digitCount(this)

private fun digitCount(input: UInt): Int {
    var value = input
    var digits = 1
    while (value >= 10u) {
        value /= 10u
        digits += 1
    }
    return digits
}
