// port-lint: source parsing/parsable.rs
package io.github.kotlinmania.time.parsing

import io.github.kotlinmania.time.Date
import io.github.kotlinmania.time.Month
import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.PrimitiveDateTime
import io.github.kotlinmania.time.Time
import io.github.kotlinmania.time.UtcDateTime
import io.github.kotlinmania.time.UtcOffset
import io.github.kotlinmania.time.error.ComponentRange
import io.github.kotlinmania.time.error.Parse
import io.github.kotlinmania.time.error.ParseFromDescription
import io.github.kotlinmania.time.error.TryFromParsed
import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatdescription.OwnedFormatItem
import io.github.kotlinmania.time.formatdescription.modifier.MonthRepr
import io.github.kotlinmania.time.formatdescription.modifier.Padding
import io.github.kotlinmania.time.formatdescription.modifier.WeekdayRepr
import io.github.kotlinmania.time.formatdescription.wellknown.Iso8601
import io.github.kotlinmania.time.formatdescription.wellknown.Rfc2822
import io.github.kotlinmania.time.formatdescription.wellknown.Rfc3339
import io.github.kotlinmania.time.parsing.combinator.ExactlyNDigits
import io.github.kotlinmania.time.parsing.combinator.Sign
import io.github.kotlinmania.time.parsing.combinator.anyDigit
import io.github.kotlinmania.time.parsing.combinator.asciiChar
import io.github.kotlinmania.time.parsing.combinator.asciiCharIgnoreCase
import io.github.kotlinmania.time.parsing.combinator.oneOrTwoDigits
import io.github.kotlinmania.time.parsing.combinator.opt
import io.github.kotlinmania.time.parsing.combinator.rfc.ExtendedKindHolder
import io.github.kotlinmania.time.parsing.combinator.rfc.cfws
import io.github.kotlinmania.time.parsing.combinator.rfc.fws
import io.github.kotlinmania.time.parsing.combinator.rfc.zoneLiteral
import io.github.kotlinmania.time.parsing.combinator.sign

/** A type that can be used to parse an item from an input. */
interface Parsable {
    /** Parse the item into the provided [Parsed] struct. */
    fun parseInto(
        input: ByteArray,
        parsed: Parsed,
    ): Result<ByteArray> =
        when (this) {
            is BorrowedFormatItem -> parsed.parseItem(input, this).fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(Parse.FromDescription(it as ParseFromDescription)) },
            )
            is OwnedFormatItem -> parsed.parseItem(input, this).fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(Parse.FromDescription(it as ParseFromDescription)) },
            )
            is Rfc2822 -> parseIntoRfc2822(input, parsed)
            is Rfc3339 -> parseIntoRfc3339(input, parsed)
            is Iso8601 -> parseIntoIso8601(this, input, parsed)
            else -> Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
        }

    /** Parse the item into a new [Parsed] struct. */
    fun parse(input: ByteArray): Result<Parsed> {
        val parsed = Parsed.new()
        val remaining = parseInto(input, parsed).getOrElse { return Result.failure(it) }
        return if (remaining.isEmpty()) {
            Result.success(parsed)
        } else {
            Result.failure(Parse.FromDescription(ParseFromDescription.UnexpectedTrailingCharacters))
        }
    }

    /** Parse a [Date] from the format description. */
    fun parseDate(input: ByteArray): Result<Date> {
        val parsed = parse(input).getOrElse { return Result.failure(it) }
        return parsed.toDate().fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(Parse.FromParsed(it as TryFromParsed)) },
        )
    }

    /** Parse a [Time] from the format description. */
    fun parseTime(input: ByteArray): Result<Time> {
        val parsed = parse(input).getOrElse { return Result.failure(it) }
        return parsed.toTime().fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(Parse.FromParsed(it as TryFromParsed)) },
        )
    }

    /** Parse a [UtcOffset] from the format description. */
    fun parseOffset(input: ByteArray): Result<UtcOffset> {
        val parsed = parse(input).getOrElse { return Result.failure(it) }
        return parsed.toUtcOffset().fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(Parse.FromParsed(it as TryFromParsed)) },
        )
    }

    /** Parse a [PrimitiveDateTime] from the format description. */
    fun parsePrimitiveDateTime(input: ByteArray): Result<PrimitiveDateTime> {
        val parsed = parse(input).getOrElse { return Result.failure(it) }
        return parsed.toPrimitiveDateTime().fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(Parse.FromParsed(it as TryFromParsed)) },
        )
    }

    /** Parse a [UtcDateTime] from the format description. */
    fun parseUtcDateTime(input: ByteArray): Result<UtcDateTime> {
        val parsed = parse(input).getOrElse { return Result.failure(it) }
        return parsed.toUtcDateTime().fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(Parse.FromParsed(it as TryFromParsed)) },
        )
    }

    /** Parse an [OffsetDateTime] from the format description. */
    fun parseOffsetDateTime(input: ByteArray): Result<OffsetDateTime> {
        if (this is Rfc2822) return parseRfc2822OffsetDateTime(input)
        if (this is Rfc3339) return parseRfc3339OffsetDateTime(input)
        val parsed = parse(input).getOrElse { return Result.failure(it) }
        return parsed.toOffsetDateTime().fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(Parse.FromParsed(it as TryFromParsed)) },
        )
    }
}

