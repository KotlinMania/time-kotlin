// port-lint: source ext/systemtime.rs
package io.github.kotlinmania.time.ext

import io.github.kotlinmania.time.Duration
import kotlin.time.Instant as SystemTime

/**
 * Extension functions for [kotlin.time.Instant] (`SystemTime`) that add methods for signed [Duration]s.
 */

fun SystemTime.checkedAddSigned(duration: Duration): SystemTime? =
    try {
        when {
            duration.isPositive() -> this + duration.unsignedAbs()
            duration.isNegative() -> this - duration.unsignedAbs()
            else -> this
        }
    } catch (_: Throwable) {
        null
    }

fun SystemTime.checkedSubSigned(duration: Duration): SystemTime? =
    try {
        when {
            duration.isPositive() -> this - duration.unsignedAbs()
            duration.isNegative() -> this + duration.unsignedAbs()
            else -> this
        }
    } catch (_: Throwable) {
        null
    }

fun SystemTime.signedDurationSince(earlier: SystemTime): Duration =
    if (this >= earlier) {
        val diff = this - earlier
        if (diff.isInfinite()) {
            Duration.MAX
        } else {
            Duration.fromStdDuration(diff)
        }
    } else {
        val diff = earlier - this
        if (diff.isInfinite()) {
            Duration.MIN
        } else {
            val seconds = -diff.inWholeSeconds
            val nanoseconds = -(diff.inWholeNanoseconds % 1_000_000_000L).toInt()
            Duration.newUnchecked(seconds, nanoseconds)
        }
    }
