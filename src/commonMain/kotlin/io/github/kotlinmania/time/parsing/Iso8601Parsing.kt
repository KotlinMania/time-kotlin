// port-lint: source parsing/iso8601.rs
package io.github.kotlinmania.time.parsing

import io.github.kotlinmania.time.error.Parse
import io.github.kotlinmania.time.error.ParseFromDescription
import io.github.kotlinmania.time.formatdescription.wellknown.Iso8601
import io.github.kotlinmania.time.parsing.combinator.Sign
import io.github.kotlinmania.time.parsing.combinator.asciiChar
import io.github.kotlinmania.time.parsing.combinator.rfc.ExtendedKind
import io.github.kotlinmania.time.parsing.combinator.rfc.ExtendedKindHolder
import io.github.kotlinmania.time.parsing.combinator.rfc.day
import io.github.kotlinmania.time.parsing.combinator.rfc.dayk
import io.github.kotlinmania.time.parsing.combinator.rfc.dayo
import io.github.kotlinmania.time.parsing.combinator.rfc.float
import io.github.kotlinmania.time.parsing.combinator.rfc.hour
import io.github.kotlinmania.time.parsing.combinator.rfc.min
import io.github.kotlinmania.time.parsing.combinator.rfc.month
import io.github.kotlinmania.time.parsing.combinator.rfc.week
import io.github.kotlinmania.time.parsing.combinator.rfc.year
import io.github.kotlinmania.time.parsing.combinator.sign
import kotlin.math.round

/** Parse a date in the basic or extended ISO 8601 format. */
internal fun Iso8601.parseDate(
    parsed: Parsed,
    extendedKind: ExtendedKindHolder,
    initialInput: ByteArray,
): Result<ByteArray> {
    val yearItem = year(initialInput)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("year")))
    var input = yearItem.remaining
    val parsedYear = yearItem.value

    val dashAfterYear = asciiChar('-'.code.toByte(), input)
    if (dashAfterYear != null) {
        input = dashAfterYear.remaining
        extendedKind.value = ExtendedKind.Extended
    } else {
        extendedKind.value = ExtendedKind.Basic
    }

    // Try year-month-day
    var retError: ParseFromDescription
    val monthItem = month(input)
    if (monthItem != null) {
        var rem = monthItem.remaining
        val m = monthItem.value
        val dashOk = if (extendedKind.isExtended()) {
            val d = asciiChar('-'.code.toByte(), rem)
            if (d != null) {
                rem = d.remaining
                true
            } else {
                false
            }
        } else {
            true
        }
        if (!dashOk) {
            retError = ParseFromDescription.InvalidLiteral
        } else {
            val dayItem = day(rem)
            if (dayItem == null) {
                retError = ParseFromDescription.InvalidComponent("day")
            } else {
                val withY = parsed.withYear(parsedYear)
                    ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("year")))
                val withM = withY.withMonth(m)
                    ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("month")))
                val withD = withM.withDay(dayItem.value)
                    ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("day")))
                parsed.copyFrom(withD)
                return Result.success(dayItem.remaining)
            }
        }
    } else {
        retError = ParseFromDescription.InvalidComponent("month")
    }

    // Try year-ordinal
    val ordinalItem = dayo(input)
    if (ordinalItem != null) {
        val withY = parsed.withYear(parsedYear)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("year")))
        val withO = withY.withOrdinal(ordinalItem.value)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("ordinal")))
        parsed.copyFrom(withO)
        return Result.success(ordinalItem.remaining)
    }

    // Try year-W-week-weekday
    val wChar = asciiChar('W'.code.toByte(), input)
    if (wChar != null) {
        val weekItem = week(wChar.remaining)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("week")))
        var rem = weekItem.remaining
        if (extendedKind.isExtended()) {
            val d = asciiChar('-'.code.toByte(), rem)
                ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
            rem = d.remaining
        }
        val weekdayItem = dayk(rem)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("weekday")))
        val withIsoY = parsed.withIsoYear(parsedYear)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("year")))
        val withIsoW = withIsoY.withIsoWeekNumber(weekItem.value)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("week")))
        val withWd = withIsoW.withWeekday(weekdayItem.value)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("weekday")))
        parsed.copyFrom(withWd)
        return Result.success(weekdayItem.remaining)
    }

    return Result.failure(Parse.FromDescription(retError))
}

