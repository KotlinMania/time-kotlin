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
 *
 * Example:
 * isLeapYear(2020) == true
 * isLeapYear(2021) == false
 * isLeapYear(2000) == true
 * isLeapYear(1900) == false
 */
fun isLeapYear(year: Int): Boolean {
    val divisorMask = if (year % 100 == 0) 15 else 3
    return year and divisorMask == 0
}

/**
 * Get the number of calendar days in a given year.
 *
 * Example:
 * daysInYear(2020) == 366
 * daysInYear(2021) == 365
 */
fun daysInYear(year: Int): Int = if (isLeapYear(year)) 366 else 365

/**
 * Get the number of weeks in the ISO year.
 *
 * Example:
 * weeksInYear(2020) == 53
 * weeksInYear(2021) == 52
 */
fun weeksInYear(year: Int): Int {
    val current = isoWeekPredicate(year)
    val previous = isoWeekPredicate(year - 1)
    return if (current == 4 || previous == 3) 53 else 52
}

/**
 * Get the number of days in the month of a given year.
 *
 * Example:
 * daysInMonth(Month.February, 2020) == 29
 */
fun daysInMonth(month: Month, year: Int): Int = daysInMonth(month.number, year)

/**
 * Get the number of days in the month of a given year.
 *
 * Example:
 * daysInYearMonth(2020, Month.February) == 29
 */
@Deprecated("use daysInMonth or Month.length instead", ReplaceWith("daysInMonth(month, year)"))
fun daysInYearMonth(year: Int, month: Month): Int = daysInMonth(month, year)

/**
 * Update time zone information from the system.
 */
fun refreshTzUnchecked() {
    // Platform time zone refresh if applicable
}

/**
 * Attempt to update time zone information from the system.
 *
 * Returns null if the call is not known to be sound.
 */
fun refreshTz(): Unit? = Unit

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
    fun isLeapYear(year: Int): Boolean =
        io.github.kotlinmania.time
            .isLeapYear(year)
}

object LocalOffset {
    enum class Soundness {
        Sound,
        Unsound,
    }

    fun setSoundness(soundness: Soundness) {
        when (soundness) {
            Soundness.Sound,
            Soundness.Unsound,
            -> Unit
        }
    }

    fun getSoundness(): Soundness = Soundness.Sound
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
