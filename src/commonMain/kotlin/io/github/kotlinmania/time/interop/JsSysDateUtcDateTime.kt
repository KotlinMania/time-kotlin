// port-lint: source interop/js_sys_date_utcdatetime.rs
package io.github.kotlinmania.time.interop

import io.github.kotlinmania.time.UtcDateTime

/**
 * Construct a [UtcDateTime] from a JavaScript `Date.getTime()` millisecond value (`Double`).
 */
fun UtcDateTime.Companion.fromJsDateMillis(jsTimeMillis: Double): UtcDateTime {
    val timestampNanos = (jsTimeMillis * 1_000_000.0).toLong()
    return fromUnixTimestampNanos(timestampNanos).getOrElse {
        throw IllegalArgumentException("invalid timestamp: Timestamp cannot fit in range", it)
    }
}

/**
 * Convert a [UtcDateTime] to a JavaScript `Date.getTime()` millisecond value (`Double`).
 */
fun UtcDateTime.toJsDateMillis(): Double =
    (unixTimestampNanos() / 1_000_000L).toDouble()

object JsSysDateUtcDateTimeInterop {
    fun from(jsDate: JsSysDate): UtcDateTime =
        UtcDateTime.fromJsDateMillis(jsDate.getTime())

    fun from(datetime: UtcDateTime): JsSysDate =
        JsSysDate.newWithTime(datetime.toJsDateMillis())
}
