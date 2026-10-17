// port-lint: source utc_date_time.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.error.ComponentRange
import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatdescription.OwnedFormatItem
import io.github.kotlinmania.time.formatting.BorrowedFormatItemSliceFormattable
import io.github.kotlinmania.time.formatting.Formattable
import io.github.kotlinmania.time.formatting.OwnedFormatItemSliceFormattable
import io.github.kotlinmania.time.formatting.computeTimestampNanosSignAndAbs
import io.github.kotlinmania.time.parsing.BorrowedFormatItemSliceParsable
import io.github.kotlinmania.time.parsing.OwnedFormatItemSliceParsable
import io.github.kotlinmania.time.parsing.Parsable
import io.github.kotlinmania.time.parsing.parseNanosStringToSecAndNano
import kotlin.jvm.JvmName

/** The Julian day of the Unix epoch. */
private const val UNIX_EPOCH_JULIAN_DAY: Int = 2_440_588

/**
 * A [PrimitiveDateTime] that is known to be UTC.
 */
class UtcDateTime internal constructor(
    private val inner: PrimitiveDateTime,
) : Comparable<UtcDateTime> {
    companion object {
        /** Midnight, 1 January, 1970. */
        val UNIX_EPOCH: UtcDateTime = new(Date.UNIX_EPOCH, Time.MIDNIGHT)

        /** The smallest value that can be represented by [UtcDateTime]. */
        val MIN: UtcDateTime = new(Date.MIN, Time.MIDNIGHT)

        /** The largest value that can be represented by [UtcDateTime]. */
        val MAX: UtcDateTime = new(Date.MAX, Time.MAX)

        /** Create a new [UtcDateTime] with the current date and time. */
        fun now(): UtcDateTime = from(kotlin.time.Clock.System.now())

        /** Create a new [UtcDateTime] from the provided [Date] and [Time]. */
        fun new(date: Date, time: Time): UtcDateTime =
            UtcDateTime(PrimitiveDateTime.new(date, time))

        /**
         * Create a new [UtcDateTime] from the [PrimitiveDateTime], assuming that the latter is UTC.
         */
        internal fun fromPrimitive(dateTime: PrimitiveDateTime): UtcDateTime =
            UtcDateTime(dateTime)

        /** Convert an [OffsetDateTime] to a [UtcDateTime]. */
        fun from(datetime: OffsetDateTime): UtcDateTime =
            datetime.toUtc()

        /** Convert a [kotlin.time.Instant] to a [UtcDateTime]. */
        fun from(systemTime: kotlin.time.Instant): UtcDateTime {
            val seconds = systemTime.epochSeconds
            val nanos = systemTime.nanosecondsOfSecond
            return fromUnixTimestamp(seconds).getOrThrow() + Duration.nanoseconds(nanos.toLong())
        }

        /** Create a [UtcDateTime] from the provided Unix timestamp. */
        fun fromUnixTimestamp(timestamp: Long): Result<UtcDateTime> {
            if (timestamp < MIN.unixTimestamp() || timestamp > MAX.unixTimestamp()) {
                return Result.failure(ComponentRange.conditional("timestamp"))
            }
            val secondsInDay = 86_400L
            val dayAdjustment = floorDiv(timestamp, secondsInDay).toInt()
            val secondsWithinDay = floorMod(timestamp, secondsInDay).toInt()

            val date = Date.fromJulianDayUnchecked(UNIX_EPOCH_JULIAN_DAY + dayAdjustment)
            val time =
                Time.fromHmsNanosUnchecked(
                    secondsWithinDay / 3600,
                    (secondsWithinDay % 3600) / 60,
                    secondsWithinDay % 60,
                    0,
                )
            return Result.success(new(date, time))
        }

        /** Construct a [UtcDateTime] from the provided Unix timestamp (in nanoseconds). */
        fun fromUnixTimestampNanos(timestamp: Long): Result<UtcDateTime> {
            val nanosInSecond = 1_000_000_000L
            val seconds = floorDiv(timestamp, nanosInSecond)
            val nanos = floorMod(timestamp, nanosInSecond).toInt()

            val datetime =
                fromUnixTimestamp(seconds).getOrElse {
                    return Result.failure(it)
                }
            return Result.success(
                new(
                    datetime.date(),
                    Time.fromHmsNanosUnchecked(
                        datetime.hour(),
                        datetime.minute(),
                        datetime.second(),
                        nanos,
                    ),
                ),
            )
        }

        /** Construct a [UtcDateTime] from the provided 128-bit Unix timestamp (in nanoseconds) as a decimal string. */
        fun fromUnixTimestampNanos(timestamp: String): Result<UtcDateTime> {
            val (seconds, nanos) =
                parseNanosStringToSecAndNano(timestamp)
                    ?: return Result.failure(ComponentRange.conditional("timestamp"))
            val datetime =
                fromUnixTimestamp(seconds).getOrElse {
                    return Result.failure(it)
                }
            return Result.success(
                new(
                    datetime.date(),
                    Time.fromHmsNanosUnchecked(
                        datetime.hour(),
                        datetime.minute(),
                        datetime.second(),
                        nanos,
                    ),
                ),
            )
        }

        /** Parse a [UtcDateTime] from the input using the provided [Parsable] format description. */
        fun parse(input: String, description: Parsable): Result<UtcDateTime> =
            description.parseUtcDateTime(input.encodeToByteArray())

        fun parse(input: ByteArray, description: Parsable): Result<UtcDateTime> =
            description.parseUtcDateTime(input)

        @JvmName("parseBorrowedSlice")
        fun parse(input: String, description: List<BorrowedFormatItem>): Result<UtcDateTime> =
            BorrowedFormatItemSliceParsable(description).parseUtcDateTime(input.encodeToByteArray())

        @JvmName("parseBytesBorrowedSlice")
        fun parse(input: ByteArray, description: List<BorrowedFormatItem>): Result<UtcDateTime> =
            BorrowedFormatItemSliceParsable(description).parseUtcDateTime(input)

        @JvmName("parseOwnedSlice")
        fun parse(input: String, description: List<OwnedFormatItem>): Result<UtcDateTime> =
            OwnedFormatItemSliceParsable(description).parseUtcDateTime(input.encodeToByteArray())

        @JvmName("parseBytesOwnedSlice")
        fun parse(input: ByteArray, description: List<OwnedFormatItem>): Result<UtcDateTime> =
            OwnedFormatItemSliceParsable(description).parseUtcDateTime(input)
    }

    /**
     * Obtain the [PrimitiveDateTime] that this [UtcDateTime] represents. The no-longer-attached
     * [UtcOffset] is assumed to be UTC.
     */
    internal fun asPrimitive(): PrimitiveDateTime = inner

    /**
     * Convert the [UtcDateTime] from UTC to the provided [UtcOffset], returning an
     * [OffsetDateTime].
     */
    fun toOffset(offset: UtcOffset): OffsetDateTime =
        checkedToOffset(offset) ?: throw ArithmeticException("local datetime out of valid range")

    /**
     * Convert the [UtcDateTime] from UTC to the provided [UtcOffset], returning an
     * [OffsetDateTime]. `null` is returned if the date-time in the resulting offset is invalid.
     */
    fun checkedToOffset(offset: UtcOffset): OffsetDateTime? {
        if (offset.isUtc()) {
            return inner.assumeUtc()
        }
        val adjusted = inner.checkedAdd(Duration.seconds(offset.wholeSeconds().toLong())) ?: return null
        return adjusted.assumeOffset(offset)
    }

    /** Convert this [UtcDateTime] to a [kotlin.time.Instant]. */
    fun toSystemTime(): kotlin.time.Instant =
        kotlin.time.Instant.fromEpochSeconds(unixTimestamp(), nanosecond())

    /** Get the Unix timestamp. */
    fun unixTimestamp(): Long {
        val days = (toJulianDay().toLong() - UNIX_EPOCH_JULIAN_DAY.toLong()) * 86_400L
        val hours = hour().toLong() * 3_600L
        val minutes = minute().toLong() * 60L
        val seconds = second().toLong()
        return days + hours + minutes + seconds
    }

    /** Get the Unix timestamp in nanoseconds. */
    fun unixTimestampNanos(): Long =
        unixTimestamp() * 1_000_000_000L + nanosecond().toLong()

    /** Get the 128-bit Unix timestamp in nanoseconds as a decimal string. */
    fun unixTimestampNanosString(): String {
        val (isNeg, absStr) = computeTimestampNanosSignAndAbs(unixTimestamp(), nanosecond())
        return if (isNeg) "-$absStr" else absStr
    }

    /** Get the [Date] component of the [UtcDateTime]. */
    fun date(): Date = inner.date()

    /** Get the [Time] component of the [UtcDateTime]. */
    fun time(): Time = inner.time()

    /** Get the year of the date. */
    fun year(): Int = date().year()

    /** Get the month of the date. */
    fun month(): Month = date().month()

    /** Get the day of the date. */
    fun day(): Int = date().day()

    /** Get the day of the year. */
    fun ordinal(): Int = date().ordinal()

    /** Get the ISO 8601 year and week number. */
    fun isoYearWeek(): Pair<Int, Int> = date().isoYearWeek()

    /** Get the ISO week number. */
    fun isoWeek(): Int = date().isoWeek()

    /** Get the week number where week 1 begins on the first Sunday. */
    fun sundayBasedWeek(): Int = date().sundayBasedWeek()

    /** Get the week number where week 1 begins on the first Monday. */
    fun mondayBasedWeek(): Int = date().mondayBasedWeek()

    /** Get the year, month, and day. */
    fun toCalendarDate(): Triple<Int, Month, Int> = date().toCalendarDate()

    /** Get the year and ordinal day number. */
    fun toOrdinalDate(): Pair<Int, Int> = date().toOrdinalDate()

    /** Get the ISO 8601 year, week number, and weekday. */
    fun toIsoWeekDate(): Triple<Int, Int, Weekday> = date().toIsoWeekDate()

    /** Get the weekday. */
    fun weekday(): Weekday = date().weekday()

    /** Get the Julian day for the date. The time is not taken into account for this calculation. */
    fun toJulianDay(): Int = date().toJulianDay()

    /** Get the clock hour, minute, and second. */
    fun asHms(): Triple<Int, Int, Int> = time().asHms()

    /** Get the clock hour, minute, second, and millisecond. */
    fun asHmsMilli(): List<Int> = time().asHmsMilli()

    /** Get the clock hour, minute, second, and microsecond. */
    fun asHmsMicro(): List<Int> = time().asHmsMicro()

    /** Get the clock hour, minute, second, and nanosecond. */
    fun asHmsNano(): List<Int> = time().asHmsNano()

    /** Get the clock hour. */
    fun hour(): Int = time().hour()

    /** Get the minute within the hour. */
    fun minute(): Int = time().minute()

    /** Get the second within the minute. */
    fun second(): Int = time().second()

    /** Get the milliseconds within the second. */
    fun millisecond(): Int = time().millisecond()

    /** Get the microseconds within the second. */
    fun microsecond(): Int = time().microsecond()

    /** Get the nanoseconds within the second. */
    fun nanosecond(): Int = time().nanosecond()

    /** Computes `self + duration`, returning `null` if an overflow occurred. */
    fun checkedAdd(duration: Duration): UtcDateTime? =
        inner.checkedAdd(duration)?.let { fromPrimitive(it) }

    /** Computes `self - duration`, returning `null` if an overflow occurred. */
    fun checkedSub(duration: Duration): UtcDateTime? =
        inner.checkedSub(duration)?.let { fromPrimitive(it) }

    /** Computes `self + duration`, saturating value on overflow. */
    fun saturatingAdd(duration: Duration): UtcDateTime =
        fromPrimitive(inner.saturatingAdd(duration))

    /** Computes `self - duration`, saturating value on overflow. */
    fun saturatingSub(duration: Duration): UtcDateTime =
        fromPrimitive(inner.saturatingSub(duration))

    /** Replace the time, preserving the date. */
    fun replaceTime(time: Time): UtcDateTime =
        fromPrimitive(inner.replaceTime(time))

    /** Replace the date, preserving the time. */
    fun replaceDate(date: Date): UtcDateTime =
        fromPrimitive(inner.replaceDate(date))

    /** Replace the year. The month and day will be unchanged. */
    fun replaceYear(year: Int): Result<UtcDateTime> =
        inner.replaceYear(year).map { fromPrimitive(it) }

    /** Replace the month of the year. */
    fun replaceMonth(month: Month): Result<UtcDateTime> =
        inner.replaceMonth(month).map { fromPrimitive(it) }

    /** Replace the day of the month. */
    fun replaceDay(day: Int): Result<UtcDateTime> =
        inner.replaceDay(day).map { fromPrimitive(it) }

    /** Replace the day of the year. */
    fun replaceOrdinal(ordinal: Int): Result<UtcDateTime> =
        inner.replaceOrdinal(ordinal).map { fromPrimitive(it) }

    /** Truncate to the start of the day, setting the time to midnight. */
    fun truncateToDay(): UtcDateTime =
        fromPrimitive(inner.truncateToDay())

    /** Replace the clock hour. */
    fun replaceHour(hour: Int): Result<UtcDateTime> =
        inner.replaceHour(hour).map { fromPrimitive(it) }

    /** Truncate to the hour, setting the minute, second, and subsecond components to zero. */
    fun truncateToHour(): UtcDateTime =
        fromPrimitive(inner.truncateToHour())

    /** Replace the minutes within the hour. */
    fun replaceMinute(minute: Int): Result<UtcDateTime> =
        inner.replaceMinute(minute).map { fromPrimitive(it) }

    /** Truncate to the minute, setting the second and subsecond components to zero. */
    fun truncateToMinute(): UtcDateTime =
        fromPrimitive(inner.truncateToMinute())

    /** Replace the seconds within the minute. */
    fun replaceSecond(second: Int): Result<UtcDateTime> =
        inner.replaceSecond(second).map { fromPrimitive(it) }

    /** Truncate to the second, setting the subsecond components to zero. */
    fun truncateToSecond(): UtcDateTime =
        fromPrimitive(inner.truncateToSecond())

    /** Replace the milliseconds within the second. */
    fun replaceMillisecond(millisecond: Int): Result<UtcDateTime> =
        inner.replaceMillisecond(millisecond).map { fromPrimitive(it) }

    /** Truncate to the millisecond, setting the microsecond and nanosecond components to zero. */
    fun truncateToMillisecond(): UtcDateTime =
        fromPrimitive(inner.truncateToMillisecond())

    /** Replace the microseconds within the second. */
    fun replaceMicrosecond(microsecond: Int): Result<UtcDateTime> =
        inner.replaceMicrosecond(microsecond).map { fromPrimitive(it) }

    /** Truncate to the microsecond, setting the nanosecond component to zero. */
    fun truncateToMicrosecond(): UtcDateTime =
        fromPrimitive(inner.truncateToMicrosecond())

    /** Replace the nanoseconds within the second. */
    fun replaceNanosecond(nanosecond: Int): Result<UtcDateTime> =
        inner.replaceNanosecond(nanosecond).map { fromPrimitive(it) }

    internal fun isValidLeapSecondStandIn(): Boolean {
        val dt = inner
        return dt.hour() == 23 &&
            dt.minute() == 59 &&
            dt.second() == 59 &&
            dt.nanosecond() == 999_999_999 &&
            dt.day() == dt.month().length(dt.year())
    }

    operator fun plus(duration: Duration): UtcDateTime =
        (inner + duration).asUtc()

    operator fun plus(duration: kotlin.time.Duration): UtcDateTime =
        (inner + duration).asUtc()

    operator fun minus(duration: Duration): UtcDateTime =
        checkedSub(duration) ?: throw ArithmeticException("resulting value is out of range")

    operator fun minus(duration: kotlin.time.Duration): UtcDateTime =
        fromPrimitive(inner - duration)

    operator fun minus(other: UtcDateTime): Duration =
        inner - other.inner

    operator fun minus(other: OffsetDateTime): Duration =
        OffsetDateTime.from(this) - other

    operator fun minus(other: kotlin.time.Instant): Duration =
        this - from(other)

    override fun compareTo(other: UtcDateTime): Int =
        inner.compareTo(other.inner)

    override fun equals(other: Any?): Boolean =
        when (other) {
            is UtcDateTime -> inner == other.inner
            is OffsetDateTime ->
                unixTimestamp() == other.unixTimestamp() && nanosecond() == other.nanosecond()
            else -> false
        }

    override fun hashCode(): Int = inner.hashCode()

    override fun toString(): String = "${date()} ${time()} +00"

    /** Format the [UtcDateTime] using the provided [Formattable] into the given [output] buffer. */
    fun formatInto(
        output: StringBuilder,
        description: Formattable,
    ): Result<Int> = description.formatInto(output, this)

    @JvmName("formatIntoBorrowedSlice")
    fun formatInto(
        output: StringBuilder,
        description: List<BorrowedFormatItem>,
    ): Result<Int> = BorrowedFormatItemSliceFormattable(description).formatInto(output, this)

    @JvmName("formatIntoOwnedSlice")
    fun formatInto(
        output: StringBuilder,
        description: List<OwnedFormatItem>,
    ): Result<Int> = OwnedFormatItemSliceFormattable(description).formatInto(output, this)

    /** Format the [UtcDateTime] using the provided [Formattable]. */
    fun format(description: Formattable): Result<String> = description.format(this)

    @JvmName("formatBorrowedSlice")
    fun format(description: List<BorrowedFormatItem>): Result<String> =
        BorrowedFormatItemSliceFormattable(description).format(this)

    @JvmName("formatOwnedSlice")
    fun format(description: List<OwnedFormatItem>): Result<String> =
        OwnedFormatItemSliceFormattable(description).format(this)
}
