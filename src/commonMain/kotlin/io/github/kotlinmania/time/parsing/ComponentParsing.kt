// port-lint: source parsing/component.rs
package io.github.kotlinmania.time.parsing

import io.github.kotlinmania.time.Month
import io.github.kotlinmania.time.Weekday
import io.github.kotlinmania.time.formatdescription.PeriodCase
import io.github.kotlinmania.time.formatdescription.modifier.Day
import io.github.kotlinmania.time.formatdescription.modifier.End
import io.github.kotlinmania.time.formatdescription.modifier.Hour
import io.github.kotlinmania.time.formatdescription.modifier.Ignore
import io.github.kotlinmania.time.formatdescription.modifier.Minute
import io.github.kotlinmania.time.formatdescription.modifier.MonthRepr
import io.github.kotlinmania.time.formatdescription.modifier.OffsetHour
import io.github.kotlinmania.time.formatdescription.modifier.OffsetMinute
import io.github.kotlinmania.time.formatdescription.modifier.OffsetSecond
import io.github.kotlinmania.time.formatdescription.modifier.Ordinal
import io.github.kotlinmania.time.formatdescription.modifier.Period
import io.github.kotlinmania.time.formatdescription.modifier.Second
import io.github.kotlinmania.time.formatdescription.modifier.Subsecond
import io.github.kotlinmania.time.formatdescription.modifier.SubsecondDigits
import io.github.kotlinmania.time.formatdescription.modifier.TrailingInput
import io.github.kotlinmania.time.formatdescription.modifier.UnixTimestamp
import io.github.kotlinmania.time.formatdescription.modifier.UnixTimestampPrecision
import io.github.kotlinmania.time.formatdescription.modifier.WeekNumber
import io.github.kotlinmania.time.formatdescription.modifier.WeekdayRepr
import io.github.kotlinmania.time.formatdescription.modifier.Year
import io.github.kotlinmania.time.formatdescription.modifier.YearRange
import io.github.kotlinmania.time.formatdescription.modifier.YearRepr
import io.github.kotlinmania.time.parsing.combinator.ExactlyNDigits
import io.github.kotlinmania.time.parsing.combinator.Sign
import io.github.kotlinmania.time.parsing.combinator.anyDigit
import io.github.kotlinmania.time.parsing.combinator.exactlyNDigitsPadded
import io.github.kotlinmania.time.parsing.combinator.nToMDigitString
import io.github.kotlinmania.time.parsing.combinator.nToMDigitsPadded
import io.github.kotlinmania.time.parsing.combinator.opt
import io.github.kotlinmania.time.parsing.combinator.sign

/** Parse the "year" component of a `Date`. */
internal fun parseYear(
    input: ByteArray,
    modifiers: Year,
): ParsedItem<Pair<Int, Boolean>>? =
    when (modifiers.repr) {
        YearRepr.Full -> {
            val (afterSign, parsedSign) = opt(::sign)(input)
            if (parsedSign != null) {
                val (rem, year) = if (modifiers.range == YearRange.Extended) {
                    nToMDigitsPadded(4, 6, modifiers.padding, UIntInteger)(afterSign) ?: return null
                } else {
                    exactlyNDigitsPadded(4, modifiers.padding, UIntInteger)(afterSign) ?: return null
                }
                val signedYear = year.toInt()
                ParsedItem(
                    rem,
                    when (parsedSign) {
                        Sign.Negative -> -signedYear to true
                        Sign.Positive -> signedYear to false
                    },
                )
            } else if (modifiers.signIsMandatory) {
                null
            } else {
                val (rem, year) = exactlyNDigitsPadded(4, modifiers.padding, UIntInteger)(afterSign) ?: return null
                ParsedItem(rem, year.toInt() to false)
            }
        }
        YearRepr.Century -> {
            val (afterSign, parsedSign) = opt(::sign)(input)
            if (parsedSign != null) {
                val (rem, year) = if (modifiers.range == YearRange.Extended) {
                    nToMDigitsPadded(2, 4, modifiers.padding, UIntInteger)(afterSign) ?: return null
                } else {
                    exactlyNDigitsPadded(2, modifiers.padding, UIntInteger)(afterSign) ?: return null
                }
                val signedYear = year.toInt()
                ParsedItem(
                    rem,
                    when (parsedSign) {
                        Sign.Negative -> -signedYear to true
                        Sign.Positive -> signedYear to false
                    },
                )
            } else if (modifiers.signIsMandatory) {
                null
            } else {
                val (rem, year) = nToMDigitsPadded(1, 2, modifiers.padding, UIntInteger)(afterSign) ?: return null
                ParsedItem(rem, year.toInt() to false)
            }
        }
        YearRepr.LastTwo -> {
            exactlyNDigitsPadded(2, modifiers.padding, UIntInteger)(input)
                ?.map { v -> v.toInt() to false }
        }
    }

