package io.github.kotlinmania.time.formatting

import io.github.kotlinmania.time.Month
import io.github.kotlinmania.time.Weekday
import io.github.kotlinmania.time.error.ComponentRange
import io.github.kotlinmania.time.error.Format
import io.github.kotlinmania.time.ext.numDigits
import io.github.kotlinmania.time.formatdescription.Component
import io.github.kotlinmania.time.formatdescription.PeriodCase
import io.github.kotlinmania.time.formatdescription.modifier.Day
import io.github.kotlinmania.time.formatdescription.modifier.Hour
import io.github.kotlinmania.time.formatdescription.modifier.Minute
import io.github.kotlinmania.time.formatdescription.modifier.MonthRepr
import io.github.kotlinmania.time.formatdescription.modifier.OffsetHour
import io.github.kotlinmania.time.formatdescription.modifier.OffsetMinute
import io.github.kotlinmania.time.formatdescription.modifier.OffsetSecond
import io.github.kotlinmania.time.formatdescription.modifier.Ordinal
import io.github.kotlinmania.time.formatdescription.modifier.Padding
import io.github.kotlinmania.time.formatdescription.modifier.Period
import io.github.kotlinmania.time.formatdescription.modifier.Second
import io.github.kotlinmania.time.formatdescription.modifier.Subsecond
import io.github.kotlinmania.time.formatdescription.modifier.SubsecondDigits
import io.github.kotlinmania.time.formatdescription.modifier.UnixTimestamp
import io.github.kotlinmania.time.formatdescription.modifier.UnixTimestampPrecision
import io.github.kotlinmania.time.formatdescription.modifier.WeekNumber
import io.github.kotlinmania.time.formatdescription.modifier.WeekNumberRepr
import io.github.kotlinmania.time.formatdescription.modifier.WeekdayRepr
import io.github.kotlinmania.time.formatdescription.modifier.Year
import io.github.kotlinmania.time.formatdescription.modifier.YearRange
import io.github.kotlinmania.time.formatdescription.modifier.YearRepr
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.truncate

internal val MONTH_NAMES: Array<String> =
    arrayOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December",
    )

internal val WEEKDAY_NAMES: Array<String> =
    arrayOf(
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
        "Sunday",
    )

/** Write all bytes/characters to the output, returning the number of bytes written. */
internal fun write(output: Appendable, str: String): Result<Int> =
    try {
        output.append(str)
        Result.success(str.encodeToByteArray().size)
    } catch (e: Throwable) {
        Result.failure(Format.StdIo(e))
    }

internal fun write(output: Appendable, bytes: ByteArray): Result<Int> =
    try {
        output.append(bytes.decodeToString())
        Result.success(bytes.size)
    } catch (e: Throwable) {
        Result.failure(Format.StdIo(e))
    }

/** If `pred` is true, write all bytes to the output, returning the number of bytes written. */
internal fun writeIf(output: Appendable, pred: Boolean, str: String): Result<Int> =
    if (pred) write(output, str) else Result.success(0)

/** If `pred` is true, write `trueStr` to the output. Otherwise, write `falseStr`. */
internal fun writeIfElse(
    output: Appendable,
    pred: Boolean,
    trueStr: String,
    falseStr: String,
): Result<Int> = write(output, if (pred) trueStr else falseStr)

internal fun f64TenPowX(x: UByte): Double =
    when (x.toInt()) {
        1 -> 10.0
        2 -> 100.0
        3 -> 1_000.0
        4 -> 10_000.0
        5 -> 100_000.0
        6 -> 1_000_000.0
        7 -> 10_000_000.0
        8 -> 100_000_000.0
        9 -> 1_000_000_000.0
        else -> 10.0.pow(x.toInt())
    }

/**
 * Write the floating point number to the output, returning the number of bytes written.
 */
