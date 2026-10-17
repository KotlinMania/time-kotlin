// port-lint: source parsing/parsed.rs
package io.github.kotlinmania.time.parsing

import io.github.kotlinmania.time.Date
import io.github.kotlinmania.time.MAX_UNIX_TIMESTAMP
import io.github.kotlinmania.time.MIN_UNIX_TIMESTAMP
import io.github.kotlinmania.time.Month
import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.PrimitiveDateTime
import io.github.kotlinmania.time.Time
import io.github.kotlinmania.time.UtcDateTime
import io.github.kotlinmania.time.UtcOffset
import io.github.kotlinmania.time.Weekday
import io.github.kotlinmania.time.error.ComponentRange
import io.github.kotlinmania.time.error.ParseFromDescription
import io.github.kotlinmania.time.error.TryFromParsed
import io.github.kotlinmania.time.floorDiv
import io.github.kotlinmania.time.floorMod
import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatdescription.Component
import io.github.kotlinmania.time.formatdescription.OwnedFormatItem
import io.github.kotlinmania.time.formatdescription.PeriodCase
import io.github.kotlinmania.time.formatdescription.modifier.WeekNumberRepr
import io.github.kotlinmania.time.formatdescription.modifier.YearRepr
import io.github.kotlinmania.time.formatting.computeTimestampNanosSignAndAbs
import kotlin.math.abs

/**
 * Parse a signed decimal string representing nanoseconds into `(seconds: Long, subsecondNanos: Int)`,
 * where `subsecondNanos` is in `0..999_999_999`. Returns `null` if invalid or out of `Long` seconds range.
 */
internal fun parseNanosStringToSecAndNano(s: String): Pair<Long, Int>? {
    if (s.isEmpty()) return null
    val isNegative = s[0] == '-'
    val digits = if (s[0] == '-' || s[0] == '+') s.substring(1) else s
    if (digits.isEmpty() || !digits.all { it in '0'..'9' }) return null

    val trimmed = digits.trimStart('0')
    if (trimmed.isEmpty()) return 0L to 0
    if (trimmed.length > 28) return null

    val quot: Long
    val rem: Int
    if (trimmed.length <= 9) {
        quot = 0L
        rem = trimmed.toInt()
    } else {
        val quotStr = trimmed.substring(0, trimmed.length - 9)
        val remStr = trimmed.substring(trimmed.length - 9)
        quot = quotStr.toLongOrNull() ?: return null
        rem = remStr.toInt()
    }

    return if (!isNegative) {
        quot to rem
    } else if (rem == 0) {
        (-quot) to 0
    } else {
        if (quot == Long.MAX_VALUE) return null
        (-quot - 1L) to (1_000_000_000 - rem)
    }
}

/**
 * All information parsed.
 *
 * This information is directly used to construct the final values.
 */
class Parsed {
    private var yearVal: Int? = null
    private var yearCenturyVal: Short? = null
    private var yearLastTwoVal: UByte? = null
    private var isoYearVal: Int? = null
    private var isoYearCenturyVal: Short? = null
    private var isoYearLastTwoVal: UByte? = null
    private var monthVal: Month? = null
    private var sundayWeekNumberVal: UByte? = null
    private var mondayWeekNumberVal: UByte? = null
    private var isoWeekNumberVal: UByte? = null
    private var weekdayVal: Weekday? = null
    private var ordinalVal: UShort? = null
    private var dayVal: UByte? = null
    private var hour24Val: UByte? = null
    private var hour12Val: UByte? = null
    private var hour12IsPmVal: Boolean? = null
    private var minuteVal: UByte? = null
    private var secondVal: UByte? = null
    private var subsecondVal: UInt? = null
    private var offsetHourVal: Byte? = null
    private var offsetMinuteVal: Byte? = null
    private var offsetSecondVal: Byte? = null
    private var unixTimestampSecVal: Long? = null
    private var unixTimestampNanoVal: Int = 0
    private var offsetIsNegative: Boolean = false
    private var yearCenturyIsNegativeVal: Boolean = false
    private var isoYearCenturyIsNegativeVal: Boolean = false
    internal var leapSecondAllowed: Boolean = false

    fun copy(): Parsed {
        val p = Parsed()
        p.copyFrom(this)
        return p
    }