/** Parse the "month" component of a `Date`. */
internal fun parseMonth(
    input: ByteArray,
    modifiers: io.github.kotlinmania.time.formatdescription.modifier.Month,
): ParsedItem<Month>? =
    when (modifiers.repr) {
        MonthRepr.Numerical -> {
            exactlyNDigitsPadded(2, modifiers.padding, UByteInteger)(input)
                ?.flatMap { n -> if (n == 0.toUByte()) null else Month.fromNumber(n.toInt()).getOrNull() }
        }
        MonthRepr.Long, MonthRepr.Short -> {
            if (input.size < 3) return null
            val rest = input.copyOfRange(3, input.size)
            val prefix = if (modifiers.caseSensitive) {
                "${input[0].toInt().toChar()}${input[1].toInt().toChar()}${input[2].toInt().toChar()}"
            } else {
                val c0 = input[0].toInt().toChar().uppercaseChar()
                val c1 = input[1].toInt().toChar().lowercaseChar()
                val c2 = input[2].toInt().toChar().lowercaseChar()
                "$c0$c1$c2"
            }

            val month = when (prefix) {
                "Jan" -> Month.January
                "Feb" -> Month.February
                "Mar" -> Month.March
                "Apr" -> Month.April
                "May" -> Month.May
                "Jun" -> Month.June
                "Jul" -> Month.July
                "Aug" -> Month.August
                "Sep" -> Month.September
                "Oct" -> Month.October
                "Nov" -> Month.November
                "Dec" -> Month.December
                else -> return null
            }

            if (modifiers.repr == MonthRepr.Short) {
                return ParsedItem(rest, month)
            }

            val expectedRemaining = when (month) {
                Month.January -> "uary"
                Month.February -> "ruary"
                Month.March -> "ch"
                Month.April -> "il"
                Month.May -> ""
                Month.June -> "e"
                Month.July -> "y"
                Month.August -> "ust"
                Month.September -> "tember"
                Month.October -> "ober"
                Month.November, Month.December -> "ember"
            }

            matchRemainingAscii(rest, expectedRemaining, modifiers.caseSensitive)?.let { tail ->
                ParsedItem(tail, month)
            }
        }
    }

/** Parse the "week number" component of a `Date`. */
internal fun parseWeekNumber(
    input: ByteArray,
    modifiers: WeekNumber,
): ParsedItem<UByte>? =
    exactlyNDigitsPadded(2, modifiers.padding, UByteInteger)(input)

/** Parse the "weekday" component of a `Date`. */
internal fun parseWeekday(
    input: ByteArray,
    modifiers: io.github.kotlinmania.time.formatdescription.modifier.Weekday,
): ParsedItem<Weekday>? =
    when (modifiers.repr) {
        WeekdayRepr.Long, WeekdayRepr.Short -> {
            if (input.size < 3) return null
            val rest = input.copyOfRange(3, input.size)
            val prefix = if (modifiers.caseSensitive) {
                "${input[0].toInt().toChar()}${input[1].toInt().toChar()}${input[2].toInt().toChar()}"
            } else {
                val c0 = input[0].toInt().toChar().uppercaseChar()
                val c1 = input[1].toInt().toChar().lowercaseChar()
                val c2 = input[2].toInt().toChar().lowercaseChar()
                "$c0$c1$c2"
            }

            val weekday = when (prefix) {
                "Mon" -> Weekday.Monday
                "Tue" -> Weekday.Tuesday
                "Wed" -> Weekday.Wednesday
                "Thu" -> Weekday.Thursday
                "Fri" -> Weekday.Friday
                "Sat" -> Weekday.Saturday
                "Sun" -> Weekday.Sunday
                else -> return null
            }

            if (modifiers.repr == WeekdayRepr.Short) {
                return ParsedItem(rest, weekday)
            }

            val expectedRemaining = when (weekday) {
                Weekday.Monday, Weekday.Friday, Weekday.Sunday -> "day"
                Weekday.Tuesday -> "sday"
                Weekday.Wednesday -> "nesday"
                Weekday.Thursday -> "rsday"
                Weekday.Saturday -> "urday"
            }

            matchRemainingAscii(rest, expectedRemaining, modifiers.caseSensitive)?.let { tail ->
                ParsedItem(tail, weekday)
            }
        }
        WeekdayRepr.Sunday, WeekdayRepr.Monday -> {
            if (input.isEmpty()) return null
            val rawDigit = input[0].toUByte()
            val oneOffset = if (modifiers.oneIndexed) 1u else 0u
            val sub = (rawDigit.toUInt() - '0'.code.toUInt() - oneOffset)
            if (sub > 6u) return null
            var digit = sub.toInt()
            if (modifiers.repr == WeekdayRepr.Sunday) {
                digit = (digit + 6) % 7
            }
            val weekday = Weekday.entries[digit]
            ParsedItem(input.copyOfRange(1, input.size), weekday)
        }
    }

