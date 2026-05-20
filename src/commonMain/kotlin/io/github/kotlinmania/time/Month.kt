// port-lint: source month.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.error.ComponentRange
import io.github.kotlinmania.time.error.InvalidVariant

/** Months of the year. */
enum class Month(val number: Int) {
    January(1),
    February(2),
    March(3),
    April(4),
    May(5),
    June(6),
    July(7),
    August(8),
    September(9),
    October(10),
    November(11),
    December(12),
    ;

    /** Get the number of days in the month of a given year. */
    fun length(year: Int): Int = daysInMonth(this, year)

    /** Get the previous month. */
    fun previous(): Month =
        when (this) {
            January -> December
            February -> January
            March -> February
            April -> March
            May -> April
            June -> May
            July -> June
            August -> July
            September -> August
            October -> September
            November -> October
            December -> November
        }

    /** Get the next month. */
    fun next(): Month =
        when (this) {
            January -> February
            February -> March
            March -> April
            April -> May
            May -> June
            June -> July
            July -> August
            August -> September
            September -> October
            October -> November
            November -> December
            December -> January
        }

    /** Get the n-th next month. */
    fun nthNext(n: Int): Month =
        when ((number - 1 + n.mod(12)).mod(12)) {
            0 -> January
            1 -> February
            2 -> March
            3 -> April
            4 -> May
            5 -> June
            6 -> July
            7 -> August
            8 -> September
            9 -> October
            10 -> November
            else -> December
        }

    /** Get the n-th previous month. */
    fun nthPrev(n: Int): Month =
        when (number - 1 - n.mod(12)) {
            1, -11 -> February
            2, -10 -> March
            3, -9 -> April
            4, -8 -> May
            5, -7 -> June
            6, -6 -> July
            7, -5 -> August
            8, -4 -> September
            9, -3 -> October
            10, -2 -> November
            11, -1 -> December
            else -> January
        }

    override fun toString(): String =
        when (this) {
            January -> "January"
            February -> "February"
            March -> "March"
            April -> "April"
            May -> "May"
            June -> "June"
            July -> "July"
            August -> "August"
            September -> "September"
            October -> "October"
            November -> "November"
            December -> "December"
        }

    fun fmt(): String = toString()

    companion object {
        /** Create a `Month` from its numerical value. */
        internal fun fromNumber(number: Int): Result<Month> =
            when (number) {
                1 -> Result.success(January)
                2 -> Result.success(February)
                3 -> Result.success(March)
                4 -> Result.success(April)
                5 -> Result.success(May)
                6 -> Result.success(June)
                7 -> Result.success(July)
                8 -> Result.success(August)
                9 -> Result.success(September)
                10 -> Result.success(October)
                11 -> Result.success(November)
                12 -> Result.success(December)
                else -> Result.failure(ComponentRange.unconditional("month"))
            }

        /** Parse a month name. */
        fun fromStr(value: String): Result<Month> = parse(value)

        /** Convert a month to its numerical value. */
        fun from(month: Month): Int = month.number

        /** Try to create a `Month` from its numerical value. */
        fun tryFrom(value: Int): Result<Month> = fromNumber(value)

        /** Parse a month name. */
        fun parse(value: String): Result<Month> =
            when (value) {
                "January" -> Result.success(January)
                "February" -> Result.success(February)
                "March" -> Result.success(March)
                "April" -> Result.success(April)
                "May" -> Result.success(May)
                "June" -> Result.success(June)
                "July" -> Result.success(July)
                "August" -> Result.success(August)
                "September" -> Result.success(September)
                "October" -> Result.success(October)
                "November" -> Result.success(November)
                "December" -> Result.success(December)
                else -> Result.failure(InvalidVariant())
            }
    }
}
