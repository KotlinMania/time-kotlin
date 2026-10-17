// port-lint: source duration.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serde.serdeCatching
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.time.serde.DurationVisitor
import io.github.kotlinmania.time.serde.I32Serialize
import io.github.kotlinmania.time.serde.I64Serialize
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.nanoseconds as stdNanoseconds
import kotlin.time.Duration.Companion.seconds as stdSeconds

internal enum class FloatConstructorError {
    Nan,
    NegOverflow,
    PosOverflow,
}

internal enum class Padding {
    Optimize,
}

/**
 * A span of time with nanosecond precision.
 *
 * Each `Duration` is composed of a whole number of seconds and a fractional part represented in
 * nanoseconds.
 *
 * This implementation allows for negative durations, unlike standard unsigned durations.
 */
class Duration internal constructor(
    /** Number of whole seconds. */
    val seconds: Long,
    /** Number of nanoseconds within the second. The sign always matches the `seconds` field. */
    val nanoseconds: Int,
    internal val padding: Padding = Padding.Optimize,
) : Comparable<Duration>, Serialize {
    init {
        require(nanoseconds in -999_999_999..999_999_999) {
            "nanoseconds must be in range -999_999_999..=999_999_999, got $nanoseconds"
        }
        if (seconds < 0) {
            require(nanoseconds <= 0) { "nanoseconds must match negative sign of seconds" }
        } else if (seconds > 0) {
            require(nanoseconds >= 0) { "nanoseconds must match positive sign of seconds" }
        }
    }

    /** Check if a duration is exactly zero. */
    fun isZero(): Boolean = seconds == 0L && nanoseconds == 0

    /** Check if a duration is negative. */
    fun isNegative(): Boolean = seconds < 0L || nanoseconds < 0

    /** Check if a duration is positive. */
    fun isPositive(): Boolean = seconds > 0L || nanoseconds > 0

    /**
     * Get the absolute value of the duration.
     *
     * This method saturates the returned value if it would otherwise overflow.
     */
    fun abs(): Duration =
        if (seconds == Long.MIN_VALUE) {
            MAX
        } else {
            newRangedUnchecked(abs(seconds), abs(nanoseconds))
        }

    /**
     * Convert the existing `Duration` to a standard duration and its magnitude.
     */
    fun unsignedAbs(): kotlin.time.Duration {
        val sec = if (seconds == Long.MIN_VALUE) Long.MAX_VALUE else abs(seconds)
        val nano = abs(nanoseconds)
        return sec.stdSeconds + nano.stdNanoseconds
    }

    /** Get the number of whole weeks in the duration. */
    fun wholeWeeks(): Long = wholeSeconds() / 604_800L

    /** Get the number of whole days in the duration. */
    fun wholeDays(): Long = wholeSeconds() / 86_400L

    /** Get the number of whole hours in the duration. */
    fun wholeHours(): Long = wholeSeconds() / 3_600L

    /** Get the number of whole minutes in the duration. */
    fun wholeMinutes(): Long = wholeSeconds() / 60L

    /** Get the number of whole seconds in the duration. */
    fun wholeSeconds(): Long = seconds

    /** Get the number of fractional seconds in the duration. */
    fun asSecondsF64(): Double = seconds.toDouble() + (nanoseconds.toDouble() / 1_000_000_000.0)

    /** Get the number of fractional seconds in the duration. */
    fun asSecondsF32(): Float = seconds.toFloat() + (nanoseconds.toFloat() / 1_000_000_000f)

    /** Get the number of whole milliseconds in the duration. */
    fun wholeMilliseconds(): Long =
        seconds * 1_000L + (nanoseconds / 1_000_000L)

    /** Get the number of milliseconds past the number of whole seconds. */
    fun subsecMilliseconds(): Short = (nanoseconds / 1_000_000).toShort()

    /** Get the number of whole microseconds in the duration. */
    fun wholeMicroseconds(): Long =
        seconds * 1_000_000L + (nanoseconds / 1_000L)

    /** Get the number of microseconds past the number of whole seconds. */
    fun subsecMicroseconds(): Int = nanoseconds / 1_000

    /** Get the number of nanoseconds in the duration. */
    fun wholeNanoseconds(): Long =
        seconds * 1_000_000_000L + nanoseconds.toLong()

    /** Get the number of nanoseconds past the number of whole seconds. */
    fun subsecNanoseconds(): Int = nanoseconds

    internal fun subsecNanosecondsRanged(): Int = nanoseconds

    /** Computes `self + rhs`, returning `null` if an overflow occurred. */
    fun checkedAdd(rhs: Duration): Duration? {
        val (secSum, secOverflow) = addWithOverflow(seconds, rhs.seconds)
        if (secOverflow) return null
        var sec = secSum
        var nano = nanoseconds + rhs.nanoseconds

        if (nano >= 1_000_000_000 || (sec < 0 && nano > 0)) {
            nano -= 1_000_000_000
            val (nextSec, overflow) = addWithOverflow(sec, 1L)
            if (overflow) return null
            sec = nextSec
        } else if (nano <= -1_000_000_000 || (sec > 0 && nano < 0)) {
            nano += 1_000_000_000
            val (nextSec, overflow) = subWithOverflow(sec, 1L)
            if (overflow) return null
            sec = nextSec
        }

        return newUnchecked(sec, nano)
    }

    /** Computes `self - rhs`, returning `null` if an overflow occurred. */
    fun checkedSub(rhs: Duration): Duration? {
        val (secSub, secOverflow) = subWithOverflow(seconds, rhs.seconds)
        if (secOverflow) return null
        var sec = secSub
        var nano = nanoseconds - rhs.nanoseconds

        if (nano >= 1_000_000_000 || (sec < 0 && nano > 0)) {
            nano -= 1_000_000_000
            val (nextSec, overflow) = addWithOverflow(sec, 1L)
            if (overflow) return null
            sec = nextSec
        } else if (nano <= -1_000_000_000 || (sec > 0 && nano < 0)) {
            nano += 1_000_000_000
            val (nextSec, overflow) = subWithOverflow(sec, 1L)
            if (overflow) return null
            sec = nextSec
        }

        return newUnchecked(sec, nano)
    }

    /** Computes `self * rhs`, returning `null` if an overflow occurred. */
    fun checkedMul(rhs: Int): Duration? {
        val totalNanos = nanoseconds.toLong() * rhs.toLong()
        val extraSecs = totalNanos / 1_000_000_000L
        val nano = (totalNanos % 1_000_000_000L).toInt()
        val (secMul, ov1) = mulWithOverflow(seconds, rhs.toLong())
        if (ov1) return null
        val (sec, ov2) = addWithOverflow(secMul, extraSecs)
        if (ov2) return null
        return newUnchecked(sec, nano)
    }

    /** Computes `self / rhs`, returning `null` if `rhs == 0` or if the result would overflow. */
    fun checkedDiv(rhs: Int): Duration? {
        if (rhs == 0) return null
        if (seconds == Long.MIN_VALUE && rhs == -1) return null

        val secs = seconds / rhs.toLong()
        val extraSecs = seconds % rhs.toLong()
        var nanos = nanoseconds / rhs
        val extraNanos = nanoseconds % rhs
        nanos += ((extraSecs * 1_000_000_000L + extraNanos.toLong()) / rhs.toLong()).toInt()

        return newUnchecked(secs, nanos)
    }

    /** Computes `-self`, returning `null` if the result would overflow. */
    fun checkedNeg(): Duration? {
        if (seconds == Long.MIN_VALUE) return null
        return newRangedUnchecked(-seconds, -nanoseconds)
    }

    /** Computes `self + rhs`, saturating if an overflow occurred. */
    fun saturatingAdd(rhs: Duration): Duration {
        val (secSum, secOverflow) = addWithOverflow(seconds, rhs.seconds)
        if (secOverflow) {
            return if (seconds > 0) MAX else MIN
        }
        var sec = secSum
        var nano = nanoseconds + rhs.nanoseconds

        if (nano >= 1_000_000_000 || (sec < 0 && nano > 0)) {
            nano -= 1_000_000_000
            val (nextSec, overflow) = addWithOverflow(sec, 1L)
            if (overflow) return MAX
            sec = nextSec
        } else if (nano <= -1_000_000_000 || (sec > 0 && nano < 0)) {
            nano += 1_000_000_000
            val (nextSec, overflow) = subWithOverflow(sec, 1L)
            if (overflow) return MIN
            sec = nextSec
        }

        return newUnchecked(sec, nano)
    }

    /** Computes `self - rhs`, saturating if an overflow occurred. */
    fun saturatingSub(rhs: Duration): Duration {
        val (secSub, secOverflow) = subWithOverflow(seconds, rhs.seconds)
        if (secOverflow) {
            return if (seconds > 0) MAX else MIN
        }
        var sec = secSub
        var nano = nanoseconds - rhs.nanoseconds

        if (nano >= 1_000_000_000 || (sec < 0 && nano > 0)) {
            nano -= 1_000_000_000
            val (nextSec, overflow) = addWithOverflow(sec, 1L)
            if (overflow) return MAX
            sec = nextSec
        } else if (nano <= -1_000_000_000 || (sec > 0 && nano < 0)) {
            nano += 1_000_000_000
            val (nextSec, overflow) = subWithOverflow(sec, 1L)
            if (overflow) return MIN
            sec = nextSec
        }

        return newUnchecked(sec, nano)
    }

    /** Computes `self * rhs`, saturating if an overflow occurred. */
    fun saturatingMul(rhs: Int): Duration {
        val totalNanos = nanoseconds.toLong() * rhs.toLong()
        val extraSecs = totalNanos / 1_000_000_000L
        val nano = (totalNanos % 1_000_000_000L).toInt()
        val (secMul, ov1) = mulWithOverflow(seconds, rhs.toLong())
        if (ov1) {
            return if ((seconds > 0 && rhs > 0) || (seconds < 0 && rhs < 0)) MAX else MIN
        }
        val (sec, ov2) = addWithOverflow(secMul, extraSecs)
        if (ov2) {
            return if (seconds > 0 && rhs > 0) MAX else MIN
        }
        return newUnchecked(sec, nano)
    }

    fun fmt(): String {
        if (isZero()) return "0s"

        val sb = StringBuilder()
        if (isNegative()) {
            sb.append("-")
        }

        val sec = if (seconds == Long.MIN_VALUE) Long.MAX_VALUE else abs(seconds)
        val nano = abs(nanoseconds)

        val days = sec / 86_400L
        val hours = (sec / 3_600L) % 24L
        val minutes = (sec / 60L) % 60L
        val remainingSecs = sec % 60L
        val millis = nano / 1_000_000
        val micros = (nano / 1_000) % 1_000
        val remainingNanos = nano % 1_000

        if (days != 0L) sb.append("${days}d")
        if (hours != 0L) sb.append("${hours}h")
        if (minutes != 0L) sb.append("${minutes}m")
        if (remainingSecs != 0L) sb.append("${remainingSecs}s")
        if (millis != 0) sb.append("${millis}ms")
        if (micros != 0) sb.append("${micros}µs")
        if (remainingNanos != 0) sb.append("${remainingNanos}ns")

        return sb.toString()
    }

    override fun toString(): String = fmt()

    override fun compareTo(other: Duration): Int {
        val secCmp = seconds.compareTo(other.seconds)
        if (secCmp != 0) return secCmp
        return nanoseconds.compareTo(other.nanoseconds)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Duration) return false
        return seconds == other.seconds && nanoseconds == other.nanoseconds
    }

    override fun hashCode(): Int {
        var result = seconds.hashCode()
        result = 31 * result + nanoseconds
        return result
    }

    operator fun plus(rhs: Duration): Duration =
        checkedAdd(rhs) ?: throw ArithmeticException("overflow when adding durations")

    operator fun minus(rhs: Duration): Duration =
        checkedSub(rhs) ?: throw ArithmeticException("overflow when subtracting durations")

    operator fun times(rhs: Int): Duration =
        checkedMul(rhs) ?: throw ArithmeticException("overflow when multiplying duration")

    operator fun times(rhs: Float): Duration =
        secondsF32(asSecondsF32() * rhs)

    operator fun times(rhs: Double): Duration =
        secondsF64(asSecondsF64() * rhs)

    operator fun div(rhs: Int): Duration =
        checkedDiv(rhs) ?: throw ArithmeticException("overflow when dividing duration")

    operator fun div(rhs: Float): Duration =
        secondsF32(asSecondsF32() / rhs)

    operator fun div(rhs: Double): Duration =
        secondsF64(asSecondsF64() / rhs)

    operator fun div(rhs: Duration): Double =
        asSecondsF64() / rhs.asSecondsF64()

    operator fun unaryMinus(): Duration =
        checkedNeg() ?: throw ArithmeticException("overflow when negating duration")

    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
        if (serializer.isHumanReadable()) {
            val sign = if (isNegative()) "-" else ""
            val secs =
                if (wholeSeconds() == Long.MIN_VALUE) {
                    9223372036854775808uL
                } else {
                    abs(wholeSeconds()).toULong()
                }
            val nanos = abs(subsecNanoseconds()).toString().padStart(9, '0')
            return serializer.collectStr("$sign$secs.$nanos")
        }
        return serdeCatching {
            val tuple = serializer.serializeTuple(2).getOrThrow()
            tuple.serializeElement(I64Serialize(wholeSeconds())).getOrThrow()
            tuple.serializeElement(I32Serialize(subsecNanoseconds())).getOrThrow()
            tuple.end().getOrThrow()
        }
    }

    companion object : Deserialize<Duration> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Duration> =
            if (deserializer.isHumanReadable()) {
                deserializer.deserializeAny(DurationVisitor)
            } else {
                deserializer.deserializeTuple(2, DurationVisitor)
            }

        val ZERO: Duration = seconds(0)
        val NANOSECOND: Duration = nanoseconds(1)
        val MICROSECOND: Duration = microseconds(1)
        val MILLISECOND: Duration = milliseconds(1)
        val SECOND: Duration = seconds(1)
        val MINUTE: Duration = minutes(1)
        val HOUR: Duration = hours(1)
        val DAY: Duration = days(1)
        val WEEK: Duration = weeks(1)
        val MIN: Duration = newRanged(Long.MIN_VALUE, -999_999_999)
        val MAX: Duration = newRanged(Long.MAX_VALUE, 999_999_999)

        fun default(): Duration = ZERO

        internal fun newUnchecked(seconds: Long, nanoseconds: Int): Duration =
            newRangedUnchecked(seconds, nanoseconds)

        internal fun newRangedUnchecked(seconds: Long, nanoseconds: Int): Duration =
            Duration(seconds, nanoseconds, Padding.Optimize)

        fun new(seconds: Long, nanoseconds: Int): Duration {
            val secExtra = nanoseconds.toLong() / 1_000_000_000L
            val (sec1, ov1) = addWithOverflow(seconds, secExtra)
            if (ov1) throw ArithmeticException("overflow constructing `time::Duration`")
            var sec = sec1
            var nano = nanoseconds % 1_000_000_000

            if (sec > 0 && nano < 0) {
                sec -= 1
                nano += 1_000_000_000
            } else if (sec < 0 && nano > 0) {
                sec += 1
                nano -= 1_000_000_000
            }

            return newUnchecked(sec, nano)
        }

        internal fun newRanged(seconds: Long, nanoseconds: Int): Duration {
            var sec = seconds
            var nano = nanoseconds
            if (sec > 0 && nano < 0) {
                sec -= 1
                nano += 1_000_000_000
            } else if (sec < 0 && nano > 0) {
                sec += 1
                nano -= 1_000_000_000
            }
            return newRangedUnchecked(sec, nano)
        }

        fun weeks(weeks: Long): Duration {
            val (sec, ov) = mulWithOverflow(weeks, 604_800L)
            if (ov) throw ArithmeticException("overflow constructing `time::Duration`")
            return seconds(sec)
        }

        fun days(days: Long): Duration {
            val (sec, ov) = mulWithOverflow(days, 86_400L)
            if (ov) throw ArithmeticException("overflow constructing `time::Duration`")
            return seconds(sec)
        }

        fun hours(hours: Long): Duration {
            val (sec, ov) = mulWithOverflow(hours, 3_600L)
            if (ov) throw ArithmeticException("overflow constructing `time::Duration`")
            return seconds(sec)
        }

        fun minutes(minutes: Long): Duration {
            val (sec, ov) = mulWithOverflow(minutes, 60L)
            if (ov) throw ArithmeticException("overflow constructing `time::Duration`")
            return seconds(sec)
        }

        fun seconds(seconds: Long): Duration =
            newRangedUnchecked(seconds, 0)

        private fun trySecondsF64(seconds: Double): Result<Duration> {
            if (seconds.isNaN()) return Result.failure(IllegalArgumentException("passed NaN to `time::Duration::seconds_f64`"))
            if (seconds.isInfinite()) {
                return Result.failure(ArithmeticException("overflow constructing `time::Duration`"))
            }
            if (seconds > Long.MAX_VALUE.toDouble() || seconds < Long.MIN_VALUE.toDouble()) {
                return Result.failure(ArithmeticException("overflow constructing `time::Duration`"))
            }
            val whole = seconds.toLong()
            val frac = seconds - whole.toDouble()
            val nano = (frac * 1_000_000_000.0).roundToLong()
            return try {
                Result.success(new(whole, nano.toInt()))
            } catch (e: ArithmeticException) {
                Result.failure(e)
            }
        }

        private fun trySecondsF32(seconds: Float): Result<Duration> =
            trySecondsF64(seconds.toDouble())

        fun secondsF64(seconds: Double): Duration =
            trySecondsF64(seconds).getOrThrow()

        fun secondsF32(seconds: Float): Duration =
            trySecondsF32(seconds).getOrThrow()

        fun saturatingSecondsF64(seconds: Double): Duration =
            if (seconds.isNaN()) {
                ZERO
            } else if (seconds.isInfinite()) {
                if (seconds > 0) MAX else MIN
            } else if (seconds >= Long.MAX_VALUE.toDouble()) {
                MAX
            } else if (seconds <= Long.MIN_VALUE.toDouble()) {
                MIN
            } else {
                trySecondsF64(seconds).getOrElse { if (seconds > 0) MAX else MIN }
            }

        fun saturatingSecondsF32(seconds: Float): Duration =
            saturatingSecondsF64(seconds.toDouble())

        fun checkedSecondsF64(seconds: Double): Duration? =
            trySecondsF64(seconds).getOrNull()

        fun checkedSecondsF32(seconds: Float): Duration? =
            trySecondsF32(seconds).getOrNull()

        fun milliseconds(milliseconds: Long): Duration =
            newUnchecked(
                milliseconds / 1_000L,
                ((milliseconds % 1_000L) * 1_000_000L).toInt(),
            )

        fun microseconds(microseconds: Long): Duration =
            newUnchecked(
                microseconds / 1_000_000L,
                ((microseconds % 1_000_000L) * 1_000L).toInt(),
            )

        fun nanoseconds(nanoseconds: Long): Duration =
            newUnchecked(
                nanoseconds / 1_000_000_000L,
                (nanoseconds % 1_000_000_000L).toInt(),
            )

        fun nanosecondsI128(nanoseconds: Long): Duration =
            nanoseconds(nanoseconds)

        fun <T> timeFn(f: () -> T): Pair<Duration, T> {
            val mark =
                kotlin.time.TimeSource.Monotonic
                    .markNow()
            val result = f()
            val elapsed = mark.elapsedNow()
            return Pair(fromStdDuration(elapsed), result)
        }

        fun fromStdDuration(std: kotlin.time.Duration): Duration {
            val totalSec = std.inWholeSeconds
            val nanos = (std.inWholeNanoseconds % 1_000_000_000L).toInt()
            return new(totalSec, nanos)
        }

        private fun addWithOverflow(x: Long, y: Long): Pair<Long, Boolean> {
            val r = x + y
            val overflow = (((x xor r) and (y xor r)) < 0)
            return Pair(r, overflow)
        }

        private fun subWithOverflow(x: Long, y: Long): Pair<Long, Boolean> {
            val r = x - y
            val overflow = (((x xor y) and (x xor r)) < 0)
            return Pair(r, overflow)
        }

        private fun mulWithOverflow(x: Long, y: Long): Pair<Long, Boolean> {
            val r = x * y
            val ax = abs(x)
            val ay = abs(y)
            val overflow =
                if (x == 0L || y == 0L) {
                    false
                } else if (x == Long.MIN_VALUE || y == Long.MIN_VALUE) {
                    !((x == Long.MIN_VALUE && y == 1L) || (x == 1L && y == Long.MIN_VALUE))
                } else {
                    (ax > Long.MAX_VALUE / ay)
                }
            return Pair(r, overflow)
        }
    }
}
