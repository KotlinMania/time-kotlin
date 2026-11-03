// port-lint: source interop/offsetdatetime_utcdatetime.rs
package io.github.kotlinmania.time.interop

import io.github.kotlinmania.time.Duration
import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.UtcDateTime

operator fun OffsetDateTime.minus(rhs: UtcDateTime): Duration =
    this - OffsetDateTime.from(rhs)

fun UtcDateTime.compareTo(other: OffsetDateTime): Int =
    OffsetDateTime.from(this).compareTo(other)

fun OffsetDateTime.compareTo(other: UtcDateTime): Int =
    this.compareTo(OffsetDateTime.from(other))

object OffsetDateTimeUtcDateTimeInterop {
    fun sub(lhs: OffsetDateTime, rhs: UtcDateTime): Duration = lhs - OffsetDateTime.from(rhs)

    fun sub(lhs: UtcDateTime, rhs: OffsetDateTime): Duration = OffsetDateTime.from(lhs) - rhs

    fun eq(lhs: OffsetDateTime, rhs: UtcDateTime): Boolean = lhs == OffsetDateTime.from(rhs)

    fun eq(lhs: UtcDateTime, rhs: OffsetDateTime): Boolean = OffsetDateTime.from(lhs) == rhs

    fun partialCmp(lhs: OffsetDateTime, rhs: UtcDateTime): Int = lhs.compareTo(OffsetDateTime.from(rhs))

    fun partialCmp(lhs: UtcDateTime, rhs: OffsetDateTime): Int = OffsetDateTime.from(lhs).compareTo(rhs)

    fun from(datetime: UtcDateTime): OffsetDateTime = OffsetDateTime.from(datetime)

    fun from(datetime: OffsetDateTime): UtcDateTime = UtcDateTime.from(datetime)
}
