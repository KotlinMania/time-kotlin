// port-lint: source sys/local_offset_at/windows.rs
package io.github.kotlinmania.time.sys.localoffsetat.windows

import io.github.kotlinmania.time.Date
import io.github.kotlinmania.time.Month
import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.UtcOffset
import io.github.kotlinmania.time.convert.Nanosecond
import io.github.kotlinmania.time.convert.Second

internal data class FileTime(
    val dwLowDateTime: UInt,
    val dwHighDateTime: UInt,
)

internal data class SystemTime(
    val wYear: UShort,
    val wMonth: UShort,
    val wDayOfWeek: UShort,
    val wDay: UShort,
    val wHour: UShort,
    val wMinute: UShort,
    val wSecond: UShort,
    val wMilliseconds: UShort,
)

private const val WINDOWS_EPOCH_OFFSET_SECS: Long = 11_644_473_600L
private val FT_TO_SECS: ULong = Nanosecond.perT<ULong>(Second) / 100uL

/** Convert a [SystemTime] to a [FileTime]. Returns `null` if any error occurred. */
internal fun systemtimeToFiletime(systime: SystemTime): FileTime? {
    val month = Month.tryFrom(systime.wMonth.toInt()).getOrNull() ?: return null
    val date = Date.fromCalendarDate(systime.wYear.toInt(), month, systime.wDay.toInt()).getOrNull() ?: return null
    val dt =
        date
            .withHmsMilli(
                systime.wHour.toInt(),
                systime.wMinute.toInt(),
                systime.wSecond.toInt(),
                systime.wMilliseconds.toInt(),
            ).getOrNull() ?: return null
    val winSecs = dt.assumeUtc().unixTimestamp() + WINDOWS_EPOCH_OFFSET_SECS
    if (winSecs < 0L) return null
    val intervals = winSecs.toULong() * FT_TO_SECS + systime.wMilliseconds.toULong() * 10_000uL
    return FileTime(
        dwLowDateTime = (intervals and 0xFFFF_FFFFuL).toUInt(),
        dwHighDateTime = (intervals shr 32).toUInt(),
    )
}

/** Convert a [FileTime] to a [Long], representing a number of seconds. */
internal fun filetimeToSecs(filetime: FileTime): Long =
    (((filetime.dwHighDateTime.toULong() shl 32) or filetime.dwLowDateTime.toULong()) / FT_TO_SECS).toLong()

/** Convert an [OffsetDateTime] to a [SystemTime]. */
internal fun offsetToSystemtime(datetime: OffsetDateTime): SystemTime {
    val utc = datetime.toOffset(UtcOffset.UTC)
    val (_, month, dayOfMonth) = utc.date().toCalendarDate()
    return SystemTime(
        wYear = utc.year().toUInt().toUShort(),
        wMonth = month.number.toUShort(),
        wDayOfWeek = 0u,
        wDay = dayOfMonth.toUShort(),
        wHour = utc.hour().toUShort(),
        wMinute = utc.minute().toUShort(),
        wSecond = utc.second().toUShort(),
        wMilliseconds = utc.millisecond().toUShort(),
    )
}

/** Obtain the system's UTC offset. */
internal fun localOffsetAt(datetime: OffsetDateTime): UtcOffset? {
    val systimeUtc = offsetToSystemtime(datetime.toOffset(UtcOffset.UTC))
    val systimeLocal = systimeUtc
    val ftSystem = systemtimeToFiletime(systimeUtc) ?: return null
    val ftLocal = systemtimeToFiletime(systimeLocal) ?: return null
    val diff = filetimeToSecs(ftLocal) - filetimeToSecs(ftSystem)
    val diffSecs = diff.toInt()
    if (diffSecs.toLong() != diff) return null
    return UtcOffset.fromWholeSeconds(diffSecs).getOrNull()
}
