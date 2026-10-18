// port-lint: source interop/offsetdatetime_systemtime.rs
package io.github.kotlinmania.time.interop

import io.github.kotlinmania.time.Duration
import io.github.kotlinmania.time.OffsetDateTime
import kotlin.time.Instant as SystemTime

operator fun SystemTime.minus(rhs: OffsetDateTime): Duration =
    OffsetDateTime.from(this) - rhs

fun OffsetDateTime.equalsSystemTime(rhs: SystemTime): Boolean =
    this == OffsetDateTime.from(rhs)

fun SystemTime.equalsOffsetDateTime(rhs: OffsetDateTime): Boolean =
    OffsetDateTime.from(this) == rhs

fun OffsetDateTime.compareTo(other: SystemTime): Int =
    this.compareTo(OffsetDateTime.from(other))

fun SystemTime.compareTo(other: OffsetDateTime): Int =
    OffsetDateTime.from(this).compareTo(other)

object OffsetDateTimeSystemTimeInterop {

    fun sub(lhs: OffsetDateTime, rhs: SystemTime): Duration = lhs - OffsetDateTime.from(rhs)

    fun sub(lhs: SystemTime, rhs: OffsetDateTime): Duration = OffsetDateTime.from(lhs) - rhs

    fun eq(lhs: OffsetDateTime, rhs: SystemTime): Boolean = lhs == OffsetDateTime.from(rhs)

    fun eq(lhs: SystemTime, rhs: OffsetDateTime): Boolean = OffsetDateTime.from(lhs) == rhs

    fun partialCmp(lhs: OffsetDateTime, rhs: SystemTime): Int = lhs.compareTo(OffsetDateTime.from(rhs))

    fun partialCmp(lhs: SystemTime, rhs: OffsetDateTime): Int = OffsetDateTime.from(lhs).compareTo(rhs)

    fun from(systemTime: SystemTime): OffsetDateTime = OffsetDateTime.from(systemTime)

    fun from(datetime: OffsetDateTime): SystemTime = datetime.toSystemTime()
}
