// port-lint: source date.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.error.ComponentRange
import kotlin.math.abs

/** The minimum valid year. */
internal const val MIN_YEAR: Int = -9999

/** The maximum valid year. */
internal const val MAX_YEAR: Int = 9999

/**
 * Date in the proleptic Gregorian calendar.
 *
 * Years between ±9999 inclusive are representable.
 */
class Date internal constructor(
    private val value: Int,
) : Comparable<Date> {
    init {
        require(value != 0) { "date value cannot be 0" }
    }

    internal fun asI32(): Int = value

    private fun isInLeapYear(): Boolean = ((value shr 9) and 1) == 1

    /** Get the year of the date. */
    fun year(): Int = value shr 10

    /** Get the day of the year. */
    fun ordinal(): Int = value and 0x1FF

    /** Get the month. */
    fun month(): Month {
        val ord = ordinal()
        val janFebLen = 59 + if (isInLeapYear()) 1 else 0

        val (monthAdj, ordAdj) =
            if (ord <= janFebLen) {
                Pair(0, 0)
            } else {
                Pair(2, janFebLen)
            }

        val adjustedOrd = ord - ordAdj
        val monthNum = ((adjustedOrd * 268 + 8031) shr 13) + monthAdj
        return Month.fromNumber(monthNum).getOrThrow()
    }

    /**
     * Get the day of the month.
     *
     * The returned value will always be in the range 1..=31.
     */
    fun day(): Int {
        val ord = ordinal()
        val janFebLen = 59 + if (isInLeapYear()) 1 else 0

        val ordAdj = if (ord <= janFebLen) 0 else janFebLen
        val adjustedOrd = ord - ordAdj
        val monthNum = (adjustedOrd * 268 + 8031) shr 13
        val daysInPrecedingMonths = (monthNum * 3917 - 3866) shr 7
        return adjustedOrd - daysInPrecedingMonths
    }

    /** Get the ISO 8601 year and week number. */
    fun isoYearWeek(): Pair<Int, Int> {
        val (year, ord) = toOrdinalDate()
        val week = (ord + 10 - weekday().numberFromMonday()) / 7
        return when (week) {
            0 -> Pair(year - 1, weeksInYear(year - 1))
            53 -> if (weeksInYear(year) == 52) Pair(year + 1, 1) else Pair(year, 53)
            else -> Pair(year, week)
        }
    }

    /**
     * Get the ISO week number.
     *
     * The returned value will always be in the range 1..=53.
     */
    fun isoWeek(): Int = isoYearWeek().second

    /**
     * Get the week number where week 1 begins on the first Sunday.
     *
     * The returned value will always be in the range 0..=53.
     */
    fun sundayBasedWeek(): Int =
        (ordinal() - weekday().numberDaysFromSunday() + 6) / 7

    /**
     * Get the week number where week 1 begins on the first Monday.
     *
     * The returned value will always be in the range 0..=53.
     */
    fun mondayBasedWeek(): Int =
        (ordinal() - weekday().numberDaysFromMonday() + 6) / 7

    /** Get the year, month, and day. */
    fun toCalendarDate(): Triple<Int, Month, Int> {
        val (year, ord) = toOrdinalDate()
        val janFebLen = 59 + if (isInLeapYear()) 1 else 0

        val (monthAdj, ordAdj) =
            if (ord <= janFebLen) {
                Pair(0, 0)
            } else {
                Pair(2, janFebLen)
            }

        val adjustedOrd = ord - ordAdj
        val monthNum = (adjustedOrd * 268 + 8031) shr 13
        val daysInPrecedingMonths = (monthNum * 3917 - 3866) shr 7
        val day = adjustedOrd - daysInPrecedingMonths
        val month = Month.fromNumber(monthNum + monthAdj).getOrThrow()
        return Triple(year, month, day)
    }

    /** Get the year and ordinal day number. */
    fun toOrdinalDate(): Pair<Int, Int> = Pair(year(), ordinal())

    /** Get the ISO 8601 year, week number, and weekday. */
    fun toIsoWeekDate(): Triple<Int, Int, Weekday> {
        val (year, week) = isoYearWeek()
        return Triple(year, week, weekday())
    }

    /** Get the weekday. */
    fun weekday(): Weekday =
        when (toJulianDay() % 7) {
            -6, 1 -> Weekday.Tuesday
            -5, 2 -> Weekday.Wednesday
            -4, 3 -> Weekday.Thursday
            -3, 4 -> Weekday.Friday
            -2, 5 -> Weekday.Saturday
            -1, 6 -> Weekday.Sunday
            else -> Weekday.Monday
        }

    /** Get the next calendar date. */
    fun nextDay(): Date? {
        val isLastDayOfYear = (value and 0x3FF == 365) || (value and 0x3FF == 878)
        return if (isLastDayOfYear) {
            if (value == MAX.value) {
                null
            } else {
                fromOrdinalDateUnchecked(year() + 1, 1)
            }
        } else {
            Date(value + 1)
        }
    }

    /** Get the previous calendar date. */
    fun previousDay(): Date? =
        if (ordinal() != 1) {
            Date(value - 1)
        } else if (value == MIN.value) {
            null
        } else {
            val year = year() - 1
            val isLeap = isLeapYear(year)
            fromParts(year, isLeap, if (isLeap) 366 else 365)
        }

    /** Calculates the first occurrence of a weekday strictly later than this date. */
    fun nextOccurrence(weekday: Weekday): Date =
        checkedNextOccurrence(weekday)
            ?: throw ArithmeticException("overflow calculating the next occurrence of a weekday")

    /** Calculates the first occurrence of a weekday strictly earlier than this date. */
    fun prevOccurrence(weekday: Weekday): Date =
        checkedPrevOccurrence(weekday)
            ?: throw ArithmeticException("overflow calculating the previous occurrence of a weekday")

    /** Calculates the nth occurrence of a weekday strictly later than this date. */
    fun nthNextOccurrence(weekday: Weekday, n: Int): Date =
        checkedNthNextOccurrence(weekday, n)
            ?: throw ArithmeticException("overflow calculating the next occurrence of a weekday")

    /** Calculates the nth occurrence of a weekday strictly earlier than this date. */
    fun nthPrevOccurrence(weekday: Weekday, n: Int): Date =
        checkedNthPrevOccurrence(weekday, n)
            ?: throw ArithmeticException("overflow calculating the previous occurrence of a weekday")

    fun checkedNextOccurrence(weekday: Weekday): Date? {
        val dayDiff =
            when (weekday.ordinal - weekday().ordinal) {
                1, -6 -> 1
                2, -5 -> 2
                3, -4 -> 3
                4, -3 -> 4
                5, -2 -> 5
                6, -1 -> 6
                else -> 7
            }
        return checkedAdd(Duration.days(dayDiff.toLong()))
    }

    fun checkedPrevOccurrence(weekday: Weekday): Date? {
        val dayDiff =
            when (weekday.ordinal - weekday().ordinal) {
                1, -6 -> 6
                2, -5 -> 5
                3, -4 -> 4
                4, -3 -> 3
                5, -2 -> 2
                6, -1 -> 1
                else -> 7
            }
        return checkedSub(Duration.days(dayDiff.toLong()))
    }

    fun checkedNthNextOccurrence(weekday: Weekday, n: Int): Date? {
        if (n <= 0) return null
        return checkedNextOccurrence(weekday)?.checkedAdd(Duration.weeks((n - 1).toLong()))
    }

    fun checkedNthPrevOccurrence(weekday: Weekday, n: Int): Date? {
        if (n <= 0) return null
        return checkedPrevOccurrence(weekday)?.checkedSub(Duration.weeks((n - 1).toLong()))
    }

    /** Get the Julian day for the date. */
    fun toJulianDay(): Int {
        val (year, ord) = toOrdinalDate()
        val adjYear = year + 999_999
        val century = adjYear / 100
        val daysBeforeYear = ((1461L * adjYear) / 4L).toInt() - century + (century / 4)
        return daysBeforeYear + ord - 363_521_075
    }

    /** Computes self + duration, returning null if overflow occurred. */
    fun checkedAdd(duration: Duration): Date? {
        val wholeDays = duration.wholeDays()
        if (wholeDays < Int.MIN_VALUE.toLong() || wholeDays > Int.MAX_VALUE.toLong()) return null
        val newJulianDayLong = toJulianDay().toLong() + wholeDays
        if (newJulianDayLong < MIN.toJulianDay().toLong() || newJulianDayLong > MAX.toJulianDay().toLong()) return null
        return fromJulianDay(newJulianDayLong.toInt()).getOrNull()
    }

    /** Computes self - duration, returning null if overflow occurred. */
    fun checkedSub(duration: Duration): Date? {
        val wholeDays = duration.wholeDays()
        if (wholeDays < Int.MIN_VALUE.toLong() || wholeDays > Int.MAX_VALUE.toLong()) return null
        val newJulianDayLong = toJulianDay().toLong() - wholeDays
        if (newJulianDayLong < MIN.toJulianDay().toLong() || newJulianDayLong > MAX.toJulianDay().toLong()) return null
        return fromJulianDay(newJulianDayLong.toInt()).getOrNull()
    }

    /** Computes self + duration, saturating on overflow. */
    fun saturatingAdd(duration: Duration): Date =
        checkedAdd(duration) ?: if (duration.isNegative()) MIN else MAX

    /** Computes self - duration, saturating on overflow. */
    fun saturatingSub(duration: Duration): Date =
        checkedSub(duration) ?: if (duration.isNegative()) MAX else MIN

    /** Replace the year. Month and day remain unchanged. */
    fun replaceYear(year: Int): Result<Date> {
        if (year !in MIN_YEAR..MAX_YEAR) {
            return Result.failure(ComponentRange.conditional("year"))
        }

        val newIsLeapYear = isLeapYear(year)
        val ord = ordinal()

        if (ord <= 59) {
            return Result.success(fromParts(year, newIsLeapYear, ord))
        }

        val curIsLeapYear = isInLeapYear()
        return when {
            (!curIsLeapYear && !newIsLeapYear) || (curIsLeapYear && newIsLeapYear) -> {
                Result.success(Date((year shl 10) or (value and 0x3FF)))
            }

            curIsLeapYear && !newIsLeapYear && ord == 60 -> {
                Result.failure(ComponentRange.conditional("day"))
            }

            !curIsLeapYear && newIsLeapYear -> {
                Result.success(fromParts(year, true, ord + 1))
            }

            else -> {
                Result.success(fromParts(year, false, ord - 1))
            }
        }
    }

    /** Replace the month of the year. */
    fun replaceMonth(month: Month): Result<Date> {
        val (year, ord) = toOrdinalDate()
        var adjustedOrd = ord
        val isLeap = isInLeapYear()
        val janFebLen = 59 + if (isLeap) 1 else 0

        if (adjustedOrd > janFebLen) {
            adjustedOrd -= janFebLen
        }
        val currentMonth = (adjustedOrd * 268 + 8031) shr 13
        val daysInPrecedingMonths = (currentMonth * 3917 - 3866) shr 7
        val day = adjustedOrd - daysInPrecedingMonths

        if (day !in 1..daysInMonthLeap(month.number, isLeap)) {
            return Result.failure(ComponentRange.conditional("day"))
        }

        val table = if (isLeap) DAYS_CUMULATIVE_LEAP else DAYS_CUMULATIVE_COMMON
        return Result.success(fromParts(year, isLeap, table[month.number - 1] + day))
    }

    /** Replace the day of the month. */
    fun replaceDay(day: Int): Result<Date> {
        val isLeap = isInLeapYear()
        if (day !in 1..daysInMonthLeap(month().number, isLeap)) {
            return Result.failure(ComponentRange.conditional("day"))
        }

        return Result.success(fromParts(year(), isLeap, ordinal() - day() + day))
    }

    /** Replace the day of the year. */
    fun replaceOrdinal(ordinal: Int): Result<Date> {
        val isLeap = isInLeapYear()
        val maxOrdinal = if (isLeap) 366 else 365
        if (ordinal !in 1..maxOrdinal) {
            return Result.failure(ComponentRange.conditional("ordinal"))
        }

        return Result.success(fromParts(year(), isLeap, ordinal))
    }

    /** Create a [PrimitiveDateTime] using the existing date and midnight. */
    fun midnight(): PrimitiveDateTime = PrimitiveDateTime.new(this, Time.MIDNIGHT)

    /** Create a [PrimitiveDateTime] using the existing date and the provided [Time]. */
    fun withTime(time: Time): PrimitiveDateTime = PrimitiveDateTime.new(this, time)

    /** Attempt to create a [PrimitiveDateTime] using the existing date and the provided time. */
    fun withHms(hour: Int, minute: Int, second: Int): Result<PrimitiveDateTime> =
        Time.fromHms(hour, minute, second).map { PrimitiveDateTime.new(this, it) }

    /** Attempt to create a [PrimitiveDateTime] using the existing date and the provided time. */
    fun withHmsMilli(hour: Int, minute: Int, second: Int, millisecond: Int): Result<PrimitiveDateTime> =
        Time.fromHmsMilli(hour, minute, second, millisecond).map { PrimitiveDateTime.new(this, it) }

    /** Attempt to create a [PrimitiveDateTime] using the existing date and the provided time. */
    fun withHmsMicro(hour: Int, minute: Int, second: Int, microsecond: Int): Result<PrimitiveDateTime> =
        Time.fromHmsMicro(hour, minute, second, microsecond).map { PrimitiveDateTime.new(this, it) }

    /** Attempt to create a [PrimitiveDateTime] using the existing date and the provided time. */
    fun withHmsNano(hour: Int, minute: Int, second: Int, nanosecond: Int): Result<PrimitiveDateTime> =
        Time.fromHmsNano(hour, minute, second, nanosecond).map { PrimitiveDateTime.new(this, it) }

    operator fun plus(duration: Duration): Date =
        checkedAdd(duration) ?: throw ArithmeticException("overflow adding duration to date")

    operator fun minus(duration: Duration): Date =
        checkedSub(duration) ?: throw ArithmeticException("overflow subtracting duration from date")

    operator fun minus(other: Date): Duration =
        Duration.days((toJulianDay() - other.toJulianDay()).toLong())

    override fun compareTo(other: Date): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean = other is Date && value == other.value

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String {
        val (year, month, day) = toCalendarDate()
        val monthStr = if (month.number < 10) "0${month.number}" else "${month.number}"
        val dayStr = if (day < 10) "0$day" else "$day"
        val yearAbs = abs(year)
        val yearStr =
            when {
                year in 0..9999 -> year.toString().padStart(4, '0')
                year < 0 -> "-${yearAbs.toString().padStart(4, '0')}"
                else -> "+${year.toString().padStart(4, '0')}"
            }
        return "$yearStr-$monthStr-$dayStr"
    }

    companion object {
        private val DAYS_CUMULATIVE_COMMON =
            intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        private val DAYS_CUMULATIVE_LEAP =
            intArrayOf(0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335)

        internal fun fromParts(year: Int, isLeapYear: Boolean, ordinal: Int): Date =
            Date((year shl 10) or ((if (isLeapYear) 1 else 0) shl 9) or ordinal)

        internal fun fromOrdinalDateUnchecked(year: Int, ordinal: Int): Date =
            fromParts(year, isLeapYear(year), ordinal)

        /** The Unix epoch: 1970-01-01. */
        val UNIX_EPOCH: Date = fromOrdinalDateUnchecked(1970, 1)

        /** The minimum valid Date. */
        val MIN: Date = fromOrdinalDateUnchecked(MIN_YEAR, 1)

        /** The maximum valid Date. */
        val MAX: Date = fromOrdinalDateUnchecked(MAX_YEAR, daysInYear(MAX_YEAR))

        /** Attempt to create a Date from year, month, and day. */
        fun fromCalendarDate(year: Int, month: Month, day: Int): Result<Date> {
            if (year !in MIN_YEAR..MAX_YEAR) {
                return Result.failure(ComponentRange.conditional("year"))
            }

            val isLeap = isLeapYear(year)
            if (day !in 1..daysInMonthLeap(month.number, isLeap)) {
                return Result.failure(ComponentRange.conditional("day"))
            }

            val table = if (isLeap) DAYS_CUMULATIVE_LEAP else DAYS_CUMULATIVE_COMMON
            val ordinal = table[month.number - 1] + day
            return Result.success(fromParts(year, isLeap, ordinal))
        }

        /** Attempt to create a Date from year and ordinal day number. */
        fun fromOrdinalDate(year: Int, ordinal: Int): Result<Date> {
            if (year !in MIN_YEAR..MAX_YEAR) {
                return Result.failure(ComponentRange.conditional("year"))
            }

            val isLeap = isLeapYear(year)
            val maxOrdinal = if (isLeap) 366 else 365
            if (ordinal !in 1..maxOrdinal) {
                return Result.failure(ComponentRange.conditional("ordinal"))
            }

            return Result.success(fromParts(year, isLeap, ordinal))
        }

        /** Attempt to create a Date from ISO year, week, and weekday. */
        fun fromIsoWeekDate(year: Int, week: Int, weekday: Weekday): Result<Date> {
            if (year !in MIN_YEAR..MAX_YEAR) {
                return Result.failure(ComponentRange.conditional("year"))
            }

            if (week !in 1..weeksInYear(year)) {
                return Result.failure(ComponentRange.conditional("week"))
            }

            val adjYear = year - 1
            val raw = 365 * adjYear + floorDiv(adjYear, 4) - floorDiv(adjYear, 100) + floorDiv(adjYear, 400)
            val jan4 =
                when ((raw % 7)) {
                    -6, 1 -> 8
                    -5, 2 -> 9
                    -4, 3 -> 10
                    -3, 4 -> 4
                    -2, 5 -> 5
                    -1, 6 -> 6
                    else -> 7
                }
            val ordinal = week * 7 + weekday.numberFromMonday() - jan4

            if (ordinal <= 0) {
                val prevYear = year - 1
                return Result.success(fromOrdinalDateUnchecked(prevYear, ordinal + daysInYear(prevYear)))
            }

            val isLeap = isLeapYear(year)
            val daysInYr = if (isLeap) 366 else 365
            return if (ordinal > daysInYr) {
                Result.success(fromOrdinalDateUnchecked(year + 1, ordinal - daysInYr))
            } else {
                Result.success(fromParts(year, isLeap, ordinal))
            }
        }

        /** Create a Date from the Julian day. */
        fun fromJulianDay(julianDay: Int): Result<Date> {
            if (julianDay !in MIN.toJulianDay()..MAX.toJulianDay()) {
                return Result.failure(ComponentRange.conditional("julian_day"))
            }
            return Result.success(fromJulianDayUnchecked(julianDay))
        }

        internal fun fromJulianDayUnchecked(julianDay: Int): Date {
            val eras = 5_949u
            val dShift = (146_097u * eras) - 1_721_060u
            val yShift = 400u * eras

            val cenMul = ((4UL shl 47) / 146_097UL).toUInt()
            val julMul = (((4UL shl 40) / 1_461UL) + 1UL).toUInt()
            val cenCut = ((365UL shl 32) / 36_525UL).toUInt()

            val day = julianDay.toUInt() + dShift
            val cN = (day.toULong() * cenMul.toULong()) shr 15
            val cen = (cN shr 32).toUInt()
            val cpt = cN.toUInt()
            val ijy = cpt > cenCut || (cen % 4u == 0u)
            val jul = day - (cen / 4u) + cen
            val yN = (jul.toULong() * julMul.toULong()) shr 8
            val yrs = (yN shr 32).toUInt()
            val ypt = yN.toUInt()

            val year = (yrs - yShift).toInt()
            val ordinal = (((ypt.toULong() * 1_461UL) shr 34).toUInt() + (if (ijy) 1u else 0u)).toInt()
            val leap = (yrs % 4u == 0u) && ijy

            return fromParts(year, leap, ordinal)
        }
    }
}
