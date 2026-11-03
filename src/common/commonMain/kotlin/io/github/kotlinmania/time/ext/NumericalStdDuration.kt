// port-lint: source ext/numerical_std_duration.rs
package io.github.kotlinmania.time.ext

import kotlin.time.Duration as StdDuration
import kotlin.time.Duration.Companion.microseconds as stdMicros
import kotlin.time.Duration.Companion.milliseconds as stdMillis
import kotlin.time.Duration.Companion.nanoseconds as stdNanos
import kotlin.time.Duration.Companion.seconds as stdSecs

private fun mulU64Checked(a: ULong, b: ULong): ULong {
    if (a != 0uL && b > ULong.MAX_VALUE / a) {
        throw ArithmeticException("overflow constructing `time::Duration`")
    }
    return a * b
}

private fun stdDurationFromSecsU64(secs: ULong): StdDuration =
    if (secs <= Long.MAX_VALUE.toULong()) {
        secs.toLong().stdSecs
    } else {
        StdDuration.INFINITE
    }

private fun stdDurationFromNanosU64(nanos: ULong): StdDuration {
    val secs = nanos / 1_000_000_000uL
    val remNanos = (nanos % 1_000_000_000uL).toLong()
    return stdDurationFromSecsU64(secs) + remNanos.stdNanos
}

fun ULong.stdNanoseconds(): StdDuration = stdDurationFromNanosU64(this)

fun ULong.stdMicroseconds(): StdDuration {
    val secs = this / 1_000_000uL
    val remMicros = (this % 1_000_000uL).toLong()
    return stdDurationFromSecsU64(secs) + remMicros.stdMicros
}

fun ULong.stdMilliseconds(): StdDuration {
    val secs = this / 1_000uL
    val remMillis = (this % 1_000uL).toLong()
    return stdDurationFromSecsU64(secs) + remMillis.stdMillis
}

fun ULong.stdSeconds(): StdDuration = stdDurationFromSecsU64(this)

fun ULong.stdMinutes(): StdDuration =
    stdDurationFromSecsU64(mulU64Checked(this, 60uL))

fun ULong.stdHours(): StdDuration =
    stdDurationFromSecsU64(mulU64Checked(this, 3_600uL))

fun ULong.stdDays(): StdDuration =
    stdDurationFromSecsU64(mulU64Checked(this, 86_400uL))

fun ULong.stdWeeks(): StdDuration =
    stdDurationFromSecsU64(mulU64Checked(this, 604_800uL))

fun Long.stdNanoseconds(): StdDuration {
    require(this >= 0L) { "duration cannot be negative" }
    return toULong().stdNanoseconds()
}

fun Long.stdMicroseconds(): StdDuration {
    require(this >= 0L) { "duration cannot be negative" }
    return toULong().stdMicroseconds()
}

fun Long.stdMilliseconds(): StdDuration {
    require(this >= 0L) { "duration cannot be negative" }
    return toULong().stdMilliseconds()
}

fun Long.stdSeconds(): StdDuration {
    require(this >= 0L) { "duration cannot be negative" }
    return toULong().stdSeconds()
}

fun Long.stdMinutes(): StdDuration {
    require(this >= 0L) { "duration cannot be negative" }
    return toULong().stdMinutes()
}

fun Long.stdHours(): StdDuration {
    require(this >= 0L) { "duration cannot be negative" }
    return toULong().stdHours()
}

fun Long.stdDays(): StdDuration {
    require(this >= 0L) { "duration cannot be negative" }
    return toULong().stdDays()
}

fun Long.stdWeeks(): StdDuration {
    require(this >= 0L) { "duration cannot be negative" }
    return toULong().stdWeeks()
}

fun Int.stdNanoseconds(): StdDuration = toLong().stdNanoseconds()

fun Int.stdMicroseconds(): StdDuration = toLong().stdMicroseconds()

fun Int.stdMilliseconds(): StdDuration = toLong().stdMilliseconds()

fun Int.stdSeconds(): StdDuration = toLong().stdSeconds()

fun Int.stdMinutes(): StdDuration = toLong().stdMinutes()

fun Int.stdHours(): StdDuration = toLong().stdHours()

fun Int.stdDays(): StdDuration = toLong().stdDays()

fun Int.stdWeeks(): StdDuration = toLong().stdWeeks()

fun Double.stdNanoseconds(): StdDuration {
    require(this >= 0.0) { "duration cannot be negative" }
    return stdDurationFromNanosU64(this.toULong())
}

fun Double.stdMicroseconds(): StdDuration {
    require(this >= 0.0) { "duration cannot be negative" }
    return stdDurationFromNanosU64((this * 1_000.0).toULong())
}

fun Double.stdMilliseconds(): StdDuration {
    require(this >= 0.0) { "duration cannot be negative" }
    return stdDurationFromNanosU64((this * 1_000_000.0).toULong())
}

fun Double.stdSeconds(): StdDuration {
    require(this >= 0.0) { "duration cannot be negative" }
    return stdDurationFromNanosU64((this * 1_000_000_000.0).toULong())
}

fun Double.stdMinutes(): StdDuration {
    require(this >= 0.0) { "duration cannot be negative" }
    return stdDurationFromNanosU64((this * 60_000_000_000.0).toULong())
}

fun Double.stdHours(): StdDuration {
    require(this >= 0.0) { "duration cannot be negative" }
    return stdDurationFromNanosU64((this * 3_600_000_000_000.0).toULong())
}

fun Double.stdDays(): StdDuration {
    require(this >= 0.0) { "duration cannot be negative" }
    return stdDurationFromNanosU64((this * 86_400_000_000_000.0).toULong())
}

fun Double.stdWeeks(): StdDuration {
    require(this >= 0.0) { "duration cannot be negative" }
    return stdDurationFromNanosU64((this * 604_800_000_000_000.0).toULong())
}
