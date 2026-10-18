// port-lint: source formatting/iso8601.rs
package io.github.kotlinmania.time.formatting

import io.github.kotlinmania.time.error.Format
import io.github.kotlinmania.time.formatdescription.wellknown.DateKind
import io.github.kotlinmania.time.formatdescription.wellknown.Iso8601
import io.github.kotlinmania.time.formatdescription.wellknown.OffsetPrecision
import io.github.kotlinmania.time.formatdescription.wellknown.TimePrecision
import io.github.kotlinmania.time.formatdescription.wellknown.iso8601.dateKind
import io.github.kotlinmania.time.formatdescription.wellknown.iso8601.formatDate
import io.github.kotlinmania.time.formatdescription.wellknown.iso8601.formatTime
import io.github.kotlinmania.time.formatdescription.wellknown.iso8601.offsetPrecision
import io.github.kotlinmania.time.formatdescription.wellknown.iso8601.timePrecision
import io.github.kotlinmania.time.formatdescription.wellknown.iso8601.useSeparators
import io.github.kotlinmania.time.formatdescription.wellknown.iso8601.yearIsSixDigits
import kotlin.math.abs

/**
 * Helpers for implementing formatting for ISO 8601.
 */

/** Format the date portion of ISO 8601. */
internal fun <V, State> formatIso8601Date(
    iso: Iso8601,
    output: Appendable,
    value: V,
    provider: ComponentProvider<V, State>,
    state: State,
): Result<Int> {
    var bytes = 0

    when (iso.dateKind) {
        DateKind.Calendar -> {
            val year = provider.calendarYear(value, state)
            if (iso.yearIsSixDigits) {
                bytes += writeIfElse(output, year < 0, "-", "+").getOrElse { return Result.failure(it) }
                bytes += formatNumberPadZero(6, output, abs(year).toUInt()).getOrElse { return Result.failure(it) }
            } else if (year !in 0..9999) {
                return Result.failure(Format.InvalidComponent("year"))
            } else {
                bytes += formatNumberPadZero(4, output, year.toUInt()).getOrElse { return Result.failure(it) }
            }
            bytes += writeIf(output, iso.useSeparators, "-").getOrElse { return Result.failure(it) }
            bytes += formatNumberPadZero(2, output, provider.month(value, state).number.toUInt()).getOrElse { return Result.failure(it) }
            bytes += writeIf(output, iso.useSeparators, "-").getOrElse { return Result.failure(it) }
            bytes += formatNumberPadZero(2, output, provider.day(value, state).toUInt()).getOrElse { return Result.failure(it) }
        }
        DateKind.Week -> {
            val year = provider.isoYear(value, state)
            if (iso.yearIsSixDigits) {
                bytes += writeIfElse(output, year < 0, "-", "+").getOrElse { return Result.failure(it) }
                bytes += formatNumberPadZero(6, output, abs(year).toUInt()).getOrElse { return Result.failure(it) }
            } else if (year !in 0..9999) {
                return Result.failure(Format.InvalidComponent("year"))
            } else {
                bytes += formatNumberPadZero(4, output, year.toUInt()).getOrElse { return Result.failure(it) }
            }
            bytes += writeIfElse(output, iso.useSeparators, "-W", "W").getOrElse { return Result.failure(it) }
            bytes += formatNumberPadZero(2, output, provider.isoWeekNumber(value, state).toUInt()).getOrElse { return Result.failure(it) }
            bytes += writeIf(output, iso.useSeparators, "-").getOrElse { return Result.failure(it) }
            bytes += formatNumberPadZero(1, output, provider.weekday(value, state).numberFromMonday().toUInt()).getOrElse { return Result.failure(it) }
        }
        DateKind.Ordinal -> {
            val year = provider.calendarYear(value, state)
            if (iso.yearIsSixDigits) {
                bytes += writeIfElse(output, year < 0, "-", "+").getOrElse { return Result.failure(it) }
                bytes += formatNumberPadZero(6, output, abs(year).toUInt()).getOrElse { return Result.failure(it) }
            } else if (year !in 0..9999) {
                return Result.failure(Format.InvalidComponent("year"))
            } else {
                bytes += formatNumberPadZero(4, output, year.toUInt()).getOrElse { return Result.failure(it) }
            }
            bytes += writeIf(output, iso.useSeparators, "-").getOrElse { return Result.failure(it) }
            bytes += formatNumberPadZero(3, output, provider.ordinal(value, state).toUInt()).getOrElse { return Result.failure(it) }
        }
    }

    return Result.success(bytes)
}

