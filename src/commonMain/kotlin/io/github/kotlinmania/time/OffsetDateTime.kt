// port-lint: source offset_date_time.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.error.ComponentRange

/**
 * A [PrimitiveDateTime] with a [UtcOffset].
 */
class OffsetDateTime internal constructor(
    private val localDateTime: PrimitiveDateTime,
    private val offset: UtcOffset,
) : Comparable<OffsetDateTime> {
    companion object {
        /** The Julian day of the Unix epoch. */
        private const val UNIX_EPOCH_JULIAN_DAY: Int = 2_440_588

        /** Midnight, 1 January, 1970 (UTC). */
        val UNIX_EPOCH: OffsetDateTime =
            newInOffset(Date.UNIX_EPOCH, Time.MIDNIGHT, UtcOffset.UTC)

        /** Create a new [OffsetDateTime] with the given [Date], [Time], and [UtcOffset]. */
        fun newInOffset(date: Date, time: Time, offset: UtcOffset): OffsetDateTime =
            OffsetDateTime(PrimitiveDateTime.new(date, time), offset)

        /** Create a new [OffsetDateTime] with the given [Date] and [Time] in the UTC timezone. */
        fun newUtc(date: Date, time: Time): OffsetDateTime =
            newInOffset(date, time, UtcOffset.UTC)

        /** Create an [OffsetDateTime] from the provided unix timestamp in seconds. */
        fun fromUnixTimestamp(timestamp: Long): Result<OffsetDateTime> {
            val secondsInDay = 86_400L
            val dayAdjustment = floorDiv(timestamp, secondsInDay)
            val secondsOfDay = floorMod(timestamp, secondsInDay).toInt()

            val targetJulianDay = UNIX_EPOCH_JULIAN_DAY + dayAdjustment
            if (targetJulianDay < Date.MIN.toJulianDay() || targetJulianDay > Date.MAX.toJulianDay()) {
                return Result.failure(ComponentRange.conditional("unix_timestamp"))
            }

            val date =
                Date.fromJulianDay(targetJulianDay.toInt()).getOrElse {
                    return Result.failure(it)
                }
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
    }

    /** Convert the datetime to UTC. */
    fun toUtc(): OffsetDateTime = toOffset(UtcOffset.UTC)

    /** Convert the datetime from the current [UtcOffset] to the provided [UtcOffset]. */
    fun toOffset(newOffset: UtcOffset): OffsetDateTime =
        checkedToOffset(newOffset) ?: throw ArithmeticException("local datetime out of valid range")

    /**
     * Convert the datetime from the current [UtcOffset] to the provided [UtcOffset],
     * returning null if the datetime in the resulting offset is invalid.
     */
    fun checkedToOffset(newOffset: UtcOffset): OffsetDateTime? {
        val diffSeconds = newOffset.wholeSeconds().toLong() - offset.wholeSeconds().toLong()
        val adjustedLocalDateTime = localDateTime.checkedAdd(Duration.seconds(diffSeconds)) ?: return null
        return OffsetDateTime(adjustedLocalDateTime, newOffset)
    }

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
        checkedAdd(duration) ?: throw ArithmeticException("overflow adding duration to datetime")

    operator fun minus(duration: Duration): OffsetDateTime =
        checkedSub(duration) ?: throw ArithmeticException("overflow subtracting duration from datetime")

    operator fun minus(other: OffsetDateTime): Duration {
        val base = localDateTime - other.localDateTime
        val adjustment = Duration.seconds(offset.wholeSeconds().toLong() - other.offset.wholeSeconds().toLong())
        return base - adjustment
    }

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

    fun replaceOffset(newOffset: UtcOffset): OffsetDateTime = OffsetDateTime(localDateTime, newOffset)

    fun replaceHour(hour: Int): Result<OffsetDateTime> =
        localDateTime.replaceHour(hour).map { OffsetDateTime(it, offset) }

    fun replaceMinute(minute: Int): Result<OffsetDateTime> =
        localDateTime.replaceMinute(minute).map { OffsetDateTime(it, offset) }

    fun replaceSecond(second: Int): Result<OffsetDateTime> =
        localDateTime.replaceSecond(second).map { OffsetDateTime(it, offset) }

    fun replaceMillisecond(millisecond: Int): Result<OffsetDateTime> =
        localDateTime.replaceMillisecond(millisecond).map { OffsetDateTime(it, offset) }

    fun replaceMicrosecond(microsecond: Int): Result<OffsetDateTime> =
        localDateTime.replaceMicrosecond(microsecond).map { OffsetDateTime(it, offset) }

    fun replaceNanosecond(nanosecond: Int): Result<OffsetDateTime> =
        localDateTime.replaceNanosecond(nanosecond).map { OffsetDateTime(it, offset) }

    override fun compareTo(other: OffsetDateTime): Int =
        toUtc().localDateTime.compareTo(other.toUtc().localDateTime)

    override fun equals(other: Any?): Boolean =
        other is OffsetDateTime && toUtc().localDateTime == other.toUtc().localDateTime

    override fun hashCode(): Int = toUtc().localDateTime.hashCode()

    override fun toString(): String = "$localDateTime $offset"
}