    internal fun copyFrom(other: Parsed) {
        this.yearVal = other.yearVal
        this.yearCenturyVal = other.yearCenturyVal
        this.yearLastTwoVal = other.yearLastTwoVal
        this.isoYearVal = other.isoYearVal
        this.isoYearCenturyVal = other.isoYearCenturyVal
        this.isoYearLastTwoVal = other.isoYearLastTwoVal
        this.monthVal = other.monthVal
        this.sundayWeekNumberVal = other.sundayWeekNumberVal
        this.mondayWeekNumberVal = other.mondayWeekNumberVal
        this.isoWeekNumberVal = other.isoWeekNumberVal
        this.weekdayVal = other.weekdayVal
        this.ordinalVal = other.ordinalVal
        this.dayVal = other.dayVal
        this.hour24Val = other.hour24Val
        this.hour12Val = other.hour12Val
        this.hour12IsPmVal = other.hour12IsPmVal
        this.minuteVal = other.minuteVal
        this.secondVal = other.secondVal
        this.subsecondVal = other.subsecondVal
        this.offsetHourVal = other.offsetHourVal
        this.offsetMinuteVal = other.offsetMinuteVal
        this.offsetSecondVal = other.offsetSecondVal
        this.unixTimestampSecVal = other.unixTimestampSecVal
        this.unixTimestampNanoVal = other.unixTimestampNanoVal
        this.offsetIsNegative = other.offsetIsNegative
        this.yearCenturyIsNegativeVal = other.yearCenturyIsNegativeVal
        this.isoYearCenturyIsNegativeVal = other.isoYearCenturyIsNegativeVal
        this.leapSecondAllowed = other.leapSecondAllowed
    }

    /** Parse a single [BorrowedFormatItem], mutating the struct. */
    fun parseItem(
        input: ByteArray,
        item: BorrowedFormatItem,
    ): Result<ByteArray> =
        when (item) {
            is BorrowedFormatItem.Literal -> parseLiteral(input, item.value)
            is BorrowedFormatItem.ComponentItem -> parseComponent(input, item.value)
            is BorrowedFormatItem.Compound -> parseItems(input, item.value)
            is BorrowedFormatItem.Optional -> {
                val res = parseItem(input, item.value)
                if (res.isSuccess) res else Result.success(input)
            }
            is BorrowedFormatItem.First -> {
                var firstErr: Throwable? = null
                var matched: ByteArray? = null
                for (sub in item.value) {
                    val res = parseItem(input, sub)
                    if (res.isSuccess) {
                        matched = res.getOrThrow()
                        break
                    } else if (firstErr == null) {
                        firstErr = res.exceptionOrNull()
                    }
                }
                if (matched != null) {
                    Result.success(matched)
                } else if (firstErr != null) {
                    Result.failure(firstErr)
                } else {
                    Result.success(input)
                }
            }
        }

    /** Parse a single [OwnedFormatItem], mutating the struct. */
    fun parseItem(
        input: ByteArray,
        item: OwnedFormatItem,
    ): Result<ByteArray> =
        when (item) {
            is OwnedFormatItem.Literal -> parseLiteral(input, item.value)
            is OwnedFormatItem.ComponentItem -> parseComponent(input, item.value)
            is OwnedFormatItem.Compound -> parseOwnedItems(input, item.value)
            is OwnedFormatItem.Optional -> {
                val res = parseItem(input, item.value)
                if (res.isSuccess) res else Result.success(input)
            }
            is OwnedFormatItem.First -> {
                var firstErr: Throwable? = null
                var matched: ByteArray? = null
                for (sub in item.value) {
                    val res = parseItem(input, sub)
                    if (res.isSuccess) {
                        matched = res.getOrThrow()
                        break
                    } else if (firstErr == null) {
                        firstErr = res.exceptionOrNull()
                    }
                }
                if (matched != null) {
                    Result.success(matched)
                } else if (firstErr != null) {
                    Result.failure(firstErr)
                } else {
                    Result.success(input)
                }
            }
        }

    /** Parse a sequence of [BorrowedFormatItem]s, mutating the struct only if all succeed. */
    fun parseItems(
        initialInput: ByteArray,
        items: List<BorrowedFormatItem>,
    ): Result<ByteArray> {
        val working = this.copy()
        var input = initialInput
        for (item in items) {
            input = working.parseItem(input, item).getOrElse { return Result.failure(it) }
        }
        this.copyFrom(working)
        return Result.success(input)
    }

    /** Parse a sequence of [OwnedFormatItem]s, mutating the struct only if all succeed. */
    fun parseOwnedItems(
        initialInput: ByteArray,
        items: List<OwnedFormatItem>,
    ): Result<ByteArray> {
        val working = this.copy()
        var input = initialInput
        for (item in items) {
            input = working.parseItem(input, item).getOrElse { return Result.failure(it) }
        }
        this.copyFrom(working)
        return Result.success(input)
    }

