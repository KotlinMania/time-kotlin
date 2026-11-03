// port-lint: source interop/js_sys_date_offsetdatetime.rs
package io.github.kotlinmania.time.interop

import io.github.kotlinmania.time.OffsetDateTime

/**
 * Lightweight representation of a JavaScript `Date` millisecond epoch value and timezone offset.
 */
data class JsSysDate(
    private val timeMillis: Double,
    private val timezoneOffsetMinutes: Double = 0.0,
) {
    fun getTime(): Double = timeMillis

    fun getTimezoneOffset(): Double = timezoneOffsetMinutes

    companion object {
        fun newWithTime(timeMillis: Double): JsSysDate = JsSysDate(timeMillis)

        fun fromOffsetDateTime(datetime: OffsetDateTime): JsSysDate =
            JsSysDate(
                timeMillis = datetime.toJsDateMillis(),
                timezoneOffsetMinutes = (-datetime.offset().wholeSeconds() / 60).toDouble(),
            )
    }
}

/**
 * Construct an [OffsetDateTime] from a JavaScript `Date.getTime()` millisecond value (`Double`).
 */
fun OffsetDateTime.Companion.fromJsDateMillis(jsTimeMillis: Double): OffsetDateTime {
    val millisLong = jsTimeMillis.toLong()
    val timestampNanos = millisLong * 1_000_000L
    return fromUnixTimestampNanos(timestampNanos).getOrElse {
        throw IllegalArgumentException("invalid timestamp: Timestamp cannot fit in range", it)
    }
}

/**
 * Convert an [OffsetDateTime] to a JavaScript `Date.getTime()` millisecond value (`Double`).
 */
fun OffsetDateTime.toJsDateMillis(): Double =
    (unixTimestampNanos() / 1_000_000L).toDouble()

object JsSysDateOffsetDateTimeInterop {
    fun from(jsDate: JsSysDate): OffsetDateTime =
        OffsetDateTime.fromJsDateMillis(jsDate.getTime())

    fun from(datetime: OffsetDateTime): JsSysDate =
        JsSysDate.fromOffsetDateTime(datetime)
}
