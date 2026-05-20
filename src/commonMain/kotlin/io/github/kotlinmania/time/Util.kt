// port-lint: source util.rs
package io.github.kotlinmania.time

/**
 * Utility functions, including updating time zone information.
 */

/** Whether to adjust the date, and in which direction. Useful when implementing arithmetic. */
internal enum class DateAdjustment {
    /** The previous day should be used. */
    Previous,

    /** The next day should be used. */
    Next,

    /** The date should be used as-is. */
    None,
}

/**
 * Returns if the provided year is a leap year in the proleptic Gregorian
 * calendar. Uses astronomical year numbering.
 */
fun isLeapYear(year: Int): Boolean {
    val divisorMask = if (year % 100 == 0) 15 else 3
    return year and divisorMask == 0
}

/** Get the number of calendar days in a given year. */
fun daysInYear(year: Int): Int = if (isLeapYear(year)) 366 else 365

/** Get the number of weeks in the ISO year. */
fun weeksInYear(year: Int): Int {
    val current = isoWeekPredicate(year)
    val previous = isoWeekPredicate(year - 1)
    return if (current == 4 || previous == 3) 53 else 52
}

/** Get the number of days in the month of a given year. */
fun daysInMonth(month: Month, year: Int): Int = daysInMonth(month.number, year)

/** Get the number of days in the month of a given year. */
@Deprecated("use daysInMonth or Month.length instead", ReplaceWith("daysInMonth(month, year)"))
fun daysInYearMonth(year: Int, month: Month): Int = daysInMonth(month, year)

internal fun daysInMonth(month: Int, year: Int): Int {
    require(month in 1..12) { "month must be in the range 1..=12" }
    return if (unlikely(month == 2)) {
        if (isLeapYear(year)) 29 else 28
    } else {
        30 or (month xor (month shr 3))
    }
}

internal fun daysInMonthLeap(month: Int, isLeapYear: Boolean): Int {
    require(month in 1..12) { "month must be in the range 1..=12" }
    return if (unlikely(month == 2)) {
        if (isLeapYear) 29 else 28
    } else {
        30 or (month xor (month shr 3))
    }
}

internal object RangeValidated {
    fun isLeapYear(year: Int): Boolean = io.github.kotlinmania.time.isLeapYear(year)
}

private fun isoWeekPredicate(year: Int): Int =
    floorMod(year + floorDiv(year, 4) - floorDiv(year, 100) + floorDiv(year, 400), 7)

private fun floorDiv(value: Int, divisor: Int): Int {
    val quotient = value / divisor
    val remainder = value % divisor
    return if (remainder != 0 && (value xor divisor) < 0) quotient - 1 else quotient
}

private fun floorMod(value: Int, divisor: Int): Int {
    val remainder = value % divisor
    return if (remainder < 0) remainder + divisor else remainder
}