    /** Parse a single component, mutating the struct. */
    fun parseComponent(
        input: ByteArray,
        component: Component,
    ): Result<ByteArray> =
        when (component) {
            is Component.Day -> {
                val remaining = parseDay(input, component.modifiers)
                    ?.consumeValue { setDay(it) }
                    ?: return Result.failure(ParseFromDescription.InvalidComponent("day"))
                Result.success(remaining)
            }
            is Component.Month -> {
                val remaining = parseMonth(input, component.modifiers)
                    ?.consumeValue { setMonth(it) }
                    ?: return Result.failure(ParseFromDescription.InvalidComponent("month"))
                Result.success(remaining)
            }
            is Component.Ordinal -> {
                val remaining = parseOrdinal(input, component.modifiers)
                    ?.consumeValue { setOrdinal(it) }
                    ?: return Result.failure(ParseFromDescription.InvalidComponent("ordinal"))
                Result.success(remaining)
            }
            is Component.Weekday -> {
                val remaining = parseWeekday(input, component.modifiers)
                    ?.consumeValue { setWeekday(it) }
                    ?: return Result.failure(ParseFromDescription.InvalidComponent("weekday"))
                Result.success(remaining)
            }
            is Component.WeekNumber -> {
                val item = parseWeekNumber(input, component.modifiers)
                    ?: return Result.failure(ParseFromDescription.InvalidComponent("week number"))
                val setOk = when (component.modifiers.repr) {
                    WeekNumberRepr.Iso -> if (item.value > 0u) setIsoWeekNumber(item.value) else null
                    WeekNumberRepr.Sunday -> setSundayWeekNumber(item.value)
                    WeekNumberRepr.Monday -> setMondayWeekNumber(item.value)
                }
                if (setOk == null) {
                    Result.failure(ParseFromDescription.InvalidComponent("week number"))
                } else {
                    Result.success(item.remaining)
                }
            }
            is Component.Year -> {
                val item = parseYear(input, component.modifiers)
                    ?: return Result.failure(ParseFromDescription.InvalidComponent("year"))
                val (value, isNegative) = item.value
                val setOk = if (!component.modifiers.isoWeekBased) {
                    when (component.modifiers.repr) {
                        YearRepr.Full -> setYear(value)
                        YearRepr.Century -> setYearCentury(value.toShort(), isNegative)
                        YearRepr.LastTwo -> setYearLastTwo(value.toUInt().toUByte())
                    }
                } else {
                    when (component.modifiers.repr) {
                        YearRepr.Full -> setIsoYear(value)
                        YearRepr.Century -> setIsoYearCentury(value.toShort(), isNegative)
                        YearRepr.LastTwo -> setIsoYearLastTwo(value.toUInt().toUByte())
                    }
                }
                if (setOk == null) {
                    Result.failure(ParseFromDescription.InvalidComponent("year"))
                } else {
                    Result.success(item.remaining)
                }
            }
            is Component.Hour -> {
                val item = parseHour(input, component.modifiers)
                    ?: return Result.failure(ParseFromDescription.InvalidComponent("hour"))
                val setOk = if (component.modifiers.is12HourClock) {
                    if (item.value > 0u) setHour12(item.value) else null
                } else {
                    setHour24(item.value)
                }
                if (setOk == null) {
                    Result.failure(ParseFromDescription.InvalidComponent("hour"))
                } else {
                    Result.success(item.remaining)
                }
            }
            is Component.Minute -> {
                val remaining = parseMinute(input, component.modifiers)
                    ?.consumeValue { setMinute(it) }
                    ?: return Result.failure(ParseFromDescription.InvalidComponent("minute"))
                Result.success(remaining)
            }
            is Component.Period -> {
                val remaining = parsePeriod(input, component.modifiers)
                    ?.consumeValue { setHour12IsPm(it == PeriodCase.Pm) }
                    ?: return Result.failure(ParseFromDescription.InvalidComponent("period"))
                Result.success(remaining)
            }
            is Component.Second -> {
                val remaining = parseSecond(input, component.modifiers)
                    ?.consumeValue { setSecond(it) }
                    ?: return Result.failure(ParseFromDescription.InvalidComponent("second"))
                Result.success(remaining)
            }
            is Component.Subsecond -> {
                val remaining = parseSubsecond(input, component.modifiers)
                    ?.consumeValue { setSubsecond(it) }
                    ?: return Result.failure(ParseFromDescription.InvalidComponent("subsecond"))
                Result.success(remaining)
            }
            is Component.OffsetHour -> {
                val remaining = parseOffsetHour(input, component.modifiers)
                    ?.consumeValue { (value, isNegative) ->
                        setOffsetHour(value) ?: return@consumeValue null
                        this.offsetIsNegative = isNegative
                        Unit
                    }
                    ?: return Result.failure(ParseFromDescription.InvalidComponent("offset hour"))
                Result.success(remaining)
            }
            is Component.OffsetMinute -> {
                val remaining = parseOffsetMinute(input, component.modifiers)
                    ?.consumeValue { setOffsetMinuteSigned(it) }
                    ?: return Result.failure(ParseFromDescription.InvalidComponent("offset minute"))
                Result.success(remaining)
            }
            is Component.OffsetSecond -> {
                val remaining = parseOffsetSecond(input, component.modifiers)
                    ?.consumeValue { setOffsetSecondSigned(it) }
                    ?: return Result.failure(ParseFromDescription.InvalidComponent("offset second"))
                Result.success(remaining)
            }
            is Component.Ignore -> {
                val item = parseIgnore(input, component.modifiers)
                    ?: return Result.failure(ParseFromDescription.InvalidComponent("ignore"))
                Result.success(item.intoInner())
            }
            is Component.UnixTimestamp -> {
                val remaining = parseUnixTimestamp(input, component.modifiers)
                    ?.consumeValue { setUnixTimestampNanosString(it) }
                    ?: return Result.failure(ParseFromDescription.InvalidComponent("unix_timestamp"))
                Result.success(remaining)
            }
            is Component.End -> {
                val item = parseEnd(input, component.modifiers)
                    ?: return Result.failure(ParseFromDescription.UnexpectedTrailingCharacters)
                Result.success(item.intoInner())
            }
        }