private fun matchRemainingAscii(
    rest: ByteArray,
    expected: String,
    caseSensitive: Boolean,
): ByteArray? {
    if (rest.size < expected.length) return null
    for (i in expected.indices) {
        val actualChar = rest[i].toInt().toChar()
        val expChar = expected[i]
        val matches = if (caseSensitive) {
            actualChar == expChar
        } else {
            actualChar.lowercaseChar() == expChar.lowercaseChar()
        }
        if (!matches) return null
    }
    return rest.copyOfRange(expected.length, rest.size)
}

/** Parse the "ordinal" component of a `Date`. */
internal fun parseOrdinal(
    input: ByteArray,
    modifiers: Ordinal,
): ParsedItem<UShort>? =
    exactlyNDigitsPadded(3, modifiers.padding, UShortInteger)(input)
        ?.flatMap { if (it > 0u) it else null }

/** Parse the "day" component of a `Date`. */
internal fun parseDay(
    input: ByteArray,
    modifiers: Day,
): ParsedItem<UByte>? =
    exactlyNDigitsPadded(2, modifiers.padding, UByteInteger)(input)
        ?.flatMap { if (it > 0u) it else null }

/** Parse the "hour" component of a `Time`. */
internal fun parseHour(
    input: ByteArray,
    modifiers: Hour,
): ParsedItem<UByte>? =
    exactlyNDigitsPadded(2, modifiers.padding, UByteInteger)(input)

/** Parse the "minute" component of a `Time`. */
internal fun parseMinute(
    input: ByteArray,
    modifiers: Minute,
): ParsedItem<UByte>? =
    exactlyNDigitsPadded(2, modifiers.padding, UByteInteger)(input)

/** Parse the "second" component of a `Time`. */
internal fun parseSecond(
    input: ByteArray,
    modifiers: Second,
): ParsedItem<UByte>? =
    exactlyNDigitsPadded(2, modifiers.padding, UByteInteger)(input)

/** Parse the "period" component of a `Time`. Required if the hour is on a 12-hour clock. */
internal fun parsePeriod(
    input: ByteArray,
    modifiers: Period,
): ParsedItem<PeriodCase>? {
    if (input.size < 2) return null
    val rest = input.copyOfRange(2, input.size)
    val c0 = input[0].toInt().toChar()
    val c1 = input[1].toInt().toChar()

    return if (modifiers.isUppercase && modifiers.caseSensitive) {
        when ("$c0$c1") {
            "AM" -> ParsedItem(rest, PeriodCase.Am)
            "PM" -> ParsedItem(rest, PeriodCase.Pm)
            else -> null
        }
    } else if (!modifiers.isUppercase && modifiers.caseSensitive) {
        when ("$c0$c1") {
            "am" -> ParsedItem(rest, PeriodCase.Am)
            "pm" -> ParsedItem(rest, PeriodCase.Pm)
            else -> null
        }
    } else {
        when ("${c0.lowercaseChar()}${c1.lowercaseChar()}") {
            "am" -> ParsedItem(rest, PeriodCase.Am)
            "pm" -> ParsedItem(rest, PeriodCase.Pm)
            else -> null
        }
    }
}

