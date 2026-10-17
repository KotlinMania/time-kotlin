// port-lint: source parsing/combinator/rfc/iso8601.rs
package io.github.kotlinmania.time.parsing.combinator.rfc

import io.github.kotlinmania.time.Month
import io.github.kotlinmania.time.Weekday
import io.github.kotlinmania.time.parsing.ParsedItem
import io.github.kotlinmania.time.parsing.combinator.ExactlyNDigits
import io.github.kotlinmania.time.parsing.combinator.Sign
import io.github.kotlinmania.time.parsing.combinator.anyDigit
import io.github.kotlinmania.time.parsing.combinator.sign

/**
 * What kind of format is being parsed. This is used to ensure each part of the format (date, time,
 * offset) is the same kind.
 */
internal enum class ExtendedKind {
    /** The basic format. */
    Basic,

    /** The extended format. */
    Extended,

    /** Unknown thus far. */
    Unknown,
    ;

    /** Is it possible that the format is extended? */
    fun maybeExtended(): Boolean = this == Extended || this == Unknown

    /** Is the format known for certain to be extended? */
    fun isExtended(): Boolean = this == Extended
}

internal class ExtendedKindHolder(var value: ExtendedKind = ExtendedKind.Unknown) {
    fun maybeExtended(): Boolean = value.maybeExtended()

    fun isExtended(): Boolean = value.isExtended()

    fun coerceBasic(): Unit? = when (value) {
        ExtendedKind.Basic -> Unit
        ExtendedKind.Extended -> null
        ExtendedKind.Unknown -> {
            value = ExtendedKind.Basic
            Unit
        }
    }

    fun coerceExtended(): Unit? = when (value) {
        ExtendedKind.Basic -> null
        ExtendedKind.Extended -> Unit
        ExtendedKind.Unknown -> {
            value = ExtendedKind.Extended
            Unit
        }
    }
}

/** Parse a possibly expanded year. */
internal fun year(input: ByteArray): ParsedItem<Int>? {
    val parsedSign = sign(input)
    return if (parsedSign != null) {
        ExactlyNDigits.parse6(parsedSign.remaining)?.map { v ->
            val signed = v.toInt()
            when (parsedSign.value) {
                Sign.Negative -> -signed
                Sign.Positive -> signed
            }
        }
    } else {
        ExactlyNDigits.parse4(input)?.map { it.toShort().toInt() }
    }
}

/** Parse a month. */
internal fun month(input: ByteArray): ParsedItem<Month>? {
    if (input.size < 2) return null
    val rest = input.copyOfRange(2, input.size)
    val c0 = input[0].toInt().toChar()
    val c1 = input[1].toInt().toChar()
    val m = when ("$c0$c1") {
        "01" -> Month.January
        "02" -> Month.February
        "03" -> Month.March
        "04" -> Month.April
        "05" -> Month.May
        "06" -> Month.June
        "07" -> Month.July
        "08" -> Month.August
        "09" -> Month.September
        "10" -> Month.October
        "11" -> Month.November
        "12" -> Month.December
        else -> return null
    }
    return ParsedItem(rest, m)
}

/** Parse a week number (non-zero 1..99). */
internal fun week(input: ByteArray): ParsedItem<UByte>? =
    ExactlyNDigits.parse2(input)?.flatMap { if (it > 0u) it else null }

/** Parse a day of the month (non-zero 1..99). */
internal fun day(input: ByteArray): ParsedItem<UByte>? =
    ExactlyNDigits.parse2(input)?.flatMap { if (it > 0u) it else null }

/** Parse a day of the week. */
internal fun dayk(input: ByteArray): ParsedItem<Weekday>? {
    if (input.isEmpty()) return null
    val rest = input.copyOfRange(1, input.size)
    val w = when (input[0].toInt().toChar()) {
        '1' -> Weekday.Monday
        '2' -> Weekday.Tuesday
        '3' -> Weekday.Wednesday
        '4' -> Weekday.Thursday
        '5' -> Weekday.Friday
        '6' -> Weekday.Saturday
        '7' -> Weekday.Sunday
        else -> return null
    }
    return ParsedItem(rest, w)
}

/** Parse a day of the year (non-zero 1..999). */
internal fun dayo(input: ByteArray): ParsedItem<UShort>? =
    ExactlyNDigits.parse3(input)?.flatMap { if (it > 0u) it else null }

/** Parse the hour. */
internal fun hour(input: ByteArray): ParsedItem<UByte>? =
    ExactlyNDigits.parse2(input)

/** Parse the minute. */
internal fun min(input: ByteArray): ParsedItem<UByte>? =
    ExactlyNDigits.parse2(input)

/**
 * Parse a floating point number as its integer and optional fractional parts.
 *
 * The number must have two digits before the decimal point. If a decimal point is present, at
 * least one digit must follow.
 */
internal fun float(input: ByteArray): ParsedItem<Pair<UByte, Double?>>? {
    val intPartItem = ExactlyNDigits.parse2(input) ?: return null
    val integerPart = intPartItem.value
    val afterInt = intPartItem.remaining

    val decSign = decimalSign(afterInt)
    return if (decSign != null) {
        val firstFrac = anyDigit(decSign.remaining) ?: return null
        var rem = firstFrac.remaining
        var fractionalPart = (firstFrac.value - '0'.code.toUByte()).toInt().toDouble() / 10.0

        var divisor = 10.0
        while (true) {
            val nextDigit = anyDigit(rem) ?: break
            rem = nextDigit.remaining
            divisor *= 10.0
            fractionalPart += (nextDigit.value - '0'.code.toUByte()).toInt().toDouble() / divisor
        }

        ParsedItem(rem, integerPart to fractionalPart)
    } else {
        ParsedItem(afterInt, integerPart to null)
    }
}

/** Parse a "decimal sign", which is either a comma or a period. */
private fun decimalSign(input: ByteArray): ParsedItem<Unit>? {
    if (input.isEmpty()) return null
    val b = input[0]
    return if (b == '.'.code.toByte() || b == ','.code.toByte()) {
        ParsedItem(input.copyOfRange(1, input.size), Unit)
    } else {
        null
    }
}