internal fun formatFloat(
    output: Appendable,
    rawValue: Double,
    digitsBeforeDecimal: UByte,
    digitsAfterDecimal: UByte?,
): Result<Int> {
    if (digitsAfterDecimal != null) {
        var value = rawValue
        val afterDigits = digitsAfterDecimal.toInt()
        if (afterDigits < 9) {
            val truncNum = f64TenPowX(digitsAfterDecimal)
            value = truncate(value * truncNum) / truncNum
        }

        val intPart = truncate(value).toULong()
        val fracMultiplier = f64TenPowX(digitsAfterDecimal.coerceAtMost(9u))
        val fracVal = kotlin.math.round((value - intPart.toDouble()) * fracMultiplier).toULong()
        val intStr = intPart.toString().padStart(digitsBeforeDecimal.toInt(), '0')
        val fracBase =
            if (afterDigits <= 9) {
                fracVal.toString().padStart(afterDigits, '0')
            } else {
                fracVal.toString().padStart(9, '0').padEnd(afterDigits, '0')
            }
        return write(output, "$intStr.$fracBase")
    } else {
        val intPart = truncate(rawValue).toULong()
        val intStr = intPart.toString().padStart(digitsBeforeDecimal.toInt(), '0')
        return write(output, intStr)
    }
}

internal fun formatNumber(
    width: Int,
    output: Appendable,
    value: UInt,
    padding: Padding,
): Result<Int> =
    when (padding) {
        Padding.Space -> formatNumberPadSpace(width, output, value)
        Padding.Zero -> formatNumberPadZero(width, output, value)
        Padding.None -> formatNumberPadNone(output, value)
    }

internal fun formatNumberPadSpace(
    width: Int,
    output: Appendable,
    value: UInt,
): Result<Int> {
    var bytes = 0
    val padCount = (width - value.numDigits().toInt()).coerceAtLeast(0)
    for (i in 0 until padCount) {
        bytes += write(output, " ").getOrElse { return Result.failure(it) }
    }
    bytes += write(output, value.toString()).getOrElse { return Result.failure(it) }
    return Result.success(bytes)
}

internal fun formatNumberPadZero(
    width: Int,
    output: Appendable,
    value: UInt,
): Result<Int> {
    var bytes = 0
    val padCount = (width - value.numDigits().toInt()).coerceAtLeast(0)
    for (i in 0 until padCount) {
        bytes += write(output, "0").getOrElse { return Result.failure(it) }
    }
    bytes += write(output, value.toString()).getOrElse { return Result.failure(it) }
    return Result.success(bytes)
}

internal fun formatNumberPadNone(
    output: Appendable,
    value: UInt,
): Result<Int> = write(output, value.toString())

internal fun formatNumberPadNoneULong(
    output: Appendable,
    value: ULong,
): Result<Int> = write(output, value.toString())

