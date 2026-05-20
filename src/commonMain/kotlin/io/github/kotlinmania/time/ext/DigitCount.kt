// port-lint: source ext/digit_count.rs
package io.github.kotlinmania.time.ext

/**
 * Indicates the formatted width of the value can be determined.
 *
 * This should not be implemented for signed integers. This forces the caller
 * to write the sign if desired.
 */
internal fun UByte.numDigits(): Int = toUInt().numDigits()

/** The number of digits in the stringified value. */
internal fun UShort.numDigits(): Int = toUInt().numDigits()

/** The number of digits in the stringified value. */
internal fun UInt.numDigits(): Int {
    var value = this
    var digits = 1
    while (value >= 10u) {
        value /= 10u
        digits += 1
    }
    return digits
}