    // Getter methods
    fun year(): Int? = yearVal

    fun yearCentury(): Short? = yearCenturyVal

    fun yearCenturyIsNegative(): Boolean? = if (yearCenturyVal != null) yearCenturyIsNegativeVal else null

    fun yearLastTwo(): UByte? = yearLastTwoVal

    fun isoYear(): Int? = isoYearVal

    fun isoYearCentury(): Short? = isoYearCenturyVal

    fun isoYearCenturyIsNegative(): Boolean? = if (isoYearCenturyVal != null) isoYearCenturyIsNegativeVal else null

    fun isoYearLastTwo(): UByte? = isoYearLastTwoVal

    fun month(): Month? = monthVal

    fun sundayWeekNumber(): UByte? = sundayWeekNumberVal

    fun mondayWeekNumber(): UByte? = mondayWeekNumberVal

    fun isoWeekNumber(): UByte? = isoWeekNumberVal

    fun weekday(): Weekday? = weekdayVal

    fun ordinal(): UShort? = ordinalVal

    fun day(): UByte? = dayVal

    fun hour24(): UByte? = hour24Val

    fun hour12(): UByte? = hour12Val

    fun hour12IsPm(): Boolean? = hour12IsPmVal

    fun minute(): UByte? = minuteVal

    fun second(): UByte? = secondVal

    fun subsecond(): UInt? = subsecondVal

    fun offsetHour(): Byte? = offsetHourVal

    fun offsetMinute(): UByte? = offsetMinuteSigned()?.let { abs(it.toInt()).toUByte() }

    fun offsetMinuteSigned(): Byte? {
        val m = offsetMinuteVal ?: return null
        return if (offsetIsNegative) (-m.toInt()).toByte() else m
    }

    fun offsetSecond(): UByte? = offsetSecondSigned()?.let { abs(it.toInt()).toUByte() }

    fun offsetSecondSigned(): Byte? {
        val s = offsetSecondVal ?: return null
        return if (offsetIsNegative) (-s.toInt()).toByte() else s
    }

    /** Obtain the `unix_timestamp_nanos` component as a 64-bit [Long] (when within [Long] range). */
    fun unixTimestampNanos(): Long? {
        val sec = unixTimestampSecVal ?: return null
        val nano = unixTimestampNanoVal.toLong()
        return if (sec >= 0L) {
            if (sec > Long.MAX_VALUE / 1_000_000_000L) return null
            val prod = sec * 1_000_000_000L
            if (prod > Long.MAX_VALUE - nano) return null
            prod + nano
        } else {
            val secPlus1 = sec + 1L
            if (secPlus1 < Long.MIN_VALUE / 1_000_000_000L) return null
            val prod = secPlus1 * 1_000_000_000L
            val remNeg = 1_000_000_000L - nano
            if (prod < Long.MIN_VALUE + remNeg) return null
            prod - remNeg
        }
    }