internal fun <V, State> formatComponent(
    output: Appendable,
    component: Component,
    value: V,
    provider: ComponentProvider<V, State>,
    state: State,
): Result<Int> =
    when (component) {
        is Component.Day -> {
            if (!provider.suppliesDate) Result.failure(Format.InsufficientTypeInformation())
            else fmtDay(output, provider.day(value, state), component.modifier)
        }
        is Component.Month -> {
            if (!provider.suppliesDate) Result.failure(Format.InsufficientTypeInformation())
            else fmtMonth(output, provider.month(value, state), component.modifier)
        }
        is Component.Ordinal -> {
            if (!provider.suppliesDate) Result.failure(Format.InsufficientTypeInformation())
            else fmtOrdinal(output, provider.ordinal(value, state), component.modifier)
        }
        is Component.Weekday -> {
            if (!provider.suppliesDate) Result.failure(Format.InsufficientTypeInformation())
            else fmtWeekday(output, provider.weekday(value, state), component.modifier)
        }
        is Component.WeekNumber -> {
            if (!provider.suppliesDate) Result.failure(Format.InsufficientTypeInformation())
            else {
                val weekNum =
                    when (component.modifier.repr) {
                        WeekNumberRepr.Iso -> provider.isoWeekNumber(value, state)
                        WeekNumberRepr.Sunday -> provider.sundayBasedWeek(value, state)
                        WeekNumberRepr.Monday -> provider.mondayBasedWeek(value, state)
                    }
                fmtWeekNumber(output, weekNum, component.modifier)
            }
        }
        is Component.Year -> {
            if (!provider.suppliesDate) Result.failure(Format.InsufficientTypeInformation())
            else {
                val yr =
                    if (component.modifier.isoWeekBased) {
                        provider.isoYear(value, state)
                    } else {
                        provider.calendarYear(value, state)
                    }
                fmtYear(output, yr, component.modifier)
            }
        }
        is Component.Hour -> {
            if (!provider.suppliesTime) Result.failure(Format.InsufficientTypeInformation())
            else fmtHour(output, provider.hour(value, state), component.modifier)
        }
        is Component.Minute -> {
            if (!provider.suppliesTime) Result.failure(Format.InsufficientTypeInformation())
            else fmtMinute(output, provider.minute(value, state), component.modifier)
        }
        is Component.Period -> {
            if (!provider.suppliesTime) Result.failure(Format.InsufficientTypeInformation())
            else fmtPeriod(output, provider.period(value, state), component.modifier)
        }
        is Component.Second -> {
            if (!provider.suppliesTime) Result.failure(Format.InsufficientTypeInformation())
            else fmtSecond(output, provider.second(value, state), component.modifier)
        }
        is Component.Subsecond -> {
            if (!provider.suppliesTime) Result.failure(Format.InsufficientTypeInformation())
            else fmtSubsecond(output, provider.nanosecond(value, state), component.modifier)
        }
        is Component.OffsetHour -> {
            if (!provider.suppliesOffset) Result.failure(Format.InsufficientTypeInformation())
            else
                fmtOffsetHour(
                    output,
                    provider.offsetIsNegative(value, state),
                    provider.offsetHour(value, state),
                    component.modifier,
                )
        }
        is Component.OffsetMinute -> {
            if (!provider.suppliesOffset) Result.failure(Format.InsufficientTypeInformation())
            else fmtOffsetMinute(output, provider.offsetMinute(value, state), component.modifier)
        }
        is Component.OffsetSecond -> {
            if (!provider.suppliesOffset) Result.failure(Format.InsufficientTypeInformation())
            else fmtOffsetSecond(output, provider.offsetSecond(value, state), component.modifier)
        }
        is Component.Ignore -> Result.success(0)
        is Component.UnixTimestamp -> {
            if (!provider.suppliesTimestamp) Result.failure(Format.InsufficientTypeInformation())
            else
                when (component.modifier.precision) {
                    UnixTimestampPrecision.Second ->
                        fmtUnixTimestampSeconds(
                            output,
                            provider.unixTimestampSeconds(value, state),
                            component.modifier,
                        )
                    UnixTimestampPrecision.Millisecond ->
                        fmtUnixTimestampMilliseconds(
                            output,
                            provider.unixTimestampMilliseconds(value, state),
                            component.modifier,
                        )
                    UnixTimestampPrecision.Microsecond ->
                        fmtUnixTimestampMicroseconds(
                            output,
                            provider.unixTimestampMicroseconds(value, state),
                            component.modifier,
                        )
                    UnixTimestampPrecision.Nanosecond ->
                        fmtUnixTimestampNanoseconds(
                            output,
                            provider.unixTimestampNanoseconds(value, state),
                            component.modifier,
                        )
                }
        }
        is Component.End -> Result.success(0)
    }

internal fun fmtDay(output: Appendable, day: UByte, modifier: Day): Result<Int> =
    formatNumber(2, output, day.toUInt(), modifier.padding)

internal fun fmtMonth(
    output: Appendable,
    month: Month,
    modifier: io.github.kotlinmania.time.formatdescription.modifier.Month,
): Result<Int> =
    when (modifier.repr) {
        MonthRepr.Numerical -> formatNumber(2, output, month.number.toUInt(), modifier.padding)
        MonthRepr.Long -> write(output, MONTH_NAMES[month.number - 1])
        MonthRepr.Short -> write(output, MONTH_NAMES[month.number - 1].substring(0, 3))
    }

internal fun fmtOrdinal(output: Appendable, ordinal: UShort, modifier: Ordinal): Result<Int> =
    formatNumber(3, output, ordinal.toUInt(), modifier.padding)

internal fun fmtWeekday(
    output: Appendable,
    weekday: Weekday,
    modifier: io.github.kotlinmania.time.formatdescription.modifier.Weekday,
): Result<Int> =
    when (modifier.repr) {
        WeekdayRepr.Short ->
            write(output, WEEKDAY_NAMES[weekday.numberDaysFromMonday()].substring(0, 3))
        WeekdayRepr.Long ->
            write(output, WEEKDAY_NAMES[weekday.numberDaysFromMonday()])
        WeekdayRepr.Sunday ->
            formatNumber(
                1,
                output,
                (weekday.numberDaysFromSunday() + if (modifier.oneIndexed) 1 else 0).toUInt(),
                Padding.None,
            )
        WeekdayRepr.Monday ->
            formatNumber(
                1,
                output,
                (weekday.numberDaysFromMonday() + if (modifier.oneIndexed) 1 else 0).toUInt(),
                Padding.None,
            )
    }

