// port-lint: source offset_date_time.rs
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
import io.github.kotlinmania.time.formatting.computeTimestampNanosSignAndAbs
import io.github.kotlinmania.time.parsing.BorrowedFormatItemSliceParsable
import io.github.kotlinmania.time.parsing.Parsable
import io.github.kotlinmania.time.parsing.parseNanosStringToSecAndNano
import io.github.kotlinmania.time.serde.I8Serialize
import io.github.kotlinmania.time.serde.I32Serialize
import io.github.kotlinmania.time.serde.OFFSET_DATE_TIME_FORMAT
import io.github.kotlinmania.time.serde.OffsetDateTimeVisitor
import io.github.kotlinmania.time.serde.U8Serialize
import io.github.kotlinmania.time.serde.U16Serialize
import io.github.kotlinmania.time.serde.U32Serialize

internal val MIN_UNIX_TIMESTAMP: Long =
    OffsetDateTime.newInOffset(Date.MIN, Time.MIDNIGHT, UtcOffset.UTC).unixTimestamp()

internal val MAX_UNIX_TIMESTAMP: Long =
    OffsetDateTime.newInOffset(Date.MAX, Time.MAX, UtcOffset.UTC).unixTimestamp()

/**
 * A [PrimitiveDateTime] with a [UtcOffset].
 */