/** Format the time portion of ISO 8601. */
internal fun <V, State> formatIso8601Time(
    iso: Iso8601,
    output: Appendable,
    value: V,
    provider: ComponentProvider<V, State>,
    state: State,
): Result<Int> {
    var bytes = 0

    // The "T" can only be omitted in extended format where there is no date being formatted.
    bytes += writeIf(output, iso.useSeparators || iso.formatDate, "T").getOrElse { return Result.failure(it) }

    when (val tp = iso.timePrecision) {
        is TimePrecision.Hour -> {
            val hours =
                provider.hour(value, state).toDouble() +
                    provider.minute(value, state).toDouble() / 60.0 +
                    provider.second(value, state).toDouble() / 3_600.0 +
                    provider.nanosecond(value, state).toDouble() / 3_600_000_000_000.0
            bytes += formatFloat(output, hours, 2u, tp.decimalDigits).getOrElse { return Result.failure(it) }
        }
        is TimePrecision.Minute -> {
            bytes += formatNumberPadZero(2, output, provider.hour(value, state).toUInt()).getOrElse { return Result.failure(it) }
            bytes += writeIf(output, iso.useSeparators, ":").getOrElse { return Result.failure(it) }
            val minutes =
                provider.minute(value, state).toDouble() +
                    provider.second(value, state).toDouble() / 60.0 +
                    provider.nanosecond(value, state).toDouble() / 60_000_000_000.0
            bytes += formatFloat(output, minutes, 2u, tp.decimalDigits).getOrElse { return Result.failure(it) }
        }
        is TimePrecision.Second -> {
            bytes += formatNumberPadZero(2, output, provider.hour(value, state).toUInt()).getOrElse { return Result.failure(it) }
            bytes += writeIf(output, iso.useSeparators, ":").getOrElse { return Result.failure(it) }
            bytes += formatNumberPadZero(2, output, provider.minute(value, state).toUInt()).getOrElse { return Result.failure(it) }
            bytes += writeIf(output, iso.useSeparators, ":").getOrElse { return Result.failure(it) }
            val seconds =
                provider.second(value, state).toDouble() +
                    provider.nanosecond(value, state).toDouble() / 1_000_000_000.0
            bytes += formatFloat(output, seconds, 2u, tp.decimalDigits).getOrElse { return Result.failure(it) }
        }
    }

    return Result.success(bytes)
}

/** Format the UTC offset portion of ISO 8601. */
internal fun <V, State> formatIso8601Offset(
    iso: Iso8601,
    output: Appendable,
    value: V,
    provider: ComponentProvider<V, State>,
    state: State,
): Result<Int> {
    if (iso.formatTime && provider.offsetIsUtc(value, state)) {
        return write(output, "Z")
    }

    var bytes = 0

    if (provider.offsetSecond(value, state).toInt() != 0) {
        return Result.failure(Format.InvalidComponent("offset_second"))
    }
    bytes += writeIfElse(output, provider.offsetIsNegative(value, state), "-", "+").getOrElse { return Result.failure(it) }
    bytes += formatNumberPadZero(2, output, abs(provider.offsetHour(value, state).toInt()).toUInt()).getOrElse { return Result.failure(it) }

    val minutes = provider.offsetMinute(value, state)

    if (iso.offsetPrecision == OffsetPrecision.Hour && minutes.toInt() != 0) {
        return Result.failure(Format.InvalidComponent("offset_minute"))
    } else if (iso.offsetPrecision == OffsetPrecision.Minute) {
        bytes += writeIf(output, iso.useSeparators, ":").getOrElse { return Result.failure(it) }
        bytes += formatNumberPadZero(2, output, abs(minutes.toInt()).toUInt()).getOrElse { return Result.failure(it) }
    }

    return Result.success(bytes)
}

internal object Iso8601Formatting {
    fun <V, State> formatDate(
        iso: Iso8601,
        output: Appendable,
        value: V,
        provider: ComponentProvider<V, State>,
        state: State,
    ): Result<Int> = formatIso8601Date(iso, output, value, provider, state)

    fun <V, State> formatTime(
        iso: Iso8601,
        output: Appendable,
        value: V,
        provider: ComponentProvider<V, State>,
        state: State,
    ): Result<Int> = formatIso8601Time(iso, output, value, provider, state)

    fun <V, State> formatOffset(
        iso: Iso8601,
        output: Appendable,
        value: V,
        provider: ComponentProvider<V, State>,
        state: State,
    ): Result<Int> = formatIso8601Offset(iso, output, value, provider, state)
}
