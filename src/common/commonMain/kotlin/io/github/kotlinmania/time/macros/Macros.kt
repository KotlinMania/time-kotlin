// port-lint: source macros.rs
package io.github.kotlinmania.time.macros

import io.github.kotlinmania.time.Date
import io.github.kotlinmania.time.Month
import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.PrimitiveDateTime
import io.github.kotlinmania.time.Time
import io.github.kotlinmania.time.UtcDateTime
import io.github.kotlinmania.time.UtcOffset
import io.github.kotlinmania.time.Weekday
import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatdescription.parse.parseStaticBorrowed

/**
 * Construct a [Date] with a statically known value.
 *
 * Three formats are supported: year-week-weekday (`2020-W01-3`), year-ordinal (`2020-001`),
 * and year-month-day (`2020-01-01`).
 */
fun date(s: String): Date {
    val trimmed = s.trim().replace(" ", "")
    val signLen = if (trimmed.startsWith('+') || trimmed.startsWith('-')) 1 else 0
    val firstDash = trimmed.indexOf('-', startIndex = signLen + 1)
    require(firstDash > 0) { "Invalid date literal: $s" }
    val year = trimmed.substring(0, firstDash).toInt()
    val rest = trimmed.substring(firstDash + 1)

    return if (rest.startsWith('W')) {
        val parts = rest.substring(1).split('-')
        require(parts.size == 2) { "Invalid ISO week date literal: $s" }
        val week = parts[0].toInt()
        val dayNum = parts[1].toInt()
        require(dayNum in 1..7) { "Invalid weekday in ISO week date literal: $s" }
        val weekday = Weekday.entries[dayNum - 1]
        Date.fromIsoWeekDate(year, week, weekday).getOrThrow()
    } else if ('-' in rest) {
        val parts = rest.split('-')
        require(parts.size == 2) { "Invalid calendar date literal: $s" }
        val month = Month.fromNumber(parts[0].toInt()).getOrThrow()
        val day = parts[1].toInt()
        Date.fromCalendarDate(year, month, day).getOrThrow()
    } else {
        val ordinal = rest.toInt()
        Date.fromOrdinalDate(year, ordinal).getOrThrow()
    }
}

/**
 * Construct a [Time] with a statically known value.
 */
fun time(s: String): Time {
    var working = s.trim()
    val lower = working.lowercase()
    val isAm: Boolean? =
        when {
            lower.endsWith("am") -> {
                working = working.substring(0, working.length - 2).trim()
                true
            }

            lower.endsWith("pm") -> {
                working = working.substring(0, working.length - 2).trim()
                false
            }

            else -> {
                null
            }
        }

    var subsecondNanos = 0
    val dotIdx = working.indexOf('.')
    if (dotIdx != -1) {
        val fracRaw = working.substring(dotIdx + 1).replace("_", "").trim()
        working = working.substring(0, dotIdx).trim()
        val padded =
            if (fracRaw.length >= 9) {
                fracRaw.substring(0, 9)
            } else {
                fracRaw.padEnd(9, '0')
            }
        subsecondNanos = padded.toInt()
    }

    val parts = working.split(':').map { it.trim() }
    val rawHour = parts[0].toInt()
    val minute = if (parts.size >= 2) parts[1].toInt() else 0
    val second = if (parts.size >= 3) parts[2].toInt() else 0

    val hour =
        when (isAm) {
            true -> {
                require(rawHour in 1..12) { "12-hour clock hour must be in 1..12" }
                if (rawHour == 12) 0 else rawHour
            }

            false -> {
                require(rawHour in 1..12) { "12-hour clock hour must be in 1..12" }
                if (rawHour == 12) 12 else rawHour + 12
            }

            null -> {
                rawHour
            }
        }

    return Time.fromHmsNano(hour, minute, second, subsecondNanos).getOrThrow()
}

/**
 * Construct a [UtcOffset] with a statically known value.
 */
fun offset(s: String): UtcOffset {
    val trimmed = s.trim().replace(" ", "")
    if (trimmed.equals("UTC", ignoreCase = true) || trimmed.equals("Z", ignoreCase = true)) {
        return UtcOffset.UTC
    }
    val isNegative =
        when {
            trimmed.startsWith('-') -> true
            trimmed.startsWith('+') -> false
            else -> throw IllegalArgumentException("Offset must start with + or - or be UTC: $s")
        }
    val parts = trimmed.substring(1).split(':')
    val h = parts[0].toInt()
    val m = if (parts.size >= 2) parts[1].toInt() else 0
    val sec = if (parts.size >= 3) parts[2].toInt() else 0
    val signMul = if (isNegative) -1 else 1
    return UtcOffset
        .fromHms(
            h * signMul,
            m * signMul,
            sec * signMul,
        ).getOrThrow()
}

/**
 * Construct a [PrimitiveDateTime] from a statically known `"date time"` string.
 */