/** Adapter to treat a slice/list of [BorrowedFormatItem] as a [Parsable]. */
class BorrowedFormatItemSliceParsable(val items: List<BorrowedFormatItem>) : Parsable {
    override fun parseInto(input: ByteArray, parsed: Parsed): Result<ByteArray> =
        parsed.parseItems(input, items).fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(Parse.FromDescription(it as ParseFromDescription)) },
        )
}

/** Adapter to treat a slice/list of [OwnedFormatItem] as a [Parsable]. */
class OwnedFormatItemSliceParsable(val items: List<OwnedFormatItem>) : Parsable {
    override fun parseInto(input: ByteArray, parsed: Parsed): Result<ByteArray> =
        parsed.parseOwnedItems(input, items).fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(Parse.FromDescription(it as ParseFromDescription)) },
        )
}

private fun parseIntoRfc2822(initialInput: ByteArray, parsed: Parsed): Result<ByteArray> {
    val colon: (ByteArray) -> ParsedItem<Unit>? = { asciiChar(':'.code.toByte(), it) }
    val comma: (ByteArray) -> ParsedItem<Unit>? = { asciiChar(','.code.toByte(), it) }

    var input = opt(::cfws)(initialInput).intoInner()
    val weekday = parseWeekday(
        input,
        io.github.kotlinmania.time.formatdescription.modifier.Weekday(
            repr = WeekdayRepr.Short,
            oneIndexed = false,
            caseSensitive = false,
        ),
    )
    if (weekday != null) {
        input = weekday.consumeValue { parsed.setWeekday(it) }
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("weekday")))
        input = comma(input)?.intoInner()
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
        input = opt(::cfws)(input).intoInner()
    }

    input = oneOrTwoDigits(input)
        ?.consumeValue { v -> if (v > 0u) parsed.setDay(v) else null }
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("day")))
    input = cfws(input)?.intoInner()
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    input = parseMonth(
        input,
        io.github.kotlinmania.time.formatdescription.modifier.Month(
            padding = Padding.None,
            repr = MonthRepr.Short,
            caseSensitive = false,
        ),
    )?.consumeValue { parsed.setMonth(it) }
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("month")))
    input = cfws(input)?.intoInner()
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))

    val year4 = ExactlyNDigits.parse4(input)
    input = if (year4 != null) {
        val afterYear = year4.flatMap { y -> if (y >= 1900u) y else null }
            ?.consumeValue { parsed.setYear(it.toShort().toInt()) }
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("year")))
        fws(afterYear)?.intoInner()
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    } else {
        val afterYear = ExactlyNDigits.parse2(input)
            ?.map { y -> y.toUInt() }
            ?.map { y -> if (y < 50u) y + 2000u else y + 1900u }
            ?.map { y -> y.toInt() }
            ?.consumeValue { parsed.setYear(it) }
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("year")))
        cfws(afterYear)?.intoInner()
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    }

    input = ExactlyNDigits.parse2(input)
        ?.consumeValue { parsed.setHour24(it) }
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("hour")))
    input = opt(::cfws)(input).intoInner()
    input = colon(input)?.intoInner()
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    input = opt(::cfws)(input).intoInner()
    input = ExactlyNDigits.parse2(input)
        ?.consumeValue { parsed.setMinute(it) }
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("minute")))

    val maybeColon = colon(opt(::cfws)(input).intoInner())
    input = if (maybeColon != null) {
        var rem = maybeColon.intoInner()
        rem = opt(::cfws)(rem).intoInner()
        rem = ExactlyNDigits.parse2(rem)
            ?.consumeValue { parsed.setSecond(it) }
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("second")))
        cfws(rem)?.intoInner()
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    } else {
        cfws(input)?.intoInner()
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    }

    parsed.leapSecondAllowed = true

    val zLit = zoneLiteral(input)
    if (zLit != null) {
        val rem = zLit.consumeValue { parsed.setOffsetHour(it) }
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset hour")))
        parsed.setOffsetMinuteSigned(0)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset minute")))
        parsed.setOffsetSecondSigned(0)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset second")))
        return Result.success(rem)
    }

    val signItem = sign(input)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset hour")))
    val offsetSign = signItem.value
    input = ExactlyNDigits.parse2(signItem.remaining)
        ?.map { h ->
            when (offsetSign) {
                Sign.Negative -> (-h.toInt()).toByte()
                Sign.Positive -> h.toByte()
            }
        }
        ?.consumeValue { parsed.setOffsetHour(it) }
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset hour")))
    input = ExactlyNDigits.parse2(input)
        ?.consumeValue { parsed.setOffsetMinuteSigned(it.toByte()) }
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset minute")))

    input = opt(::cfws)(input).intoInner()
    return Result.success(input)
}