    /** Obtain the `unix_timestamp_nanos` component as a signed decimal [String] (full 128-bit range). */
    fun unixTimestampNanosString(): String? {
        val sec = unixTimestampSecVal ?: return null
        val (isNeg, absStr) = computeTimestampNanosSignAndAbs(sec, unixTimestampNanoVal)
        return if (isNeg) "-$absStr" else absStr
    }

    internal fun unixTimestampSecAndNano(): Pair<Long, Int>? {
        val sec = unixTimestampSecVal ?: return null
        return sec to unixTimestampNanoVal
    }

    // Setter methods
    fun setYear(value: Int): Unit? = withYear(value)?.let { copyFrom(it) }

    fun setYearCentury(value: Short, isNegative: Boolean): Unit? {
        val minCentury = (Date.MIN_YEAR / 100).toShort()
        val maxCentury = (Date.MAX_YEAR / 100).toShort()
        if (value !in minCentury..maxCentury) return null
        yearCenturyVal = value
        yearCenturyIsNegativeVal = if (value.toInt() != 0) value < 0 else isNegative
        return Unit
    }

    fun setYearLastTwo(value: UByte): Unit? = withYearLastTwo(value)?.let { copyFrom(it) }

    fun setIsoYear(value: Int): Unit? = withIsoYear(value)?.let { copyFrom(it) }

    fun setIsoYearCentury(value: Short, isNegative: Boolean): Unit? {
        val minCentury = (Date.MIN_YEAR / 100).toShort()
        val maxCentury = (Date.MAX_YEAR / 100).toShort()
        if (value !in minCentury..maxCentury) return null
        isoYearCenturyVal = value
        isoYearCenturyIsNegativeVal = if (value.toInt() != 0) value < 0 else isNegative
        return Unit
    }

    fun setIsoYearLastTwo(value: UByte): Unit? = withIsoYearLastTwo(value)?.let { copyFrom(it) }

    fun setMonth(value: Month): Unit? = withMonth(value)?.let { copyFrom(it) }

    fun setSundayWeekNumber(value: UByte): Unit? = withSundayWeekNumber(value)?.let { copyFrom(it) }

    fun setMondayWeekNumber(value: UByte): Unit? = withMondayWeekNumber(value)?.let { copyFrom(it) }

    fun setIsoWeekNumber(value: UByte): Unit? = withIsoWeekNumber(value)?.let { copyFrom(it) }

    fun setWeekday(value: Weekday): Unit? = withWeekday(value)?.let { copyFrom(it) }

    fun setOrdinal(value: UShort): Unit? = withOrdinal(value)?.let { copyFrom(it) }

    fun setDay(value: UByte): Unit? = withDay(value)?.let { copyFrom(it) }

    fun setHour24(value: UByte): Unit? = withHour24(value)?.let { copyFrom(it) }

    fun setHour12(value: UByte): Unit? = withHour12(value)?.let { copyFrom(it) }

    fun setHour12IsPm(value: Boolean): Unit? = withHour12IsPm(value)?.let { copyFrom(it) }

    fun setMinute(value: UByte): Unit? = withMinute(value)?.let { copyFrom(it) }

    fun setSecond(value: UByte): Unit? = withSecond(value)?.let { copyFrom(it) }

    fun setSubsecond(value: UInt): Unit? = withSubsecond(value)?.let { copyFrom(it) }

    fun setOffsetHour(value: Byte): Unit? = withOffsetHour(value)?.let { copyFrom(it) }

    fun setOffsetMinuteSigned(value: Byte): Unit? = withOffsetMinuteSigned(value)?.let { copyFrom(it) }

    fun setOffsetSecondSigned(value: Byte): Unit? = withOffsetSecondSigned(value)?.let { copyFrom(it) }

    fun setOffsetMinute(value: UByte): Unit? =
        if (value > Byte.MAX_VALUE.toUByte()) null else setOffsetMinuteSigned(value.toByte())

    fun setOffsetSecond(value: UByte): Unit? =
        if (value > Byte.MAX_VALUE.toUByte()) null else setOffsetSecondSigned(value.toByte())

    fun setUnixTimestampNanos(value: Long): Unit? = withUnixTimestampNanos(value)?.let { copyFrom(it) }

    fun setUnixTimestampNanosString(value: String): Unit? = withUnixTimestampNanosString(value)?.let { copyFrom(it) }

    // Builder methods
    fun withYear(value: Int): Parsed? {
        if (value !in Date.MIN_YEAR..Date.MAX_YEAR) return null
        val p = copy()
        p.yearVal = value
        return p
    }

