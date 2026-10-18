package io.github.kotlinmania.time

import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdetest.Compact
import io.github.kotlinmania.serdetest.Readable
import io.github.kotlinmania.serdetest.Token
import io.github.kotlinmania.serdetest.assertDeTokens
import io.github.kotlinmania.serdetest.assertSerTokens
import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem

fun date(s: String): Date = io.github.kotlinmania.time.macros.date(s)

fun date(year: Int, ordinal: Int): Date =
    io.github.kotlinmania.time.macros.date(year, ordinal)

fun date(year: Int, month: Month, day: Int): Date =
    io.github.kotlinmania.time.macros.date(year, month, day)

fun date(year: Int, month: Int, day: Int): Date =
    io.github.kotlinmania.time.macros.date(year, month, day)

fun date(year: Int, week: UInt, weekday: Weekday): Date =
    io.github.kotlinmania.time.macros.date(year, week, weekday)

fun date(year: Int, week: Int, weekday: Weekday): Date =
    io.github.kotlinmania.time.macros.date(year, week, weekday)

fun time(s: String): Time = io.github.kotlinmania.time.macros.time(s)

fun time(
    hour: Int,
    minute: Int,
    second: Int = 0,
    nanosecond: Int = 0,
): Time = io.github.kotlinmania.time.macros.time(hour, minute, second, nanosecond)

fun offset(s: String): UtcOffset = io.github.kotlinmania.time.macros.offset(s)

fun offset(
    hours: Int,
    minutes: Int = 0,
    seconds: Int = 0,
): UtcOffset = io.github.kotlinmania.time.macros.offset(hours, minutes, seconds)

fun datetime(s: String): OffsetDateTime = io.github.kotlinmania.time.macros.datetime(s)

fun datetime(
    year: Int,
    month: Month,
    day: Int,
    hour: Int,
    minute: Int,
    second: Int = 0,
    nanosecond: Int = 0,
): PrimitiveDateTime =
    io.github.kotlinmania.time.macros.datetime(year, month, day, hour, minute, second, nanosecond)

fun datetime(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int,
    second: Int = 0,
    nanosecond: Int = 0,
): PrimitiveDateTime =
    io.github.kotlinmania.time.macros.datetime(year, month, day, hour, minute, second, nanosecond)

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
    io.github.kotlinmania.time.macros.datetime(
        year,
        month,
        day,
        hour,
        minute,
        second,
        nanosecond,
        offsetHours,
        offsetMinutes,
        offsetSeconds,
    )

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
    io.github.kotlinmania.time.macros.datetime(
        year,
        month,
        day,
        hour,
        minute,
        second,
        nanosecond,
        offsetHours,
        offsetMinutes,
        offsetSeconds,
    )

fun utcDatetime(s: String): UtcDateTime = io.github.kotlinmania.time.macros.utcDatetime(s)

fun utcDatetime(
    year: Int,
    month: Month,
    day: Int,
    hour: Int,
    minute: Int,
    second: Int = 0,
    nanosecond: Int = 0,
): UtcDateTime =
    io.github.kotlinmania.time.macros.utcDatetime(year, month, day, hour, minute, second, nanosecond)

fun utcDatetime(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int,
    second: Int = 0,
    nanosecond: Int = 0,
): UtcDateTime =
    io.github.kotlinmania.time.macros.utcDatetime(year, month, day, hour, minute, second, nanosecond)

fun formatDescription(s: String): List<BorrowedFormatItem> =
    io.github.kotlinmania.time.macros.formatDescription(s)

fun formatDescription(version: Int, s: String): List<BorrowedFormatItem> =
    io.github.kotlinmania.time.macros.formatDescription(version, s)

fun <T> Result<T>.unwrap(): T = getOrThrow()

fun <T> Result<T>.unwrapErr(): Throwable =
    exceptionOrNull() ?: error("Expected failure but was $this")

val <T> Result<T>.error: Throwable
    get() = exceptionOrNull() ?: error("Expected failure but was $this")

object TimeResult {
    fun <T> Ok(value: T): Result<T> = Result.success(value)

    fun Err(err: Throwable): Result<Nothing> = Result.failure(err)
}

fun <T : Serialize> assertTokens(
    value: Compact<T>,
    deserialize: Deserialize<T>,
    tokens: List<Token>,
) {
    assertSerTokens(value, tokens)
    assertDeTokens(value.value, deserialize, tokens)
}

fun <T : Serialize> assertTokens(
    value: Readable<T>,
    deserialize: Deserialize<T>,
    tokens: List<Token>,
) {
    assertSerTokens(value, tokens)
    assertDeTokens(value.value, deserialize, tokens)
}