private fun parseRfc2822OffsetDateTime(initialInput: ByteArray): Result<OffsetDateTime> {
    val colon: (ByteArray) -> ParsedItem<Unit>? = { asciiChar(':'.code.toByte(), it) }
    val comma: (ByteArray) -> ParsedItem<Unit>? = { asciiChar(','.code.toByte(), it) }

    var input = opt(::cfws)(initialInput).intoInner()
    val weekday = parseWeekday(
        input,
        io.github.kotlinmania.time.formatdescription.modifier.Weekday(
            repr = WeekdayRepr.Short,
            oneIndexed = false,
            caseSensitive = false,
        ),
    )
    if (weekday != null) {
        input = weekday.discardValue()
        input = comma(input)?.intoInner()
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
        input = opt(::cfws)(input).intoInner()
    }

    val dayItem = oneOrTwoDigits(input)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("day")))
    input = dayItem.remaining
    val day = dayItem.value

    input = cfws(input)?.intoInner()
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    val monthItem = parseMonth(
        input,
        io.github.kotlinmania.time.formatdescription.modifier.Month(
            padding = Padding.None,
            repr = MonthRepr.Short,
            caseSensitive = false,
        ),
    ) ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("month")))
    input = monthItem.remaining
    val month = monthItem.value

    input = cfws(input)?.intoInner()
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))

    val year4 = ExactlyNDigits.parse4(input)
    val year: UShort = if (year4 != null) {
        val validYear = year4.flatMap { y -> if (y >= 1900u) y else null }
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("year")))
        input = fws(validYear.remaining)?.intoInner()
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
        validYear.value
    } else {
        val year2 = ExactlyNDigits.parse2(input)
            ?.map { y -> y.toUShort() }
            ?.map { y -> if (y < 50u) (y + 2000u).toUShort() else (y + 1900u).toUShort() }
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("year")))
        input = cfws(year2.remaining)?.intoInner()
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
        year2.value
    }

    val hourItem = ExactlyNDigits.parse2(input)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("hour")))
    val hour = hourItem.value
    input = opt(::cfws)(hourItem.remaining).intoInner()
    input = colon(input)?.intoInner()
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    input = opt(::cfws)(input).intoInner()
    val minuteItem = ExactlyNDigits.parse2(input)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("minute")))
    val minute = minuteItem.value
    input = minuteItem.remaining

    val maybeColon = colon(opt(::cfws)(input).intoInner())
    var second: UByte
    if (maybeColon != null) {
        var rem = maybeColon.intoInner()
        rem = opt(::cfws)(rem).intoInner()
        val secItem = ExactlyNDigits.parse2(rem)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("second")))
        second = secItem.value
        input = cfws(secItem.remaining)?.intoInner()
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    } else {
        input = cfws(input)?.intoInner()
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
        second = 0u
    }

    val offsetHour: Byte
    val offsetMinute: Byte
    val zLit = zoneLiteral(input)
    if (zLit != null) {
        input = zLit.remaining
        offsetHour = zLit.value
        offsetMinute = 0
    } else {
        val signItem = sign(input)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset hour")))
        val offsetSign = signItem.value
        val offHourItem = ExactlyNDigits.parse2(signItem.remaining)
            ?.map { h ->
                when (offsetSign) {
                    Sign.Negative -> (-h.toInt()).toByte()
                    Sign.Positive -> h.toByte()
                }
            }
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset hour")))
        val offMinItem = ExactlyNDigits.parse2(offHourItem.remaining)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset minute")))
        input = offMinItem.remaining
        offsetHour = offHourItem.value
        offsetMinute = when (offsetSign) {
            Sign.Negative -> (-offMinItem.value.toInt()).toByte()
            Sign.Positive -> offMinItem.value.toByte()
        }
    }

    input = opt(::cfws)(input).intoInner()
    if (input.isNotEmpty()) {
        return Result.failure(Parse.FromDescription(ParseFromDescription.UnexpectedTrailingCharacters))
    }

    var nanosecond = 0
    val leapSecondInput = if (second == 60.toUByte()) {
        second = 59u
        nanosecond = 999_999_999
        true
    } else {
        false
    }

    val date = Date.fromCalendarDate(year.toShort().toInt(), month, day.toInt()).getOrElse {
        return Result.failure(Parse.FromParsed(TryFromParsed.Range(it as ComponentRange)))
    }
    val time = Time.fromHmsNano(hour.toInt(), minute.toInt(), second.toInt(), nanosecond).getOrElse {
        return Result.failure(Parse.FromParsed(TryFromParsed.Range(it as ComponentRange)))
    }
    val offset = UtcOffset.fromHms(offsetHour, offsetMinute, 0).getOrElse {
        return Result.failure(Parse.FromParsed(TryFromParsed.Range(it as ComponentRange)))
    }
    val dt = OffsetDateTime.newInOffset(date, time, offset)

    if (leapSecondInput && !dt.isValidLeapSecondStandIn()) {
        return Result.failure(
            Parse.FromParsed(TryFromParsed.Range(ComponentRange.conditional("second"))),
        )
    }

    return Result.success(dt)
}

