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

fun Double.seconds(): Duration = Duration.secondsF64(this)
fun Float.seconds(): Duration = Duration.secondsF32(this)

fun Double.nanoseconds(): Duration = Duration.secondsF64(this / 1_000_000_000.0)
fun Double.microseconds(): Duration = Duration.secondsF64(this / 1_000_000.0)
fun Double.milliseconds(): Duration = Duration.secondsF64(this / 1_000.0)
fun Double.minutes(): Duration = Duration.secondsF64(this * 60.0)
fun Double.hours(): Duration = Duration.secondsF64(this * 3_600.0)
fun Double.days(): Duration = Duration.secondsF64(this * 86_400.0)
fun Double.weeks(): Duration = Duration.secondsF64(this * 604_800.0)