class OffsetDateTime internal constructor(
    private val localDateTime: PrimitiveDateTime,
    private val offset: UtcOffset,
) : Comparable<OffsetDateTime>, Serialize {
    companion object : Deserialize<OffsetDateTime> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<OffsetDateTime> =
            if (deserializer.isHumanReadable()) {
                deserializer.deserializeAny(OffsetDateTimeVisitor)
            } else {
                deserializer.deserializeTuple(9, OffsetDateTimeVisitor)
            }

        /** The Julian day of the Unix epoch. */
        private const val UNIX_EPOCH_JULIAN_DAY: Int = 2_440_588

        /** Midnight, 1 January, 1970 (UTC). */
        val UNIX_EPOCH: OffsetDateTime =
            newInOffset(Date.UNIX_EPOCH, Time.MIDNIGHT, UtcOffset.UTC)

        /** Create a new [OffsetDateTime] with the current date and time in UTC. */
        fun nowUtc(): OffsetDateTime = from(kotlin.time.Clock.System.now())

        /**
         * Attempt to create a new [OffsetDateTime] with the current date and time in the local offset.
         */
        fun nowLocal(): Result<OffsetDateTime> {
            val t = nowUtc()
            return UtcOffset.localOffsetAt(t).map { t.toOffset(it) }
        }

        /** Create a new [OffsetDateTime] with the given [Date], [Time], and [UtcOffset]. */
        fun newInOffset(date: Date, time: Time, offset: UtcOffset): OffsetDateTime =
            OffsetDateTime(PrimitiveDateTime.new(date, time), offset)

        /** Create a new [OffsetDateTime] with the given [Date] and [Time] in the UTC timezone. */
        fun newUtc(date: Date, time: Time): OffsetDateTime =
            newInOffset(date, time, UtcOffset.UTC)

        /** Convert a [UtcDateTime] to an [OffsetDateTime] in UTC. */
        fun from(datetime: UtcDateTime): OffsetDateTime =
            datetime.asPrimitive().assumeUtc()

        /** Convert a [kotlin.time.Instant] to an [OffsetDateTime] in UTC. */
        fun from(systemTime: kotlin.time.Instant): OffsetDateTime =
            UtcDateTime.from(systemTime).asPrimitive().assumeUtc()

        /** Create an [OffsetDateTime] from the provided unix timestamp in seconds. */
        fun fromUnixTimestamp(timestamp: Long): Result<OffsetDateTime> {
            if (timestamp < MIN_UNIX_TIMESTAMP || timestamp > MAX_UNIX_TIMESTAMP) {
                return Result.failure(ComponentRange.unconditional("timestamp"))
            }
            val secondsInDay = 86_400L
            val dayAdjustment = floorDiv(timestamp, secondsInDay).toInt()
            val secondsOfDay = floorMod(timestamp, secondsInDay).toInt()

            val date = Date.fromJulianDayUnchecked(UNIX_EPOCH_JULIAN_DAY + dayAdjustment)
            val hour = secondsOfDay / 3600
            val minute = (secondsOfDay % 3600) / 60
            val second = secondsOfDay % 60
            val time = Time.fromHmsUnchecked(hour, minute, second)

            return Result.success(newInOffset(date, time, UtcOffset.UTC))
        }

        /** Create an [OffsetDateTime] from the provided unix timestamp in nanoseconds. */
        fun fromUnixTimestampNanos(timestamp: Long): Result<OffsetDateTime> {
            val nanosInSecond = 1_000_000_000L
            val seconds = floorDiv(timestamp, nanosInSecond)
            val nanos = floorMod(timestamp, nanosInSecond).toInt()

            val base =
                fromUnixTimestamp(seconds).getOrElse {
                    return Result.failure(it)
                }
            val timeWithNanos = Time.fromHmsNanoUnchecked(base.hour(), base.minute(), base.second(), nanos)
            return Result.success(newInOffset(base.date(), timeWithNanos, UtcOffset.UTC))
        }

        /** Create an [OffsetDateTime] from the provided 128-bit unix timestamp in nanoseconds as a decimal string. */
        fun fromUnixTimestampNanos(timestamp: String): Result<OffsetDateTime> {
            val (seconds, nanos) =
                parseNanosStringToSecAndNano(timestamp)
                    ?: return Result.failure(ComponentRange.unconditional("timestamp"))
            val base =
                fromUnixTimestamp(seconds).getOrElse {
                    return Result.failure(it)
                }
            val timeWithNanos = Time.fromHmsNanoUnchecked(base.hour(), base.minute(), base.second(), nanos)
            return Result.success(newInOffset(base.date(), timeWithNanos, UtcOffset.UTC))
        }

        /** Parse an [OffsetDateTime] from the input using the provided [Parsable] format description. */
        fun parse(input: String, description: Parsable): Result<OffsetDateTime> =
            description.parseOffsetDateTime(input.encodeToByteArray())

        fun parse(input: String, description: List<BorrowedFormatItem>): Result<OffsetDateTime> =
            BorrowedFormatItemSliceParsable(description).parseOffsetDateTime(input.encodeToByteArray())
    }

    internal fun dateTime(): PrimitiveDateTime = localDateTime

    /** Convert the [OffsetDateTime] from the current [UtcOffset] to the provided [UtcOffset]. */
    fun toOffset(newOffset: UtcOffset): OffsetDateTime =
        checkedToOffset(newOffset) ?: throw ArithmeticException("local datetime out of valid range")

    /**
     * Convert the [OffsetDateTime] from the current [UtcOffset] to the provided [UtcOffset],
     * returning null if the datetime in the resulting offset is invalid.
     */
    fun checkedToOffset(newOffset: UtcOffset): OffsetDateTime? {
        if (offset.asU32ForEquality() == newOffset.asU32ForEquality()) {
            return this
        }
        val diffSeconds = newOffset.wholeSeconds().toLong() - offset.wholeSeconds().toLong()
        val adjustedLocalDateTime = localDateTime.checkedAdd(Duration.seconds(diffSeconds)) ?: return null
        return OffsetDateTime(adjustedLocalDateTime, newOffset)
    }

    /** Convert the [OffsetDateTime] from the current [UtcOffset] to UTC, returning a [UtcDateTime]. */
    fun toUtc(): UtcDateTime =
        checkedToUtc() ?: throw ArithmeticException("local datetime out of valid range")

    /**
     * Convert the [OffsetDateTime] from the current [UtcOffset] to UTC, returning null if the
     * UTC date-time is invalid. Returns a [UtcDateTime].
     */
    fun checkedToUtc(): UtcDateTime? {
        if (offset.isUtc()) {
            return localDateTime.asUtc()
        }
        val adjusted = localDateTime.checkedSub(Duration.seconds(offset.wholeSeconds().toLong())) ?: return null
        return adjusted.asUtc()
    }

    /** Convert this [OffsetDateTime] to a [kotlin.time.Instant]. */
    fun toSystemTime(): kotlin.time.Instant =
        kotlin.time.Instant.fromEpochSeconds(unixTimestamp(), nanosecond())

    /** Get the local date component. */
    fun date(): Date = localDateTime.date()

    /** Get the local time component. */
    fun time(): Time = localDateTime.time()

    /** Get the UTC offset. */
    fun offset(): UtcOffset = offset

    /** Get the year of the date. */
    fun year(): Int = localDateTime.year()

    /** Get the month. */
    fun month(): Month = localDateTime.month()

    /** Get the day of the month. */
    fun day(): Int = localDateTime.day()

    /** Get the day of the year. */
    fun ordinal(): Int = localDateTime.ordinal()

    /** Get the ISO 8601 year and week number. */
    fun isoYearWeek(): Pair<Int, Int> = localDateTime.isoYearWeek()

    /** Get the ISO week number. */
    fun isoWeek(): Int = localDateTime.isoWeek()

    /** Get the week number where week 1 begins on the first Sunday. */
    fun sundayBasedWeek(): Int = localDateTime.sundayBasedWeek()

    /** Get the week number where week 1 begins on the first Monday. */
    fun mondayBasedWeek(): Int = localDateTime.mondayBasedWeek()

    /** Get the year, month, and day. */
    fun toCalendarDate(): Triple<Int, Month, Int> = localDateTime.toCalendarDate()

    /** Get the year and ordinal day number. */
    fun toOrdinalDate(): Pair<Int, Int> = localDateTime.toOrdinalDate()

    /** Get the ISO 8601 year, week number, and weekday. */
    fun toIsoWeekDate(): Triple<Int, Int, Weekday> = localDateTime.toIsoWeekDate()

    /** Get the weekday. */
    fun weekday(): Weekday = localDateTime.weekday()

    /** Get the clock hour. */
    fun hour(): Int = localDateTime.hour()

    /** Get the minute within the hour. */
    fun minute(): Int = localDateTime.minute()

    /** Get the second within the minute. */
    fun second(): Int = localDateTime.second()

    /** Get the milliseconds within the second. */
    fun millisecond(): Int = localDateTime.millisecond()

    /** Get the microseconds within the second. */
    fun microsecond(): Int = localDateTime.microsecond()

    /** Get the nanoseconds within the second. */
    fun nanosecond(): Int = localDateTime.nanosecond()

    /** Get the clock hour, minute, and second. */
    fun asHms(): Triple<Int, Int, Int> = localDateTime.asHms()

    /** Get the clock hour, minute, second, and millisecond. */
    fun asHmsMilli(): List<Int> = localDateTime.asHmsMilli()

    /** Get the clock hour, minute, second, and microsecond. */
    fun asHmsMicro(): List<Int> = localDateTime.asHmsMicro()

    /** Get the clock hour, minute, second, and nanosecond. */
    fun asHmsNano(): List<Int> = localDateTime.asHmsNano()

    /** Get the Julian day for the date. */
    fun toJulianDay(): Int = date().toJulianDay()

    /** Get the Unix timestamp in seconds. */
    fun unixTimestamp(): Long {
        val days = (toJulianDay() - UNIX_EPOCH_JULIAN_DAY).toLong()
        val seconds = (hour().toLong() * 3600L) + (minute().toLong() * 60L) + second().toLong()
        return (days * 86_400L) + seconds - offset.wholeSeconds().toLong()
    }

    /** Get the Unix timestamp in nanoseconds. */
    fun unixTimestampNanos(): Long =
        (unixTimestamp() * 1_000_000_000L) + nanosecond().toLong()

    /** Get the 128-bit Unix timestamp in nanoseconds as a decimal string. */
    fun unixTimestampNanosString(): String {
        val (isNeg, absStr) = computeTimestampNanosSignAndAbs(unixTimestamp(), nanosecond())
        return if (isNeg) "-$absStr" else absStr
    }

    /** Computes self + duration, returning null if overflow occurred. */
    fun checkedAdd(duration: Duration): OffsetDateTime? =
        localDateTime.checkedAdd(duration)?.let { OffsetDateTime(it, offset) }

    /** Computes self - duration, returning null if overflow occurred. */
    fun checkedSub(duration: Duration): OffsetDateTime? =
        localDateTime.checkedSub(duration)?.let { OffsetDateTime(it, offset) }

    /** Computes self + duration, saturating on overflow. */
    fun saturatingAdd(duration: Duration): OffsetDateTime =
        checkedAdd(duration) ?: if (duration.isNegative()) newInOffset(Date.MIN, Time.MIDNIGHT, offset) else newInOffset(Date.MAX, Time.MAX, offset)

    /** Computes self - duration, saturating on overflow. */
    fun saturatingSub(duration: Duration): OffsetDateTime =
        checkedSub(duration) ?: if (duration.isNegative()) newInOffset(Date.MAX, Time.MAX, offset) else newInOffset(Date.MIN, Time.MIDNIGHT, offset)

    operator fun plus(duration: Duration): OffsetDateTime =
        checkedAdd(duration) ?: throw ArithmeticException("resulting value is out of range")

    operator fun plus(duration: kotlin.time.Duration): OffsetDateTime =
        OffsetDateTime(localDateTime + duration, offset)

    operator fun minus(duration: Duration): OffsetDateTime =
        checkedSub(duration) ?: throw ArithmeticException("resulting value is out of range")

    operator fun minus(duration: kotlin.time.Duration): OffsetDateTime =
        OffsetDateTime(localDateTime - duration, offset)

    operator fun minus(other: OffsetDateTime): Duration {
        val base = localDateTime - other.localDateTime
        val adjustment = Duration.seconds(offset.wholeSeconds().toLong() - other.offset.wholeSeconds().toLong())
        return base - adjustment
    }

    operator fun minus(other: UtcDateTime): Duration =
        this - from(other)

    operator fun minus(other: kotlin.time.Instant): Duration =
        this - from(other)

    fun replaceYear(year: Int): Result<OffsetDateTime> =
        localDateTime.replaceYear(year).map { OffsetDateTime(it, offset) }

    fun replaceMonth(month: Month): Result<OffsetDateTime> =
        localDateTime.replaceMonth(month).map { OffsetDateTime(it, offset) }

    fun replaceDay(day: Int): Result<OffsetDateTime> =
        localDateTime.replaceDay(day).map { OffsetDateTime(it, offset) }

    fun replaceOrdinal(ordinal: Int): Result<OffsetDateTime> =
        localDateTime.replaceOrdinal(ordinal).map { OffsetDateTime(it, offset) }

    fun replaceDate(newDate: Date): OffsetDateTime = OffsetDateTime(localDateTime.replaceDate(newDate), offset)

    fun replaceTime(newTime: Time): OffsetDateTime = OffsetDateTime(localDateTime.replaceTime(newTime), offset)

    fun replaceDateTime(newDateTime: PrimitiveDateTime): OffsetDateTime = newDateTime.assumeOffset(offset)

    fun replaceOffset(newOffset: UtcOffset): OffsetDateTime = OffsetDateTime(localDateTime, newOffset)

    fun truncateToDay(): OffsetDateTime = OffsetDateTime(localDateTime.truncateToDay(), offset)

    fun replaceHour(hour: Int): Result<OffsetDateTime> =
        localDateTime.replaceHour(hour).map { OffsetDateTime(it, offset) }

    fun truncateToHour(): OffsetDateTime = OffsetDateTime(localDateTime.truncateToHour(), offset)

    fun replaceMinute(minute: Int): Result<OffsetDateTime> =
        localDateTime.replaceMinute(minute).map { OffsetDateTime(it, offset) }

    fun truncateToMinute(): OffsetDateTime = OffsetDateTime(localDateTime.truncateToMinute(), offset)

    fun replaceSecond(second: Int): Result<OffsetDateTime> =
        localDateTime.replaceSecond(second).map { OffsetDateTime(it, offset) }

    fun truncateToSecond(): OffsetDateTime = OffsetDateTime(localDateTime.truncateToSecond(), offset)

    fun replaceMillisecond(millisecond: Int): Result<OffsetDateTime> =
        localDateTime.replaceMillisecond(millisecond).map { OffsetDateTime(it, offset) }

    fun truncateToMillisecond(): OffsetDateTime = OffsetDateTime(localDateTime.truncateToMillisecond(), offset)

    fun replaceMicrosecond(microsecond: Int): Result<OffsetDateTime> =
        localDateTime.replaceMicrosecond(microsecond).map { OffsetDateTime(it, offset) }

    fun truncateToMicrosecond(): OffsetDateTime = OffsetDateTime(localDateTime.truncateToMicrosecond(), offset)

    fun replaceNanosecond(nanosecond: Int): Result<OffsetDateTime> =
        localDateTime.replaceNanosecond(nanosecond).map { OffsetDateTime(it, offset) }

    internal fun isValidLeapSecondStandIn(): Boolean {
        if (nanosecond() != 999_999_999) return false
        val utc = checkedToUtc() ?: return false
        return utc.isValidLeapSecondStandIn()
    }

    override fun compareTo(other: OffsetDateTime): Int {
        val secCmp = unixTimestamp().compareTo(other.unixTimestamp())
        return if (secCmp != 0) secCmp else nanosecond().compareTo(other.nanosecond())
    }

    override fun equals(other: Any?): Boolean =
        when (other) {
            is OffsetDateTime ->
                unixTimestamp() == other.unixTimestamp() && nanosecond() == other.nanosecond()
            is UtcDateTime ->
                unixTimestamp() == other.unixTimestamp() && nanosecond() == other.nanosecond()
            else -> false
        }

    override fun hashCode(): Int = 31 * unixTimestamp().hashCode() + nanosecond()

    override fun toString(): String = "$localDateTime $offset"

    /** Format the [OffsetDateTime] using the provided [Formattable] into the given [output] buffer. */
    fun formatInto(
        output: StringBuilder,
        description: Formattable,
    ): Result<Int> = description.formatInto(output, this)

    fun formatInto(
        output: StringBuilder,
        description: List<BorrowedFormatItem>,
    ): Result<Int> = BorrowedFormatItemSliceFormattable(description).formatInto(output, this)

    /** Format the [OffsetDateTime] using the provided [Formattable]. */
    fun format(description: Formattable): Result<String> = description.format(this)

    fun format(description: List<BorrowedFormatItem>): Result<String> =
        BorrowedFormatItemSliceFormattable(description).format(this)

    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
        if (serializer.isHumanReadable()) {
            val s =
                format(OFFSET_DATE_TIME_FORMAT).getOrElse {
                    return SerdeResult.failure(SerdeError.custom("failed formatting `OffsetDateTime`"))
                }
            return serializer.serializeStr(s)
        }
        return serdeCatching {
            val tuple = serializer.serializeTuple(9).getOrThrow()
            tuple.serializeElement(I32Serialize(year())).getOrThrow()
            tuple.serializeElement(U16Serialize(ordinal().toUShort())).getOrThrow()
            tuple.serializeElement(U8Serialize(hour().toUByte())).getOrThrow()
            tuple.serializeElement(U8Serialize(minute().toUByte())).getOrThrow()
            tuple.serializeElement(U8Serialize(second().toUByte())).getOrThrow()
            tuple.serializeElement(U32Serialize(nanosecond().toUInt())).getOrThrow()
            tuple.serializeElement(I8Serialize(offset().wholeHours().toByte())).getOrThrow()
            tuple.serializeElement(I8Serialize(offset().minutesPastHour().toByte())).getOrThrow()
            tuple.serializeElement(I8Serialize(offset().secondsPastMinute().toByte())).getOrThrow()
            tuple.end().getOrThrow()
        }
    }
}