    fun withYearCentury(value: Short, isNegative: Boolean): Parsed? {
        val p = copy()
        p.setYearCentury(value, isNegative) ?: return null
        return p
    }

    fun withYearLastTwo(value: UByte): Parsed? {
        if (value > 99u) return null
        val p = copy()
        p.yearLastTwoVal = value
        return p
    }

    fun withIsoYear(value: Int): Parsed? {
        if (value !in Date.MIN_YEAR..Date.MAX_YEAR) return null
        val p = copy()
        p.isoYearVal = value
        return p
    }

    fun withIsoYearCentury(value: Short, isNegative: Boolean): Parsed? {
        val p = copy()
        p.setIsoYearCentury(value, isNegative) ?: return null
        return p
    }

    fun withIsoYearLastTwo(value: UByte): Parsed? {
        if (value > 99u) return null
        val p = copy()
        p.isoYearLastTwoVal = value
        return p
    }

    fun withMonth(value: Month): Parsed? {
        val p = copy()
        p.monthVal = value
        return p
    }

    fun withSundayWeekNumber(value: UByte): Parsed? {
        if (value > 53u) return null
        val p = copy()
        p.sundayWeekNumberVal = value
        return p
    }

    fun withMondayWeekNumber(value: UByte): Parsed? {
        if (value > 53u) return null
        val p = copy()
        p.mondayWeekNumberVal = value
        return p
    }

    fun withIsoWeekNumber(value: UByte): Parsed? {
        if (value !in 1u..53u) return null
        val p = copy()
        p.isoWeekNumberVal = value
        return p
    }

    fun withWeekday(value: Weekday): Parsed? {
        val p = copy()
        p.weekdayVal = value
        return p
    }

    fun withOrdinal(value: UShort): Parsed? {
        if (value !in 1u..366u) return null
        val p = copy()
        p.ordinalVal = value
        return p
    }

    fun withDay(value: UByte): Parsed? {
        if (value !in 1u..31u) return null
        val p = copy()
        p.dayVal = value
        return p
    }

    fun withHour24(value: UByte): Parsed? {
        if (value > 23u) return null
        val p = copy()
        p.hour24Val = value
        return p
    }

    fun withHour12(value: UByte): Parsed? {
        if (value !in 1u..12u) return null
        val p = copy()
        p.hour12Val = value
        return p
    }

    fun withHour12IsPm(value: Boolean): Parsed? {
        val p = copy()
        p.hour12IsPmVal = value
        return p
    }

    fun withMinute(value: UByte): Parsed? {
        if (value > 59u) return null
        val p = copy()
        p.minuteVal = value
        return p
    }

    fun withSecond(value: UByte): Parsed? {
        if (value > 60u) return null
        val p = copy()
        p.secondVal = value
        return p
    }

    fun withSubsecond(value: UInt): Parsed? {
        if (value > 999_999_999u) return null
        val p = copy()
        p.subsecondVal = value
        return p
    }

    fun withOffsetHour(value: Byte): Parsed? {
        if (value !in -23..23) return null
        val p = copy()
        p.offsetHourVal = value
        return p
    }

    fun withOffsetMinute(value: UByte): Parsed? =
        if (value > Byte.MAX_VALUE.toUByte()) null else withOffsetMinuteSigned(value.toByte())

    fun withOffsetMinuteSigned(value: Byte): Parsed? {
        if (value !in -59..59) return null
        val p = copy()
        p.offsetMinuteVal = value
        return p
    }

    fun withOffsetSecond(value: UByte): Parsed? =
        if (value > Byte.MAX_VALUE.toUByte()) null else withOffsetSecondSigned(value.toByte())

    fun withOffsetSecondSigned(value: Byte): Parsed? {
        if (value !in -59..59) return null
        val p = copy()
        p.offsetSecondVal = value
        return p
    }

    fun withUnixTimestampNanos(value: Long): Parsed? {
        val sec = floorDiv(value, 1_000_000_000L)
        val nano = floorMod(value, 1_000_000_000L).toInt()
        if (sec !in MIN_UNIX_TIMESTAMP..MAX_UNIX_TIMESTAMP) return null
        val p = copy()
        p.unixTimestampSecVal = sec
        p.unixTimestampNanoVal = nano
        return p
    }

    fun withUnixTimestampNanosString(value: String): Parsed? {
        val (sec, nano) = parseNanosStringToSecAndNano(value) ?: return null
        if (sec !in MIN_UNIX_TIMESTAMP..MAX_UNIX_TIMESTAMP) return null
        val p = copy()
        p.unixTimestampSecVal = sec
        p.unixTimestampNanoVal = nano
        return p
    }

