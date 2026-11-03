// port-lint: source utc_offset.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serde.serdeCatching
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.time.error.ComponentRange
import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatting.BorrowedFormatItemSliceFormattable
import io.github.kotlinmania.time.formatting.Formattable
import io.github.kotlinmania.time.parsing.BorrowedFormatItemSliceParsable
import io.github.kotlinmania.time.parsing.Parsable
import io.github.kotlinmania.time.serde.I8Serialize
import io.github.kotlinmania.time.serde.UTC_OFFSET_FORMAT
import io.github.kotlinmania.time.serde.UtcOffsetVisitor
import kotlin.math.abs

/**
 * The `UtcOffset` struct and its associated implementations.
 */

/** An offset from UTC. */
class UtcOffset private constructor(
    private val hours: Int,
    private val minutes: Int,
    private val seconds: Int,
) : Comparable<UtcOffset>,
    Serialize {
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

    /**
     * Obtain the UTC offset as its hours, minutes, and seconds. The sign of all three components
     * will always match. A positive value indicates an offset to the east; a negative to the west.
     *
     * Example:
     * UtcOffset.fromHms(1, 2, 3).getOrThrow().asHms() == Triple(1, 2, 3)
     */
    fun asHms(): Triple<Int, Int, Int> = Triple(hours, minutes, seconds)

    /** Obtain the UTC offset as its hours, minutes, and seconds. */
    internal fun asHmsRanged(): Triple<Int, Int, Int> = Triple(hours, minutes, seconds)

    /**
     * Obtain the number of whole hours the offset is from UTC. A positive value indicates an
     * offset to the east; a negative to the west.
     *
     * Example:
     * UtcOffset.fromHms(1, 2, 3).getOrThrow().wholeHours() == 1
     * UtcOffset.fromHms(-1, -2, -3).getOrThrow().wholeHours() == -1
     */
    fun wholeHours(): Int = hours

    /**
     * Obtain the number of whole minutes the offset is from UTC. A positive value indicates an
     * offset to the east; a negative to the west.
     *
     * Example:
     * UtcOffset.fromHms(1, 2, 3).getOrThrow().wholeMinutes() == 62
     * UtcOffset.fromHms(-1, -2, -3).getOrThrow().wholeMinutes() == -62
     */
    fun wholeMinutes(): Int = hours * MINUTES_PER_HOUR + minutes

    /**
     * Obtain the number of minutes past the hour the offset is from UTC. A positive value
     * indicates an offset to the east; a negative to the west.
     *
     * Example:
     * UtcOffset.fromHms(1, 2, 3).getOrThrow().minutesPastHour() == 2
     * UtcOffset.fromHms(-1, -2, -3).getOrThrow().minutesPastHour() == -2
     */
    fun minutesPastHour(): Int = minutes

    /**
     * Obtain the number of whole seconds the offset is from UTC. A positive value indicates an
     * offset to the east; a negative to the west.
     *
     * Example:
     * UtcOffset.fromHms(1, 2, 3).getOrThrow().wholeSeconds() == 3723
     * UtcOffset.fromHms(-1, -2, -3).getOrThrow().wholeSeconds() == -3723
     */
    fun wholeSeconds(): Int =
        hours * SECONDS_PER_HOUR +
            minutes * SECONDS_PER_MINUTE +
            seconds

    /**
     * Obtain the number of seconds past the minute the offset is from UTC. A positive value
     * indicates an offset to the east; a negative to the west.
     *
     * Example:
     * UtcOffset.fromHms(1, 2, 3).getOrThrow().secondsPastMinute() == 3
     * UtcOffset.fromHms(-1, -2, -3).getOrThrow().secondsPastMinute() == -3
     */
    fun secondsPastMinute(): Int = seconds

    /**
     * Check if the offset is exactly UTC.
     *
     * Example:
     * UtcOffset.UTC.isUtc() == true
     * UtcOffset.fromHms(1, 2, 3).getOrThrow().isUtc() == false
     */
    fun isUtc(): Boolean = asU32ForEquality() == UTC.asU32ForEquality()

    /**
     * Check if the offset is positive, or east of UTC.
     *
     * Example:
     * UtcOffset.fromHms(1, 2, 3).getOrThrow().isPositive() == true
     * UtcOffset.UTC.isPositive() == false
     */
    fun isPositive(): Boolean = compareTo(UTC) > 0

    /**
     * Check if the offset is negative, or west of UTC.
     *
     * Example:
     * UtcOffset.fromHms(-1, -2, -3).getOrThrow().isNegative() == true
     * UtcOffset.UTC.isNegative() == false
     */
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

    /** Format the `UtcOffset` as a string. */
    fun format(): String = toString()

    /** Format the [UtcOffset] using the provided [Formattable] into the given [output] buffer. */
    fun formatInto(
        output: StringBuilder,
        description: Formattable,
    ): Result<Int> = description.formatInto(output, this)

    fun formatInto(
        output: StringBuilder,
        description: List<BorrowedFormatItem>,
    ): Result<Int> = BorrowedFormatItemSliceFormattable(description).formatInto(output, this)

    /** Format the [UtcOffset] using the provided [Formattable]. */
    fun format(description: Formattable): Result<String> = description.format(this)

    fun format(description: List<BorrowedFormatItem>): Result<String> =
        BorrowedFormatItemSliceFormattable(description).format(this)

    fun fmt(): String = toString()

    /** Negate this `UtcOffset`. */
    operator fun unaryMinus(): UtcOffset = neg()

    fun neg(): UtcOffset = fromHmsRanged(-hours, -minutes, -seconds)

    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
        if (serializer.isHumanReadable()) {
            val s =
                format(UTC_OFFSET_FORMAT).getOrElse {
                    return SerdeResult.failure(SerdeError.custom("failed formatting `UtcOffset`"))
                }
            return serializer.serializeStr(s)
        }
        return serdeCatching {
            val tuple = serializer.serializeTuple(3).getOrThrow()
            tuple.serializeElement(I8Serialize(wholeHours().toByte())).getOrThrow()
            tuple.serializeElement(I8Serialize(minutesPastHour().toByte())).getOrThrow()
            tuple.serializeElement(I8Serialize(secondsPastMinute().toByte())).getOrThrow()
            tuple.end().getOrThrow()
        }
    }

    companion object : Deserialize<UtcOffset> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<UtcOffset> =
            if (deserializer.isHumanReadable()) {
                deserializer.deserializeAny(UtcOffsetVisitor)
            } else {
                deserializer.deserializeTuple(3, UtcOffsetVisitor)
            }

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

        /**
         * A `UtcOffset` that is UTC.
         *
         * Example:
         * UtcOffset.UTC.wholeSeconds() == 0
         */
        val UTC: UtcOffset = fromWholeSecondsRanged(0)

        /**
         * Attempt to obtain the system's UTC offset at a known moment in time. If the offset cannot
         * be determined, an error is returned.
         */
        fun localOffsetAt(datetime: OffsetDateTime): Result<UtcOffset> =
            io.github.kotlinmania.time.sys.localoffsetat.unix
                .localOffsetAt(datetime)
                ?.let { Result.success(it) }
                ?: Result.failure(io.github.kotlinmania.time.error.IndeterminateOffset)

        /**
         * Attempt to obtain the system's current UTC offset. If the offset cannot be determined,
         * an error is returned.
         */
        fun currentLocalOffset(): Result<UtcOffset> =
            localOffsetAt(OffsetDateTime.nowUtc())

        /**
         * Parse a `UtcOffset` from string input.
         */
        fun parse(input: String): Result<UtcOffset> {
            val trimmed = input.trim()
            if (trimmed == "UTC" || trimmed == "Z" || trimmed == "+00:00" || trimmed == "-00:00") {
                return Result.success(UTC)
            }
            val parts = trimmed.removePrefix("+").split(":")
            if (parts.size >= 2) {
                val h = parts[0].toIntOrNull()
                val m = parts[1].toIntOrNull()
                val s = if (parts.size >= 3) parts[2].toIntOrNull() ?: 0 else 0
                if (h != null && m != null) {
                    return fromHms(h, m, s)
                }
            }
            return Result.failure(ComponentRange.unconditional("offset"))
        }

        /** Parse a [UtcOffset] from the input using the provided [Parsable] format description. */
        fun parse(input: String, description: Parsable): Result<UtcOffset> =
            description.parseUtcOffset(input.encodeToByteArray())

        fun parse(input: String, description: List<BorrowedFormatItem>): Result<UtcOffset> =
            BorrowedFormatItemSliceParsable(description).parseUtcOffset(input.encodeToByteArray())

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
            hours: Int,
            minutes: Int,
            seconds: Int,
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
            hours: Int,
            minutes: Int,
            seconds: Int,
        ): UtcOffset = UtcOffset(hours, minutes, seconds)

        /**
         * Create a `UtcOffset` representing an offset by the number of hours,
         * minutes, and seconds provided.
         *
         * The sign of all three components should match. If they do not, all
         * smaller components will have their signs flipped.
         */
        internal fun fromHmsRanged(
            hours: Int,
            minutes: Int,
            seconds: Int,
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
        fun fromWholeSeconds(seconds: Int): Result<UtcOffset> {
            validate(seconds, MIN_WHOLE_SECONDS, MAX_WHOLE_SECONDS, "offset second").onFailure {
                return Result.failure(it)
            }
            return Result.success(fromWholeSecondsRanged(seconds))
        }

        /** Create a `UtcOffset` representing an offset by the number of seconds provided. */
        internal fun fromWholeSecondsRanged(seconds: Int): UtcOffset =
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