/** Parse the "subsecond" component of a `Time`. */
internal fun parseSubsecond(
    input: ByteArray,
    modifiers: Subsecond,
): ParsedItem<UInt>? =
    when (modifiers.digits) {
        SubsecondDigits.One -> ExactlyNDigits.parse1(input)?.map { it.toUInt() * 100_000_000u }
        SubsecondDigits.Two -> ExactlyNDigits.parse2(input)?.map { it.toUInt() * 10_000_000u }
        SubsecondDigits.Three -> ExactlyNDigits.parse3(input)?.map { it.toUInt() * 1_000_000u }
        SubsecondDigits.Four -> ExactlyNDigits.parse4(input)?.map { it.toUInt() * 100_000u }
        SubsecondDigits.Five -> ExactlyNDigits.parse5(input)?.map { it * 10_000u }
        SubsecondDigits.Six -> ExactlyNDigits.parse6(input)?.map { it * 1_000u }
        SubsecondDigits.Seven -> ExactlyNDigits.parse7(input)?.map { it * 100u }
        SubsecondDigits.Eight -> ExactlyNDigits.parse8(input)?.map { it * 10u }
        SubsecondDigits.Nine -> ExactlyNDigits.parse9(input)
        SubsecondDigits.OneOrMore -> {
            val first = anyDigit(input) ?: return null
            var rem = first.remaining
            var value = (first.value - '0'.code.toUByte()) * 100_000_000u
            var multiplier = 10_000_000u
            while (true) {
                val next = anyDigit(rem) ?: break
                value += (next.value - '0'.code.toUByte()) * multiplier
                rem = next.remaining
                multiplier /= 10u
            }
            ParsedItem(rem, value)

        }
    }

/** Parse the "hour" component of a `UtcOffset`. */
internal fun parseOffsetHour(
    input: ByteArray,
    modifiers: OffsetHour,
): ParsedItem<Pair<Byte, Boolean>>? {
    val (afterSign, parsedSign) = opt(::sign)(input)
    val (rem, hour) = exactlyNDigitsPadded(2, modifiers.padding, UByteInteger)(afterSign) ?: return null
    return when {
        parsedSign == Sign.Negative -> ParsedItem(rem, (-hour.toInt()).toByte() to true)
        parsedSign == null && modifiers.signIsMandatory -> null
        else -> ParsedItem(rem, hour.toByte() to false)
    }
}

/** Parse the "minute" component of a `UtcOffset`. */
internal fun parseOffsetMinute(
    input: ByteArray,
    modifiers: OffsetMinute,
): ParsedItem<Byte>? =
    exactlyNDigitsPadded(2, modifiers.padding, UByteInteger)(input)
        ?.map { it.toByte() }

/** Parse the "second" component of a `UtcOffset`. */
internal fun parseOffsetSecond(
    input: ByteArray,
    modifiers: OffsetSecond,
): ParsedItem<Byte>? =
    exactlyNDigitsPadded(2, modifiers.padding, UByteInteger)(input)
        ?.map { it.toByte() }

/** Ignore the given number of bytes. */
internal fun parseIgnore(
    input: ByteArray,
    modifiers: Ignore,
): ParsedItem<Unit>? {
    val count = modifiers.count.toInt()
    if (input.size < count) return null
    return ParsedItem(input.copyOfRange(count, input.size), Unit)
}

/** Parse the Unix timestamp component as a signed decimal string of nanoseconds. */
internal fun parseUnixTimestamp(
    input: ByteArray,
    modifiers: UnixTimestamp,
): ParsedItem<String>? {
    val (afterSign, parsedSign) = opt(::sign)(input)
    if (parsedSign == null && modifiers.signIsMandatory) return null

    val (rem, rawDigits) = when (modifiers.precision) {
        UnixTimestampPrecision.Second ->
            nToMDigitString(1, 14, afterSign)?.let { it.remaining to "${it.value}000000000" }
        UnixTimestampPrecision.Millisecond ->
            nToMDigitString(1, 17, afterSign)?.let { it.remaining to "${it.value}000000" }
        UnixTimestampPrecision.Microsecond ->
            nToMDigitString(1, 20, afterSign)?.let { it.remaining to "${it.value}000" }
        UnixTimestampPrecision.Nanosecond ->
            nToMDigitString(1, 23, afterSign)?.let { it.remaining to it.value }
    } ?: return null

    val trimmed = rawDigits.trimStart('0')
    val normalized = if (trimmed.isEmpty()) {
        "0"
    } else if (parsedSign == Sign.Negative) {
        "-$trimmed"
    } else {
        trimmed
    }

    return ParsedItem(rem, normalized)
}

/** Parse the `end` component, which represents the end of input. */
internal fun parseEnd(
    input: ByteArray,
    end: End,
): ParsedItem<Unit>? =
    if (end.trailingInput == TrailingInput.Discard || input.isEmpty()) {
        ParsedItem(ByteArray(0), Unit)
    } else {
        null
    }
