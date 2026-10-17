// port-lint: source sys/local_offset_at/unix.rs
package io.github.kotlinmania.time.sys.localoffsetat

import io.github.kotlinmania.libc.Tm
import io.github.kotlinmania.time.Date
import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.UtcOffset

internal object Unix {
    /** Convert the given Unix timestamp to a [Tm]. Returns `null` on any error. */
    fun timestampToTm(timestamp: Long): Tm? {
        val dt = OffsetDateTime.fromUnixTimestamp(timestamp).getOrNull() ?: return null
        return Tm(
            tmSec = dt.second(),
            tmMin = dt.minute(),
            tmHour = dt.hour(),
            tmMday = dt.day(),
            tmMon = dt.month().number - 1,
            tmYear = dt.year() - 1900,
            tmWday = dt.weekday().numberDaysFromSunday(),
            tmYday = dt.ordinal() - 1,
            tmIsdst = 0,
            tmGmtoff = 0L,
            tmZone = "UTC",
        )
    }

    /** Convert a [Tm] to a [UtcOffset]. Returns `null` on any error. */
    fun tmToOffset(unixTimestamp: Long, tm: Tm): UtcOffset? {
        if (unixTimestamp == Long.MIN_VALUE) return null
        val seconds = tm.tmGmtoff.toInt()
        if (seconds.toLong() != tm.tmGmtoff) return null
        return UtcOffset.fromWholeSeconds(seconds).getOrNull()
    }

    /**
     * Convert a [Tm] to a [UtcOffset] by approximating the `tmGmtoff` field from calendar fields.
     * Returns `null` on any error.
     */
    fun tmToOffsetWithoutGmtoff(unixTimestamp: Long, tm: Tm): UtcOffset? {
        val sec = if (tm.tmSec == 60) 59 else tm.tmSec
        if (tm.tmYday < 0) return null

        val localTimestamp =
            Date.fromOrdinalDate(1900 + tm.tmYear, tm.tmYday + 1)
                .getOrNull()
                ?.withHms(tm.tmHour, tm.tmMin, sec)
                ?.getOrNull()
                ?.assumeUtc()
                ?.unixTimestamp() ?: return null

        val diff = localTimestamp - unixTimestamp
        val diffSecs = diff.toInt()
        if (diffSecs.toLong() != diff) return null

        return UtcOffset.fromWholeSeconds(diffSecs).getOrNull()
    }

    /** Obtain the system's UTC offset. */
    fun localOffsetAt(datetime: OffsetDateTime): UtcOffset? {
        val unixTimestamp = datetime.unixTimestamp()
        val tm = timestampToTm(unixTimestamp) ?: return null
        return tmToOffset(unixTimestamp, tm)
    }
}

internal fun unixLocalOffsetAt(datetime: OffsetDateTime): UtcOffset? = Unix.localOffsetAt(datetime)