internal fun fmtWeekNumber(
    output: Appendable,
    weekNumber: UByte,
    modifier: WeekNumber,
): Result<Int> = formatNumber(2, output, weekNumber.toUInt(), modifier.padding)

internal fun fmtYear(
    output: Appendable,
    fullYear: Int,
    modifier: Year,
): Result<Int> {
    val value =
        when (modifier.repr) {
            YearRepr.Full -> fullYear
            YearRepr.Century -> fullYear / 100
            YearRepr.LastTwo -> abs(fullYear % 100)
        }
    val width =
        if (modifier.range == YearRange.Extended) {
            when (modifier.repr) {
                YearRepr.Full ->
                    when {
                        abs(value) >= 100_000 -> 6
                        abs(value) >= 10_000 -> 5
                        else -> 4
                    }
                YearRepr.Century ->
                    when {
                        abs(value) >= 1_000 -> 4
                        abs(value) >= 100 -> 3
                        else -> 2
                    }
                YearRepr.LastTwo -> 2
            }
        } else {
            if ((modifier.repr == YearRepr.Full || modifier.repr == YearRepr.Century) &&
                abs(fullYear) >= 10_000
            ) {
                return Result.failure(Format.from(ComponentRange.conditional("year")))
            }
            when (modifier.repr) {
                YearRepr.Full -> 4
                YearRepr.Century -> 2
                YearRepr.LastTwo -> 2
            }
        }
    var bytes = 0
    if (modifier.repr != YearRepr.LastTwo) {
        if (fullYear < 0) {
            bytes += write(output, "-").getOrElse { return Result.failure(it) }
        } else if (modifier.signIsMandatory || fullYear >= 10_000) {
            bytes += write(output, "+").getOrElse { return Result.failure(it) }
        }
    }
    bytes +=
        formatNumber(width, output, abs(value).toUInt(), modifier.padding).getOrElse {
            return Result.failure(it)
        }
    return Result.success(bytes)
}

internal fun fmtHour(output: Appendable, hour: UByte, modifier: Hour): Result<Int> {
    val h = hour.toInt()
    val value =
        when {
            !modifier.is12HourClock -> h
            h == 0 || h == 12 -> 12
            h < 12 -> h
            else -> h - 12
        }
    return formatNumber(2, output, value.toUInt(), modifier.padding)
}

internal fun fmtMinute(output: Appendable, minute: UByte, modifier: Minute): Result<Int> =
    formatNumber(2, output, minute.toUInt(), modifier.padding)

internal fun fmtPeriod(output: Appendable, period: PeriodCase, modifier: Period): Result<Int> =
    write(
        output,
        when (period) {
            PeriodCase.Am -> if (modifier.isUppercase) "AM" else "am"
            PeriodCase.Pm -> if (modifier.isUppercase) "PM" else "pm"
        },
    )

internal fun fmtSecond(output: Appendable, second: UByte, modifier: Second): Result<Int> =
    formatNumber(2, output, second.toUInt(), modifier.padding)

internal fun fmtSubsecond(
    output: Appendable,
    nanos: UInt,
    modifier: Subsecond,
): Result<Int> {
    val digits = modifier.digits
    return if (digits == SubsecondDigits.Nine || (digits == SubsecondDigits.OneOrMore && nanos % 10u != 0u)) {
        formatNumberPadZero(9, output, nanos)
    } else if (digits == SubsecondDigits.Eight || (digits == SubsecondDigits.OneOrMore && (nanos / 10u) % 10u != 0u)) {
        formatNumberPadZero(8, output, nanos / 10u)
    } else if (digits == SubsecondDigits.Seven || (digits == SubsecondDigits.OneOrMore && (nanos / 100u) % 10u != 0u)) {
        formatNumberPadZero(7, output, nanos / 100u)
    } else if (digits == SubsecondDigits.Six || (digits == SubsecondDigits.OneOrMore && (nanos / 1_000u) % 10u != 0u)) {
        formatNumberPadZero(6, output, nanos / 1_000u)
    } else if (digits == SubsecondDigits.Five || (digits == SubsecondDigits.OneOrMore && (nanos / 10_000u) % 10u != 0u)) {
        formatNumberPadZero(5, output, nanos / 10_000u)
    } else if (digits == SubsecondDigits.Four || (digits == SubsecondDigits.OneOrMore && (nanos / 100_000u) % 10u != 0u)) {
        formatNumberPadZero(4, output, nanos / 100_000u)
    } else if (digits == SubsecondDigits.Three || (digits == SubsecondDigits.OneOrMore && (nanos / 1_000_000u) % 10u != 0u)) {
        formatNumberPadZero(3, output, nanos / 1_000_000u)
    } else if (digits == SubsecondDigits.Two || (digits == SubsecondDigits.OneOrMore && (nanos / 10_000_000u) % 10u != 0u)) {
        formatNumberPadZero(2, output, nanos / 10_000_000u)
    } else {
        formatNumberPadZero(1, output, nanos / 100_000_000u)
    }
}