private fun parseIntoRfc3339(initialInput: ByteArray, parsed: Parsed): Result<ByteArray> {
    val dash: (ByteArray) -> ParsedItem<Unit>? = { asciiChar('-'.code.toByte(), it) }
    val colon: (ByteArray) -> ParsedItem<Unit>? = { asciiChar(':'.code.toByte(), it) }

    var input = ExactlyNDigits.parse4(initialInput)
        ?.consumeValue { parsed.setYear(it.toShort().toInt()) }
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("year")))
    input = dash(input)?.intoInner()
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    input = ExactlyNDigits.parse2(input)
        ?.flatMap { v -> if (v > 0u) Month.fromNumber(v.toInt()).getOrNull() else null }
        ?.consumeValue { parsed.setMonth(it) }
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("month")))
    input = dash(input)?.intoInner()
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    input = ExactlyNDigits.parse2(input)
        ?.consumeValue { v -> if (v > 0u) parsed.setDay(v) else null }
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("day")))

    if (input.isEmpty()) {
        return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("separator")))
    }
    input = input.copyOfRange(1, input.size)

    input = ExactlyNDigits.parse2(input)
        ?.consumeValue { parsed.setHour24(it) }
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("hour")))
    input = colon(input)?.intoInner()
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    input = ExactlyNDigits.parse2(input)
        ?.consumeValue { parsed.setMinute(it) }
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("minute")))
    input = colon(input)?.intoInner()
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    input = ExactlyNDigits.parse2(input)
        ?.consumeValue { parsed.setSecond(it) }
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("second")))

    val dot = asciiChar('.'.code.toByte(), input)
    if (dot != null) {
        val firstDigit = anyDigit(dot.remaining)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("subsecond")))
        var rem = firstDigit.remaining
        var value = (firstDigit.value - '0'.code.toUByte()).toUInt() * 100_000_000u
        var multiplier = 10_000_000u
        while (true) {
            val next = anyDigit(rem) ?: break
            value += (next.value - '0'.code.toUByte()).toUInt() * multiplier
            rem = next.remaining
            multiplier /= 10u
        }
        parsed.setSubsecond(value)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("subsecond")))
        input = rem
    }

    parsed.leapSecondAllowed = true

    val zChar = asciiCharIgnoreCase('Z'.code.toByte(), input)
    if (zChar != null) {
        parsed.setOffsetHour(0)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset hour")))
        parsed.setOffsetMinuteSigned(0)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset minute")))
        parsed.setOffsetSecondSigned(0)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset second")))
        return Result.success(zChar.remaining)
    }

    val signItem = sign(input)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset hour")))
    val offsetSign = signItem.value
    input = ExactlyNDigits.parse2(signItem.remaining)
        ?.filter { it <= 23u }
        ?.map { h ->
            when (offsetSign) {
                Sign.Negative -> (-h.toInt()).toByte()
                Sign.Positive -> h.toByte()
            }
        }
        ?.consumeValue { parsed.setOffsetHour(it) }
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset hour")))
    input = colon(input)?.intoInner()
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    input = ExactlyNDigits.parse2(input)
        ?.map { m ->
            when (offsetSign) {
                Sign.Negative -> (-m.toInt()).toByte()
                Sign.Positive -> m.toByte()
            }
        }
        ?.consumeValue { parsed.setOffsetMinuteSigned(it) }
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset minute")))

    return Result.success(input)
}