    // TryFrom<Parsed> conversions
    fun toDate(): Result<Date> {
        val parsed = this.copy()

        fun adjustment(year: Int): Int =
            when (Date.fromOrdinalDateUnchecked(year, 1).weekday()) {
                Weekday.Monday -> 7
                Weekday.Tuesday -> 1
                Weekday.Wednesday -> 2
                Weekday.Thursday -> 3
                Weekday.Friday -> 4
                Weekday.Saturday -> 5
                Weekday.Sunday -> 6
            }

        if (parsed.year() == null) {
            val century = parsed.yearCentury()
            val isNegative = parsed.yearCenturyIsNegative()
            val lastTwo = parsed.yearLastTwo()
            if (century != null && isNegative != null && lastTwo != null) {
                val y = if (isNegative) {
                    100 * century.toInt() - lastTwo.toInt()
                } else {
                    100 * century.toInt() + lastTwo.toInt()
                }
                if (y in Date.MIN_YEAR..Date.MAX_YEAR) {
                    parsed.yearVal = y
                }
            }
        }

        if (parsed.isoYear() == null) {
            val century = parsed.isoYearCentury()
            val isNegative = parsed.isoYearCenturyIsNegative()
            val lastTwo = parsed.isoYearLastTwo()
            if (century != null && isNegative != null && lastTwo != null) {
                val y = if (isNegative) {
                    100 * century.toInt() - lastTwo.toInt()
                } else {
                    100 * century.toInt() + lastTwo.toInt()
                }
                if (y in Date.MIN_YEAR..Date.MAX_YEAR) {
                    parsed.isoYearVal = y
                }
            }
        }

        val y = parsed.year()
        val ord = parsed.ordinal()
        if (y != null && ord != null) {
            return Date.fromOrdinalDate(y, ord.toInt()).fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(TryFromParsed.Range(it as ComponentRange)) },
            )
        }

        val m = parsed.month()
        val d = parsed.day()
        if (y != null && m != null && d != null) {
            return Date.fromCalendarDate(y, m, d.toInt()).fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(TryFromParsed.Range(it as ComponentRange)) },
            )
        }

        val isoY = parsed.isoYear()
        val isoW = parsed.isoWeekNumber()
        val wd = parsed.weekday()
        if (isoY != null && isoW != null && wd != null) {
            return Date.fromIsoWeekDate(isoY, isoW.toInt(), wd).fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(TryFromParsed.Range(it as ComponentRange)) },
            )
        }

        val sunW = parsed.sundayWeekNumber()
        if (y != null && sunW != null && wd != null) {
            val computedOrd = (sunW.toInt() * 7 + wd.numberDaysFromSunday() - adjustment(y) + 1)
                .toShort().toUShort().toInt()
            return Date.fromOrdinalDate(y, computedOrd).fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(TryFromParsed.Range(it as ComponentRange)) },
            )
        }

        val monW = parsed.mondayWeekNumber()
        if (y != null && monW != null && wd != null) {
            val computedOrd = (monW.toInt() * 7 + wd.numberDaysFromMonday() - adjustment(y) + 1)
                .toShort().toUShort().toInt()
            return Date.fromOrdinalDate(y, computedOrd).fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(TryFromParsed.Range(it as ComponentRange)) },
            )
        }

        return Result.failure(TryFromParsed.InsufficientInformation)
    }

    fun toTime(): Result<Time> {
        val h24 = hour24()
        val h12 = hour12()
        val isPm = hour12IsPm()
        val hour: Int = when {
            h24 != null -> h24.toInt()
            h12 != null && isPm == false && h12.toInt() == 12 -> 0
            h12 != null && isPm == true && h12.toInt() == 12 -> 12
            h12 != null && isPm == false -> h12.toInt()
            h12 != null && isPm == true -> h12.toInt() + 12
            else -> return Result.failure(TryFromParsed.InsufficientInformation)
        }

        val min = minute()
        val sec = second()
        val sub = subsecond()

        if (h24 == null && h12 != null && isPm != null && min == null && sec == null && sub == null) {
            return Time.fromHmsNano(hour, 0, 0, 0).fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(TryFromParsed.Range(it as ComponentRange)) },
            )
        }

        return when {
            min == null && sec == null && sub == null ->
                Time.fromHmsNano(hour, 0, 0, 0)
            min != null && sec == null && sub == null ->
                Time.fromHmsNano(hour, min.toInt(), 0, 0)
            min != null && sec != null && sub == null ->
                Time.fromHmsNano(hour, min.toInt(), sec.toInt(), 0)
            min != null && sec != null && sub != null ->
                Time.fromHmsNano(hour, min.toInt(), sec.toInt(), sub.toInt())
            else -> return Result.failure(TryFromParsed.InsufficientInformation)
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(TryFromParsed.Range(it as ComponentRange)) },
        )
    }

    internal fun toUtcOffset(required: Boolean): Result<UtcOffset> {
        val h = offsetHour()
        if (h == null) {
            return if (required) {
                Result.failure(TryFromParsed.InsufficientInformation)
            } else {
                Result.success(UtcOffset.UTC)
            }
        }
        val m = offsetMinuteSigned()
        val s = if (m != null) offsetSecondSigned() else null
        return UtcOffset.fromHms(h, m ?: 0, s ?: 0).fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(TryFromParsed.Range(it as ComponentRange)) },
        )
    }

    fun toUtcOffset(): Result<UtcOffset> = toUtcOffset(required = true)

    fun toPrimitiveDateTime(): Result<PrimitiveDateTime> {
        val d = toDate().getOrElse { return Result.failure(it) }
        val t = toTime().getOrElse { return Result.failure(it) }
        return Result.success(PrimitiveDateTime.new(d, t))
    }

    fun toUtcDateTime(): Result<UtcDateTime> {
        val parsed = this.copy()
        val ts = parsed.unixTimestampSecAndNano()
        if (ts != null) {
            val (sec, nano) = ts
            val base = UtcDateTime.fromUnixTimestamp(sec).getOrElse {
                return Result.failure(TryFromParsed.Range(it as ComponentRange))
            }
            val finalNano = parsed.subsecond()?.toInt() ?: nano
            val value = base.replaceNanosecond(finalNano).getOrElse {
                return Result.failure(TryFromParsed.Range(it as ComponentRange))
            }
            return Result.success(value)
        }

        val leapSecondInput = if (parsed.leapSecondAllowed && parsed.second() == 60.toUByte()) {
            parsed.setSecond(59u)
            parsed.setSubsecond(999_999_999u)
            true
        } else {
            false
        }

        val d = parsed.toDate().getOrElse { return Result.failure(it) }
        val t = parsed.toTime().getOrElse { return Result.failure(it) }
        val off = parsed.toUtcOffset(required = false).getOrElse { return Result.failure(it) }

        val dt = OffsetDateTime.newInOffset(d, t, off).toUtc()
        if (leapSecondInput && !dt.isValidLeapSecondStandIn()) {
            return Result.failure(TryFromParsed.Range(ComponentRange.conditional("second")))
        }
        return Result.success(dt)
    }

    fun toOffsetDateTime(): Result<OffsetDateTime> {
        val parsed = this.copy()
        val ts = parsed.unixTimestampSecAndNano()
        if (ts != null) {
            val (sec, nano) = ts
            val base = OffsetDateTime.fromUnixTimestamp(sec).getOrElse {
                return Result.failure(TryFromParsed.Range(it as ComponentRange))
            }
            val finalNano = parsed.subsecond()?.toInt() ?: nano
            val value = base.replaceNanosecond(finalNano).getOrElse {
                return Result.failure(TryFromParsed.Range(it as ComponentRange))
            }
            return Result.success(value)
        }

        val leapSecondInput = if (parsed.leapSecondAllowed && parsed.second() == 60.toUByte()) {
            parsed.setSecond(59u)
            parsed.setSubsecond(999_999_999u)
            true
        } else {
            false
        }

        val d = parsed.toDate().getOrElse { return Result.failure(it) }
        val t = parsed.toTime().getOrElse { return Result.failure(it) }
        val off = parsed.toUtcOffset(required = true).getOrElse { return Result.failure(it) }

        val dt = OffsetDateTime.newInOffset(d, t, off)
        if (leapSecondInput && !dt.isValidLeapSecondStandIn()) {
            return Result.failure(TryFromParsed.Range(ComponentRange.conditional("second")))
        }
        return Result.success(dt)
    }

    companion object {
        fun new(): Parsed = Parsed()

        /** Parse a literal byte sequence. The remaining input is returned as the `Ok` value. */
        fun parseLiteral(
            input: ByteArray,
            literal: ByteArray,
        ): Result<ByteArray> {
            if (input.size < literal.size) {
                return Result.failure(ParseFromDescription.InvalidLiteral)
            }
            for (i in literal.indices) {
                if (input[i] != literal[i]) {
                    return Result.failure(ParseFromDescription.InvalidLiteral)
                }
            }
            return Result.success(input.copyOfRange(literal.size, input.size))
        }
    }
}
