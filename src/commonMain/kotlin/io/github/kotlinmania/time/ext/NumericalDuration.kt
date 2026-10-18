// port-lint: source ext/numerical_duration.rs
package io.github.kotlinmania.time.ext

import io.github.kotlinmania.time.Duration

/**
 * Extension functions to create [Duration]s from numeric literals.
 */

fun Long.nanoseconds(): Duration = Duration.nanoseconds(this)

fun Long.microseconds(): Duration = Duration.microseconds(this)

fun Long.milliseconds(): Duration = Duration.milliseconds(this)

fun Long.seconds(): Duration = Duration.seconds(this)

fun Long.minutes(): Duration = Duration.minutes(this)

fun Long.hours(): Duration = Duration.hours(this)

fun Long.days(): Duration = Duration.days(this)

fun Long.weeks(): Duration = Duration.weeks(this)

fun Int.nanoseconds(): Duration = toLong().nanoseconds()

fun Int.microseconds(): Duration = toLong().microseconds()

fun Int.milliseconds(): Duration = toLong().milliseconds()

fun Int.seconds(): Duration = toLong().seconds()

fun Int.minutes(): Duration = toLong().minutes()

fun Int.hours(): Duration = toLong().hours()

fun Int.days(): Duration = toLong().days()

fun Int.weeks(): Duration = toLong().weeks()

fun ULong.nanoseconds(): Duration = toLong().nanoseconds()

fun ULong.microseconds(): Duration = toLong().microseconds()

fun ULong.milliseconds(): Duration = toLong().milliseconds()

fun ULong.seconds(): Duration = toLong().seconds()

fun ULong.minutes(): Duration = toLong().minutes()

fun ULong.hours(): Duration = toLong().hours()

fun ULong.days(): Duration = toLong().days()

fun ULong.weeks(): Duration = toLong().weeks()

fun Double.nanoseconds(): Duration = Duration.nanoseconds(this.toLong())

fun Double.microseconds(): Duration = Duration.nanoseconds((this * 1_000.0).toLong())

fun Double.milliseconds(): Duration = Duration.nanoseconds((this * 1_000_000.0).toLong())

fun Double.seconds(): Duration = Duration.nanoseconds((this * 1_000_000_000.0).toLong())

fun Double.minutes(): Duration = Duration.nanoseconds((this * 60_000_000_000.0).toLong())

fun Double.hours(): Duration = Duration.nanoseconds((this * 3_600_000_000_000.0).toLong())

fun Double.days(): Duration = Duration.nanoseconds((this * 86_400_000_000_000.0).toLong())

fun Double.weeks(): Duration = Duration.nanoseconds((this * 604_800_000_000_000.0).toLong())

fun Float.seconds(): Duration = Duration.secondsF32(this)
