// port-lint: source interop/utcdatetime_systemtime.rs
package io.github.kotlinmania.time.interop

import io.github.kotlinmania.time.Duration
import io.github.kotlinmania.time.UtcDateTime
import kotlin.time.Instant as SystemTime

operator fun SystemTime.minus(rhs: UtcDateTime): Duration =
    UtcDateTime.from(this) - rhs

fun UtcDateTime.equalsSystemTime(rhs: SystemTime): Boolean =
    this == UtcDateTime.from(rhs)

fun SystemTime.equalsUtcDateTime(rhs: UtcDateTime): Boolean =
    UtcDateTime.from(this) == rhs

fun UtcDateTime.compareTo(other: SystemTime): Int =
    this.compareTo(UtcDateTime.from(other))

fun SystemTime.compareTo(other: UtcDateTime): Int =
    UtcDateTime.from(this).compareTo(other)

object UtcDateTimeSystemTimeInterop {

    fun sub(lhs: UtcDateTime, rhs: SystemTime): Duration = lhs - UtcDateTime.from(rhs)

    fun sub(lhs: SystemTime, rhs: UtcDateTime): Duration = UtcDateTime.from(lhs) - rhs

    fun eq(lhs: UtcDateTime, rhs: SystemTime): Boolean = lhs == UtcDateTime.from(rhs)

    fun eq(lhs: SystemTime, rhs: UtcDateTime): Boolean = UtcDateTime.from(lhs) == rhs

    fun partialCmp(lhs: UtcDateTime, rhs: SystemTime): Int = lhs.compareTo(UtcDateTime.from(rhs))

    fun partialCmp(lhs: SystemTime, rhs: UtcDateTime): Int = UtcDateTime.from(lhs).compareTo(rhs)

    fun from(systemTime: SystemTime): UtcDateTime = UtcDateTime.from(systemTime)

    fun from(datetime: UtcDateTime): SystemTime = datetime.toSystemTime()
}
