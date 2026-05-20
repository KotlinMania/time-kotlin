// port-lint: source utc_offset.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.error.ComponentRange
import kotlin.math.abs

/**
 * The `UtcOffset` struct and its associated implementations.
 */

/** The type of the `hours` field of `UtcOffset`. */
private typealias Hours = Int

/** The type of the `minutes` field of `UtcOffset`. */
private typealias Minutes = Int

/** The type of the `seconds` field of `UtcOffset`. */
private typealias Seconds = Int

/** The type capable of storing the range of whole seconds that a `UtcOffset` can encompass. */
private typealias WholeSeconds = Int

/** An offset from UTC. */
class UtcOffset private constructor(
    private val hours: Hours,
    private val minutes: Minutes,
    private val seconds: Seconds,
) : Comparable<UtcOffset> {
    fun hash(): UInt = asU32ForEquality()

    fun eq(other: UtcOffset): Boolean = asU32ForEquality() == other.asU32ForEquality()

    fun partialCmp(other: UtcOffset): Int = cmp(other)

    fun cmp(other: UtcOffset): Int = compareTo(other)

    /** Provide a representation of the `UtcOffset` as an `Int` for equality and hashing. */
    internal fun asU32ForEquality(): UInt {
        val first = seconds.toByte().toUByte().toUInt()
        val second = minutes.toByte().toUByte().toUInt() shl 8
        val third = hours.toByte().toUByte().toUInt() shl 16
        return first or second or third
    }

    /**
     * Provide a representation of the `UtcOffset` as an `Int`. This value can
     * be used for ordering.
     */
    internal fun asI32ForComparison(): Int =
        (hours shl 16) or ((minutes and 0xFF) shl 8) or (seconds and 0xFF)

    /** Obtain the UTC offset as its hours, minutes, and seconds. */
    fun asHms(): Triple<Int, Int, Int> = Triple(hours, minutes, seconds)

    /** Obtain the UTC offset as its hours, minutes, and seconds. */
    internal fun asHmsRanged(): Triple<Hours, Minutes, Seconds> = Triple(hours, minutes, seconds)

    /** Obtain the number of whole hours the offset is from UTC. */
    fun wholeHours(): Int = hours

    /** Obtain the number of whole minutes the offset is from UTC. */
    fun wholeMinutes(): Int = hours * MINUTES_PER_HOUR + minutes

    /** Obtain the number of minutes past the hour the offset is from UTC. */
    fun minutesPastHour(): Int = minutes

    /** Obtain the number of whole seconds the offset is from UTC. */
    fun wholeSeconds(): Int =
        hours * SECONDS_PER_HOUR +
            minutes * SECONDS_PER_MINUTE +
            seconds

    /** Obtain the number of seconds past the minute the offset is from UTC. */
    fun secondsPastMinute(): Int = seconds

    /** Check if the offset is exactly UTC. */
    fun isUtc(): Boolean = asU32ForEquality() == UTC.asU32ForEquality()

    /** Check if the offset is positive, or east of UTC. */
    fun isPositive(): Boolean = compareTo(UTC) > 0

    /** Check if the offset is negative, or west of UTC. */
    fun isNegative(): Boolean = compareTo(UTC) < 0

    override fun compareTo(other: UtcOffset): Int =
        wholeSeconds().compareTo(other.wholeSeconds())

    override fun equals(other: Any?): Boolean =
        other is UtcOffset &&
            other.hours == hours &&
            other.minutes == minutes &&
            other.seconds == seconds

    override fun hashCode(): Int = asU32ForEquality().hashCode()

    /** Format the offset as `+HH:MM:SS` or `-HH:MM:SS`. */
    override fun toString(): String {
        val sign = if (isNegative()) "-" else "+"
        return "$sign${abs(hours).twoDigits()}:${abs(minutes).twoDigits()}:${abs(seconds).twoDigits()}"
    }

    fun fmt(): String = toString()

    /** Negate this `UtcOffset`. */
    operator fun unaryMinus(): UtcOffset = neg()

    fun neg(): UtcOffset = fromHmsRanged(-hours, -minutes, -seconds)

    companion object {
        private const val MINUTES_PER_HOUR = 60
        private const val SECONDS_PER_MINUTE = 60
        private const val SECONDS_PER_HOUR = 3_600
        private const val MIN_HOURS = -25
        private const val MAX_HOURS = 25
        private const val MIN_COMPONENT = -59
        private const val MAX_COMPONENT = 59
        private const val MIN_WHOLE_SECONDS =
            MIN_HOURS * SECONDS_PER_HOUR + MIN_COMPONENT * SECONDS_PER_MINUTE + MIN_COMPONENT
        private const val MAX_WHOLE_SECONDS =
            MAX_HOURS * SECONDS_PER_HOUR + MAX_COMPONENT * SECONDS_PER_MINUTE + MAX_COMPONENT

        /** A `UtcOffset` that is UTC. */
        val UTC: UtcOffset = fromWholeSecondsRanged(0)

        /**
         * Create a `UtcOffset` representing an offset of the hours, minutes,
         * and seconds provided, the validity of which must be guaranteed by
         * the caller. All three parameters must have the same sign.
         */
        fun fromHmsUnchecked(hours: Int, minutes: Int, seconds: Int): UtcOffset =
            fromHmsRangedUnchecked(hours, minutes, seconds)

        /**
         * Create a `UtcOffset` representing an offset by the number of hours,
         * minutes, and seconds provided.
         *
         * The sign of all three components should match. If they do not, all
         * smaller components will have their signs flipped.
         */
        fun fromHms(
            hours: Hours,
            minutes: Minutes,
            seconds: Seconds,
        ): Result<UtcOffset> {
            validate(hours, MIN_HOURS, MAX_HOURS, "offset hour").onFailure {
                return Result.failure(it)
            }
            validate(minutes, MIN_COMPONENT, MAX_COMPONENT, "offset minute").onFailure {
                return Result.failure(it)
            }
            validate(seconds, MIN_COMPONENT, MAX_COMPONENT, "offset second").onFailure {
                return Result.failure(it)
            }
            return Result.success(fromHmsRanged(hours, minutes, seconds))
        }

        /**
         * Create a `UtcOffset` representing an offset of the hours, minutes,
         * and seconds provided. All three parameters must have the same sign.
         */
        internal fun fromHmsRangedUnchecked(
            hours: Hours,
            minutes: Minutes,
            seconds: Seconds,
        ): UtcOffset = UtcOffset(hours, minutes, seconds)

        /**
         * Create a `UtcOffset` representing an offset by the number of hours,
         * minutes, and seconds provided.
         *
         * The sign of all three components should match. If they do not, all
         * smaller components will have their signs flipped.
         */
        internal fun fromHmsRanged(
            hours: Hours,
            minutes: Minutes,
            seconds: Seconds,
        ): UtcOffset {
            var normalizedMinutes = minutes
            var normalizedSeconds = seconds

            if ((hours > 0 && normalizedMinutes < 0) || (hours < 0 && normalizedMinutes > 0)) {
                normalizedMinutes = -normalizedMinutes
            }
            if ((hours > 0 && normalizedSeconds < 0) ||
                (hours < 0 && normalizedSeconds > 0) ||
                (normalizedMinutes > 0 && normalizedSeconds < 0) ||
                (normalizedMinutes < 0 && normalizedSeconds > 0)
            ) {
                normalizedSeconds = -normalizedSeconds
            }

            return UtcOffset(hours, normalizedMinutes, normalizedSeconds)
        }

        /** Create a `UtcOffset` representing an offset by the number of seconds provided. */
        fun fromWholeSeconds(seconds: WholeSeconds): Result<UtcOffset> {
            validate(seconds, MIN_WHOLE_SECONDS, MAX_WHOLE_SECONDS, "offset second").onFailure {
                return Result.failure(it)
            }
            return Result.success(fromWholeSecondsRanged(seconds))
        }

        /** Create a `UtcOffset` representing an offset by the number of seconds provided. */
        internal fun fromWholeSecondsRanged(seconds: WholeSeconds): UtcOffset =
            fromHmsUnchecked(
                seconds / SECONDS_PER_HOUR,
                (seconds % SECONDS_PER_HOUR) / MINUTES_PER_HOUR,
                seconds % SECONDS_PER_MINUTE,
            )

        private fun validate(
            value: Int,
            minimum: Int,
            maximum: Int,
            name: String,
        ): Result<Unit> =
            if (value in minimum..maximum) {
                Result.success(Unit)
            } else {
                Result.failure(ComponentRange.unconditional(name))
            }
    }
}

private fun Int.twoDigits(): String =
    if (this < 10) {
        "0$this"
    } else {
        toString()
    }