fun primitiveDatetime(s: String): PrimitiveDateTime {
    val trimmed = s.trim()
    val spaceIdx = trimmed.indexOf(' ')
    require(spaceIdx != -1) { "Invalid datetime literal: $s" }
    val d = date(trimmed.substring(0, spaceIdx))
    val t = time(trimmed.substring(spaceIdx + 1))
    return PrimitiveDateTime.new(d, t)
}

/**
 * Construct an [OffsetDateTime] from a statically known `"date time offset"` string.
 */
fun datetime(s: String): OffsetDateTime {
    val trimmed = s.trim()
    val firstSpace = trimmed.indexOf(' ')
    require(firstSpace != -1) { "Invalid datetime literal: $s" }
    val d = date(trimmed.substring(0, firstSpace))
    val afterDate = trimmed.substring(firstSpace + 1).trim()
    val lastSpace = afterDate.lastIndexOf(' ')
    require(lastSpace != -1) { "Missing offset in datetime literal: $s" }
    val possibleOffset = afterDate.substring(lastSpace + 1).trim()
    val timeStr: String
    val offsetStr: String
    if (
        possibleOffset.equals("am", ignoreCase = true) ||
        possibleOffset.equals("pm", ignoreCase = true)
    ) {
        throw IllegalArgumentException("Missing offset in datetime literal: $s")
    } else {
        timeStr = afterDate.substring(0, lastSpace).trim()
        offsetStr = possibleOffset
    }
    val t = time(timeStr)
    val off = offset(offsetStr)
    return OffsetDateTime.newInOffset(d, t, off)
}

/**
 * Construct a [UtcDateTime] with a statically known value.
 */
fun utcDatetime(s: String): UtcDateTime {
    val pdt = primitiveDatetime(s)
    return pdt.asUtc()
}

/**
 * Parse a format description with the given version (`1` or `2`, defaulting to `1`).
 */
fun formatDescription(s: String): List<BorrowedFormatItem> =
    parseStaticBorrowed(1, s).getOrThrow()

/**
 * Parse a format description with an explicit version (`1` or `2`).
 */
fun formatDescription(version: Int, s: String): List<BorrowedFormatItem> =
    parseStaticBorrowed(version, s).getOrThrow()

fun date(year: Int, ordinal: Int): Date =
    Date.fromOrdinalDate(year, ordinal).getOrThrow()

fun date(year: Int, month: Month, day: Int): Date =
    Date.fromCalendarDate(year, month, day).getOrThrow()

fun date(year: Int, month: Int, day: Int): Date =
    Date.fromCalendarDate(year, Month.fromNumber(month).getOrThrow(), day).getOrThrow()

fun date(year: Int, week: UInt, weekday: Weekday): Date =
    Date.fromIsoWeekDate(year, week.toInt(), weekday).getOrThrow()

fun date(year: Int, week: Int, weekday: Weekday): Date =
    Date.fromIsoWeekDate(year, week, weekday).getOrThrow()

fun time(
    hour: Int,
    minute: Int,
    second: Int = 0,
    nanosecond: Int = 0,
): Time = Time.fromHmsNano(hour, minute, second, nanosecond).getOrThrow()

fun offset(
    hours: Int,
    minutes: Int = 0,
    seconds: Int = 0,
): UtcOffset = UtcOffset.fromHms(hours, minutes, seconds).getOrThrow()

fun datetime(
    year: Int,
    month: Month,
    day: Int,
    hour: Int,
    minute: Int,
    second: Int = 0,
    nanosecond: Int = 0,
): PrimitiveDateTime =
    PrimitiveDateTime.new(date(year, month, day), time(hour, minute, second, nanosecond))

fun datetime(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int,
    second: Int = 0,
    nanosecond: Int = 0,
): PrimitiveDateTime =
    PrimitiveDateTime.new(date(year, month, day), time(hour, minute, second, nanosecond))

fun datetime(
    year: Int,
    month: Month,
    day: Int,
    hour: Int,
    minute: Int,
    second: Int,
    nanosecond: Int,
    offsetHours: Int,
    offsetMinutes: Int = 0,
    offsetSeconds: Int = 0,
): OffsetDateTime =
    datetime(year, month, day, hour, minute, second, nanosecond)
        .assumeOffset(offset(offsetHours, offsetMinutes, offsetSeconds))

fun datetime(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int,
    second: Int,
    nanosecond: Int,
    offsetHours: Int,
    offsetMinutes: Int = 0,
    offsetSeconds: Int = 0,
): OffsetDateTime =
    datetime(year, month, day, hour, minute, second, nanosecond)
        .assumeOffset(offset(offsetHours, offsetMinutes, offsetSeconds))

fun utcDatetime(
    year: Int,
    month: Month,
    day: Int,
    hour: Int,
    minute: Int,
    second: Int = 0,
    nanosecond: Int = 0,
): UtcDateTime =
    UtcDateTime.new(date(year, month, day), time(hour, minute, second, nanosecond))

fun utcDatetime(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int,
    second: Int = 0,
    nanosecond: Int = 0,
): UtcDateTime =
    UtcDateTime.new(date(year, month, day), time(hour, minute, second, nanosecond))