private fun parseRfc3339OffsetDateTime(initialInput: ByteArray): Result<OffsetDateTime> {
    val dash: (ByteArray) -> ParsedItem<Unit>? = { asciiChar('-'.code.toByte(), it) }
    val colon: (ByteArray) -> ParsedItem<Unit>? = { asciiChar(':'.code.toByte(), it) }

    val yearItem = ExactlyNDigits.parse4(initialInput)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("year")))
    val year = yearItem.value
    var input = dash(yearItem.remaining)?.intoInner()
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    val monthItem = ExactlyNDigits.parse2(input)
        ?.flatMap { if (it > 0u) it else null }
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("month")))
    val monthNum = monthItem.value
    input = dash(monthItem.remaining)?.intoInner()
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    val dayItem = ExactlyNDigits.parse2(input)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("day")))
    val day = dayItem.value
    input = dayItem.remaining

    if (input.isEmpty()) {
        return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("separator")))
    }
    input = input.copyOfRange(1, input.size)

    val hourItem = ExactlyNDigits.parse2(input)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("hour")))
    val hour = hourItem.value
    input = colon(hourItem.remaining)?.intoInner()
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    val minItem = ExactlyNDigits.parse2(input)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("minute")))
    val minute = minItem.value
    input = colon(minItem.remaining)?.intoInner()
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    val secItem = ExactlyNDigits.parse2(input)
        ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("second")))
    var second = secItem.value
    input = secItem.remaining

    var nanosecond: UInt
    val dot = asciiChar('.'.code.toByte(), input)
    if (dot != null) {
        val firstDigit = anyDigit(dot.remaining)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("subsecond")))
        var rem = firstDigit.remaining
        var value = (firstDigit.value - '0'.code.toUByte()).toUInt() * 100_000_000u
        var multiplier = 10_000_000u
        while (true) {
            val next = anyDigit(rem) ?: break
            value += (next.value - '0'.code.toUByte()).toUInt() * multiplier
            rem = next.remaining
            multiplier /= 10u
        }
        input = rem
        nanosecond = value
    } else {
        nanosecond = 0u
    }

    val offset: UtcOffset
    val zChar = asciiCharIgnoreCase('Z'.code.toByte(), input)
    if (zChar != null) {
        input = zChar.remaining
        offset = UtcOffset.UTC
    } else {
        val signItem = sign(input)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset hour")))
        val offsetSign = signItem.value
        val offHourItem = ExactlyNDigits.parse2(signItem.remaining)
            ?.filter { it <= 23u }
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset hour")))
        val offHour = offHourItem.value
        input = colon(offHourItem.remaining)?.intoInner()
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidLiteral))
        val offMinItem = ExactlyNDigits.parse2(input)
            ?: return Result.failure(Parse.FromDescription(ParseFromDescription.InvalidComponent("offset minute")))
        val offMin = offMinItem.value
        input = offMinItem.remaining

        offset = when (offsetSign) {
            Sign.Negative -> UtcOffset.fromHms((-offHour.toInt()).toByte(), (-offMin.toInt()).toByte(), 0)
            Sign.Positive -> UtcOffset.fromHms(offHour.toByte(), offMin.toByte(), 0)
        }.getOrElse {
            return Result.failure(Parse.FromParsed(TryFromParsed.Range(it as ComponentRange)))
        }
    }

    if (input.isNotEmpty()) {
        return Result.failure(Parse.FromDescription(ParseFromDescription.UnexpectedTrailingCharacters))
    }

    val leapSecondInput = if (second == 60.toUByte()) {
        second = 59u
        nanosecond = 999_999_999u
        true
    } else {
        false
    }

    val month = Month.fromNumber(monthNum.toInt()).getOrElse {
        return Result.failure(Parse.FromParsed(TryFromParsed.Range(it as ComponentRange)))
    }
    val date = Date.fromCalendarDate(year.toShort().toInt(), month, day.toInt()).getOrElse {
        return Result.failure(Parse.FromParsed(TryFromParsed.Range(it as ComponentRange)))
    }
    val time = Time.fromHmsNano(hour.toInt(), minute.toInt(), second.toInt(), nanosecond.toInt()).getOrElse {
        return Result.failure(Parse.FromParsed(TryFromParsed.Range(it as ComponentRange)))
    }
    val dt = OffsetDateTime.newInOffset(date, time, offset)

    if (leapSecondInput && !dt.isValidLeapSecondStandIn()) {
        return Result.failure(
            Parse.FromParsed(TryFromParsed.Range(ComponentRange.conditional("second"))),
        )
    }

    return Result.success(dt)
}