/** Parse a time in the basic or extended ISO 8601 format. */
internal fun Iso8601.parseTime(
    parsed: Parsed,
    extendedKind: ExtendedKindHolder,
    dateIsPresent: Boolean,
    initialInput: ByteArray,
): Result<ByteArray> {
    var input = initialInput
    if (dateIsPresent) {
        val tChar = asciiChar('T'.code.toByte(), input)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
        input = tChar.remaining
    }

    val hourFloat = float(input)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("hour")))
    input = hourFloat.remaining
    val (hourVal, hourFrac) = hourFloat.value
    if (hourFrac == null) {
        parsed.setHour24(hourVal)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("hour")))
    } else {
        val withH = parsed.withHour24(hourVal)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("hour")))
        val minFromFrac = (hourFrac * 60.0).toInt().toUByte()
        val secFromFrac = ((hourFrac * 3600.0) % 60.0).toInt().toUByte()
        val subsecFromFrac = ((hourFrac * 3_600_000_000_000.0) % 1_000_000_000.0).toLong().toUInt()
        val withM = withH.withMinute(minFromFrac)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("minute")))
        val withS = withM.withSecond(secFromFrac)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("second")))
        val withSub = withS.withSubsecond(subsecFromFrac)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("subsecond")))
        parsed.copyFrom(withSub)
        return Result.success(input)
    }

    val colonAfterHour = asciiChar(':'.code.toByte(), input)
    if (colonAfterHour != null) {
        extendedKind.coerceExtended()
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("minute")))
        input = colonAfterHour.remaining
    }

    val minFloat = float(input)
    if (minFloat != null) {
        val (minVal, minFrac) = minFloat.value
        if (minFrac == null) {
            extendedKind.coerceBasic()
            parsed.setMinute(minVal)
                ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("minute")))
            input = minFloat.remaining
        } else {
            extendedKind.coerceBasic()
            val secFromFrac = (minFrac * 60.0).toInt().toUByte()
            val subsecFromFrac = ((minFrac * 60_000_000_000.0) % 1_000_000_000.0).toLong().toUInt()
            val withM = parsed.withMinute(minVal)
                ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("minute")))
            val withS = withM.withSecond(secFromFrac)
                ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("second")))
            val withSub = withS.withSubsecond(subsecFromFrac)
                ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("subsecond")))
            parsed.copyFrom(withSub)
            return Result.success(minFloat.remaining)
        }
    } else if (extendedKind.isExtended()) {
        return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("minute")))
    } else {
        val withM = parsed.withMinute(0u)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("minute")))
        val withS = withM.withSecond(0u)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("second")))
        val withSub = withS.withSubsecond(0u)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("subsecond")))
        parsed.copyFrom(withSub)
        return Result.success(input)
    }

    if (extendedKind.isExtended()) {
        val colonAfterMin = asciiChar(':'.code.toByte(), input)
        if (colonAfterMin != null) {
            input = colonAfterMin.remaining
        } else {
            val withS = parsed.withSecond(0u)
                ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("second")))
            val withSub = withS.withSubsecond(0u)
                ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("subsecond")))
            parsed.copyFrom(withSub)
            return Result.success(input)
        }
    }

    val secFloat = float(input)
    val (afterSec, secondVal, subsecondVal) = if (secFloat != null) {
        val (sec, secFrac) = secFloat.value
        if (secFrac == null) {
            Triple(secFloat.remaining, sec, 0u)
        } else {
            Triple(
                secFloat.remaining,
                sec,
                round(secFrac * 1_000_000_000.0).toLong().toUInt(),
            )
        }
    } else if (extendedKind.isExtended()) {
        return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("second")))
    } else {
        Triple(input, 0.toUByte(), 0u)
    }

    val withS = parsed.withSecond(secondVal)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("second")))
    val withSub = withS.withSubsecond(subsecondVal)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("subsecond")))
    parsed.copyFrom(withSub)

    return Result.success(afterSec)
}

/** Parse a UTC offset in the basic or extended ISO 8601 format. */
internal fun Iso8601.parseOffset(
    parsed: Parsed,
    extendedKind: ExtendedKindHolder,
    initialInput: ByteArray,
): Result<ByteArray> {
    val zChar = asciiChar('Z'.code.toByte(), initialInput)
    if (zChar != null) {
        val withH = parsed.withOffsetHour(0)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset hour")))
        val withM = withH.withOffsetMinuteSigned(0)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset minute")))
        val withS = withM.withOffsetSecondSigned(0)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset second")))
        parsed.copyFrom(withS)
        return Result.success(zChar.remaining)
    }

    val signItem = sign(initialInput)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset hour")))
    val parsedSign = signItem.value
    val hourItem = hour(signItem.remaining)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset hour")))
    val signedHour = when (parsedSign) {
        Sign.Negative -> (-hourItem.value.toInt()).toByte()
        Sign.Positive -> hourItem.value.toByte()
    }
    parsed.setOffsetHour(signedHour)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset hour")))
    var input = hourItem.remaining

    if (extendedKind.maybeExtended()) {
        val colon = asciiChar(':'.code.toByte(), input)
        if (colon != null) {
            extendedKind.coerceExtended()
                ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset minute")))
            input = colon.remaining
        }
    }

    val minItem = min(input)
    if (minItem != null) {
        input = minItem.remaining
        val signedMin = when (parsedSign) {
            Sign.Negative -> (-minItem.value.toInt()).toByte()
            Sign.Positive -> minItem.value.toByte()
        }
        parsed.setOffsetMinuteSigned(signedMin)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset minute")))
    } else {
        parsed.setOffsetMinuteSigned(0)
    }

    extendedKind.coerceBasic()
    return Result.success(input)
}
