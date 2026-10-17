// port-lint: source primitive_date_time.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatdescription.OwnedFormatItem
import io.github.kotlinmania.time.formatting.BorrowedFormatItemSliceFormattable
import io.github.kotlinmania.time.formatting.Formattable
import io.github.kotlinmania.time.formatting.OwnedFormatItemSliceFormattable
import io.github.kotlinmania.time.parsing.BorrowedFormatItemSliceParsable
import io.github.kotlinmania.time.parsing.OwnedFormatItemSliceParsable
import io.github.kotlinmania.time.parsing.Parsable
import kotlin.jvm.JvmName

/**
 * Combined date and time.
 */
class PrimitiveDateTime internal constructor(
    private val date: Date,
    private val time: Time,
) : Comparable<PrimitiveDateTime> {
    companion object {
        /** The smallest value that can be represented by [PrimitiveDateTime]. */
        val MIN: PrimitiveDateTime = PrimitiveDateTime(Date.MIN, Time.MIDNIGHT)

        /** The largest value that can be represented by [PrimitiveDateTime]. */
        val MAX: PrimitiveDateTime = PrimitiveDateTime(Date.MAX, Time.MAX)

        /** Create a new [PrimitiveDateTime] from a [Date] and [Time]. */
        fun new(date: Date, time: Time): PrimitiveDateTime = PrimitiveDateTime(date, time)

        /** Parse a [PrimitiveDateTime] from the input using the provided [Parsable] format description. */
        fun parse(input: String, description: Parsable): Result<PrimitiveDateTime> =
            description.parseDateTime(input.encodeToByteArray())

        fun parse(input: ByteArray, description: Parsable): Result<PrimitiveDateTime> =
            description.parseDateTime(input)

        @JvmName("parseBorrowedSlice")
        fun parse(input: String, description: List<BorrowedFormatItem>): Result<PrimitiveDateTime> =
            BorrowedFormatItemSliceParsable(description).parseDateTime(input.encodeToByteArray())

        @JvmName("parseBytesBorrowedSlice")
        fun parse(input: ByteArray, description: List<BorrowedFormatItem>): Result<PrimitiveDateTime> =
            BorrowedFormatItemSliceParsable(description).parseDateTime(input)

        @JvmName("parseOwnedSlice")
        fun parse(input: String, description: List<OwnedFormatItem>): Result<PrimitiveDateTime> =
            OwnedFormatItemSliceParsable(description).parseDateTime(input.encodeToByteArray())

        @JvmName("parseBytesOwnedSlice")
        fun parse(input: ByteArray, description: List<OwnedFormatItem>): Result<PrimitiveDateTime> =
            OwnedFormatItemSliceParsable(description).parseDateTime(input)
    }

    /** Get the date component. */
    fun date(): Date = date

    /** Get the time component. */
    fun time(): Time = time

    /** Get the year of the date. */
    fun year(): Int = date.year()

    /** Get the month. */
    fun month(): Month = date.month()

    /** Get the day of the month. */
    fun day(): Int = date.day()

    /** Get the day of the year. */
    fun ordinal(): Int = date.ordinal()

    /** Get the ISO 8601 year and week number. */
    fun isoYearWeek(): Pair<Int, Int> = date.isoYearWeek()

    /** Get the ISO week number. */
    fun isoWeek(): Int = date.isoWeek()

    /** Get the week number where week 1 begins on the first Sunday. */
    fun sundayBasedWeek(): Int = date.sundayBasedWeek()

    /** Get the week number where week 1 begins on the first Monday. */
    fun mondayBasedWeek(): Int = date.mondayBasedWeek()

    /** Get the year, month, and day. */
    fun toCalendarDate(): Triple<Int, Month, Int> = date.toCalendarDate()

    /** Get the year and ordinal day number. */
    fun toOrdinalDate(): Pair<Int, Int> = date.toOrdinalDate()

    /** Get the ISO 8601 year, week number, and weekday. */
    fun toIsoWeekDate(): Triple<Int, Int, Weekday> = date.toIsoWeekDate()

    /** Get the weekday. */
    fun weekday(): Weekday = date.weekday()

    /** Get the Julian day for the date. The time is not taken into account for this calculation. */
    fun toJulianDay(): Int = date.toJulianDay()

    /** Get the clock hour. */
    fun hour(): Int = time.hour()

    /** Get the minute within the hour. */
    fun minute(): Int = time.minute()

    /** Get the second within the minute. */
    fun second(): Int = time.second()

    /** Get the milliseconds within the second. */
    fun millisecond(): Int = time.millisecond()

    /** Get the microseconds within the second. */
    fun microsecond(): Int = time.microsecond()

    /** Get the nanoseconds within the second. */
    fun nanosecond(): Int = time.nanosecond()

    /** Get the clock hour, minute, and second. */
    fun asHms(): Triple<Int, Int, Int> = time.asHms()

    /** Get the clock hour, minute, second, and millisecond. */
    fun asHmsMilli(): List<Int> = time.asHmsMilli()

    /** Get the clock hour, minute, second, and microsecond. */
    fun asHmsMicro(): List<Int> = time.asHmsMicro()

    /** Get the clock hour, minute, second, and nanosecond. */
    fun asHmsNano(): List<Int> = time.asHmsNano()

    /** Assume the datetime is in UTC, creating an [OffsetDateTime]. */
    fun assumeUtc(): OffsetDateTime = OffsetDateTime.newInOffset(date, time, UtcOffset.UTC)

    /** Assume the datetime is in the provided offset, creating an [OffsetDateTime]. */
    fun assumeOffset(offset: UtcOffset): OffsetDateTime = OffsetDateTime.newInOffset(date, time, offset)

    /** Assume the datetime represents a moment in UTC, returning a [UtcDateTime]. */
    fun asUtc(): UtcDateTime = UtcDateTime.fromPrimitive(this)

    /** Computes self + duration, returning null if overflow occurred. */
    fun checkedAdd(duration: Duration): PrimitiveDateTime? {
        val (dateAdjustment, newTime) = time.adjustingAdd(duration)
        val baseDate = date.checkedAdd(duration) ?: return null
        val newDate =
            when (dateAdjustment) {
                DateAdjustment.Previous -> baseDate.previousDay() ?: return null
                DateAdjustment.Next -> baseDate.nextDay() ?: return null
                DateAdjustment.None -> baseDate
            }
        return PrimitiveDateTime(newDate, newTime)
    }

    /** Computes self - duration, returning null if overflow occurred. */
    fun checkedSub(duration: Duration): PrimitiveDateTime? {
        val (dateAdjustment, newTime) = time.adjustingSub(duration)
        val baseDate = date.checkedSub(duration) ?: return null
        val newDate =
            when (dateAdjustment) {
                DateAdjustment.Previous -> baseDate.previousDay() ?: return null
                DateAdjustment.Next -> baseDate.nextDay() ?: return null
                DateAdjustment.None -> baseDate
            }
        return PrimitiveDateTime(newDate, newTime)
    }

    /** Computes self + duration, saturating on overflow. */
    fun saturatingAdd(duration: Duration): PrimitiveDateTime =
        checkedAdd(duration) ?: if (duration.isNegative()) MIN else MAX

    /** Computes self - duration, saturating on overflow. */
    fun saturatingSub(duration: Duration): PrimitiveDateTime =
        checkedSub(duration) ?: if (duration.isNegative()) MAX else MIN

    operator fun plus(duration: Duration): PrimitiveDateTime =
        checkedAdd(duration) ?: throw ArithmeticException("resulting value is out of range")

    operator fun plus(duration: kotlin.time.Duration): PrimitiveDateTime =
        duration.toComponents { seconds, nanoseconds ->
            checkedAdd(Duration.new(seconds, nanoseconds))
                ?: throw ArithmeticException("resulting value is out of range")
        }

    operator fun minus(duration: Duration): PrimitiveDateTime =
        checkedSub(duration) ?: throw ArithmeticException("resulting value is out of range")

    operator fun minus(duration: kotlin.time.Duration): PrimitiveDateTime =
        duration.toComponents { seconds, nanoseconds ->
            checkedSub(Duration.new(seconds, nanoseconds))
                ?: throw ArithmeticException("resulting value is out of range")
        }

    operator fun minus(other: PrimitiveDateTime): Duration =
        (date - other.date) + (time - other.time)

    fun replaceYear(year: Int): Result<PrimitiveDateTime> =
        date.replaceYear(year).map { PrimitiveDateTime(it, time) }

    fun replaceMonth(month: Month): Result<PrimitiveDateTime> =
        date.replaceMonth(month).map { PrimitiveDateTime(it, time) }

    fun replaceDay(day: Int): Result<PrimitiveDateTime> =
        date.replaceDay(day).map { PrimitiveDateTime(it, time) }

    fun replaceOrdinal(ordinal: Int): Result<PrimitiveDateTime> =
        date.replaceOrdinal(ordinal).map { PrimitiveDateTime(it, time) }

    fun replaceDate(newDate: Date): PrimitiveDateTime = PrimitiveDateTime(newDate, time)

    fun replaceTime(newTime: Time): PrimitiveDateTime = PrimitiveDateTime(date, newTime)

    fun truncateToDay(): PrimitiveDateTime = replaceTime(Time.MIDNIGHT)

    fun replaceHour(hour: Int): Result<PrimitiveDateTime> =
        time.replaceHour(hour).map { PrimitiveDateTime(date, it) }

    fun truncateToHour(): PrimitiveDateTime = replaceTime(time.truncateToHour())

    fun replaceMinute(minute: Int): Result<PrimitiveDateTime> =
        time.replaceMinute(minute).map { PrimitiveDateTime(date, it) }

    fun truncateToMinute(): PrimitiveDateTime = replaceTime(time.truncateToMinute())

    fun replaceSecond(second: Int): Result<PrimitiveDateTime> =
        time.replaceSecond(second).map { PrimitiveDateTime(date, it) }

    fun truncateToSecond(): PrimitiveDateTime = replaceTime(time.truncateToSecond())

    fun replaceMillisecond(millisecond: Int): Result<PrimitiveDateTime> =
        time.replaceMillisecond(millisecond).map { PrimitiveDateTime(date, it) }

    fun truncateToMillisecond(): PrimitiveDateTime = replaceTime(time.truncateToMillisecond())

    fun replaceMicrosecond(microsecond: Int): Result<PrimitiveDateTime> =
        time.replaceMicrosecond(microsecond).map { PrimitiveDateTime(date, it) }

    fun truncateToMicrosecond(): PrimitiveDateTime = replaceTime(time.truncateToMicrosecond())

    fun replaceNanosecond(nanosecond: Int): Result<PrimitiveDateTime> =
        time.replaceNanosecond(nanosecond).map { PrimitiveDateTime(date, it) }

    override fun compareTo(other: PrimitiveDateTime): Int {
        val dateCmp = date.compareTo(other.date)
        return if (dateCmp != 0) dateCmp else time.compareTo(other.time)
    }

    override fun equals(other: Any?): Boolean =
        other is PrimitiveDateTime && date == other.date && time == other.time

    override fun hashCode(): Int = 31 * date.hashCode() + time.hashCode()

    override fun toString(): String = "$date $time"

    /** Format the [PrimitiveDateTime] using the provided [Formattable] into the given [output] buffer. */
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

    /** Format the [PrimitiveDateTime] using the provided [Formattable]. */
    fun format(description: Formattable): Result<String> = description.format(this)

    @JvmName("formatBorrowedSlice")
    fun format(description: List<BorrowedFormatItem>): Result<String> =
        BorrowedFormatItemSliceFormattable(description).format(this)

    @JvmName("formatOwnedSlice")
    fun format(description: List<OwnedFormatItem>): Result<String> =
        OwnedFormatItemSliceFormattable(description).format(this)
}