private fun parseIntoIso8601(
    iso8601: Iso8601,
    initialInput: ByteArray,
    parsed: Parsed,
): Result<ByteArray> {
    var input = initialInput
    val extendedKind = ExtendedKindHolder()
    var dateIsPresent = false
    var timeIsPresent = false
    var offsetIsPresent = false
    var firstError: Throwable? = null

    parsed.leapSecondAllowed = true

    val dateRes = iso8601.parseDate(parsed, extendedKind, input)
    if (dateRes.isSuccess) {
        input = dateRes.getOrThrow()
        dateIsPresent = true
    } else if (firstError == null) {
        firstError = dateRes.exceptionOrNull()
    }

    val timeRes = iso8601.parseTime(parsed, extendedKind, dateIsPresent, input)
    if (timeRes.isSuccess) {
        input = timeRes.getOrThrow()
        timeIsPresent = true
    } else if (firstError == null) {
        firstError = timeRes.exceptionOrNull()
    }

    if (!dateIsPresent || timeIsPresent) {
        val offsetRes = iso8601.parseOffset(parsed, extendedKind, input)
        if (offsetRes.isSuccess) {
            input = offsetRes.getOrThrow()
            offsetIsPresent = true
        } else if (firstError == null) {
            firstError = offsetRes.exceptionOrNull()
        }
    }

    if (!dateIsPresent && !timeIsPresent && !offsetIsPresent) {
        return Result.failure(firstError ?: Parse.FromDescription(ParseFromDescription.InvalidLiteral))
    }

    return Result.success(input)
}