internal fun fmtOffsetSign(
    output: Appendable,
    isNegative: Boolean,
    signIsMandatory: Boolean,
): Result<Int> =
    when {
        isNegative -> write(output, "-")
        signIsMandatory -> write(output, "+")
        else -> Result.success(0)
    }

internal fun fmtOffsetHour(
    output: Appendable,
    isNegative: Boolean,
    hour: Byte,
    modifier: OffsetHour,
): Result<Int> {
    var bytes = 0
    bytes += fmtOffsetSign(output, isNegative, modifier.signIsMandatory).getOrElse { return Result.failure(it) }
    bytes += formatNumber(2, output, abs(hour.toInt()).toUInt(), modifier.padding).getOrElse { return Result.failure(it) }
    return Result.success(bytes)
}

internal fun fmtOffsetMinute(
    output: Appendable,
    offsetMinute: Byte,
    modifier: OffsetMinute,
): Result<Int> = formatNumber(2, output, abs(offsetMinute.toInt()).toUInt(), modifier.padding)

internal fun fmtOffsetSecond(
    output: Appendable,
    offsetSecond: Byte,
    modifier: OffsetSecond,
): Result<Int> = formatNumber(2, output, abs(offsetSecond.toInt()).toUInt(), modifier.padding)

private fun Long.unsignedAbs(): ULong =
    if (this < 0L) (0uL - this.toULong()) else this.toULong()

internal fun fmtUnixTimestampSeconds(
    output: Appendable,
    timestamp: Long,
    modifier: UnixTimestamp,
): Result<Int> {
    var bytes = 0
    bytes += fmtOffsetSign(output, timestamp < 0L, modifier.signIsMandatory).getOrElse { return Result.failure(it) }
    bytes += formatNumberPadNoneULong(output, timestamp.unsignedAbs()).getOrElse { return Result.failure(it) }
    return Result.success(bytes)
}

internal fun fmtUnixTimestampMilliseconds(
    output: Appendable,
    timestampMillis: Long,
    modifier: UnixTimestamp,
): Result<Int> {
    var bytes = 0
    bytes += fmtOffsetSign(output, timestampMillis < 0L, modifier.signIsMandatory).getOrElse { return Result.failure(it) }
    bytes += formatNumberPadNoneULong(output, timestampMillis.unsignedAbs()).getOrElse { return Result.failure(it) }
    return Result.success(bytes)
}

internal fun fmtUnixTimestampMicroseconds(
    output: Appendable,
    timestampMicros: Pair<Boolean, String>,
    modifier: UnixTimestamp,
): Result<Int> {
    var bytes = 0
    bytes += fmtOffsetSign(output, timestampMicros.first, modifier.signIsMandatory).getOrElse { return Result.failure(it) }
    bytes += write(output, timestampMicros.second).getOrElse { return Result.failure(it) }
    return Result.success(bytes)
}

internal fun fmtUnixTimestampNanoseconds(
    output: Appendable,
    timestampNanos: Pair<Boolean, String>,
    modifier: UnixTimestamp,
): Result<Int> {
    var bytes = 0
    bytes += fmtOffsetSign(output, timestampNanos.first, modifier.signIsMandatory).getOrElse { return Result.failure(it) }
    bytes += write(output, timestampNanos.second).getOrElse { return Result.failure(it) }
    return Result.success(bytes)
}
