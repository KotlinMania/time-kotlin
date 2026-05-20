// port-lint: source weekday.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.error.InvalidVariant

/**
 * Days of the week.
 *
 * As order is dependent on context, Sunday could be either two days after or
 * five days before Friday, this type does not implement natural ordering.
 */
enum class Weekday {
    Monday,
    Tuesday,
    Wednesday,
    Thursday,
    Friday,
    Saturday,
    Sunday,
    ;

    /** Get the previous weekday. */
    fun previous(): Weekday =
        when (this) {
            Monday -> Sunday
            Tuesday -> Monday
            Wednesday -> Tuesday
            Thursday -> Wednesday
            Friday -> Thursday
            Saturday -> Friday
            Sunday -> Saturday
        }

    /** Get the next weekday. */
    fun next(): Weekday =
        when (this) {
            Monday -> Tuesday
            Tuesday -> Wednesday
            Wednesday -> Thursday
            Thursday -> Friday
            Friday -> Saturday
            Saturday -> Sunday
            Sunday -> Monday
        }

    /** Get the n-th next day. */
    fun nthNext(n: Int): Weekday =
        when ((numberDaysFromMonday() + n.mod(7)).mod(7)) {
            0 -> Monday
            1 -> Tuesday
            2 -> Wednesday
            3 -> Thursday
            4 -> Friday
            5 -> Saturday
            else -> Sunday
        }

    /** Get the n-th previous day. */
    fun nthPrev(n: Int): Weekday =
        when (numberDaysFromMonday() - n.mod(7)) {
            1, -6 -> Tuesday
            2, -5 -> Wednesday
            3, -4 -> Thursday
            4, -3 -> Friday
            5, -2 -> Saturday
            6, -1 -> Sunday
            else -> Monday
        }

    /** Get the one-indexed number of days from Monday. */
    fun numberFromMonday(): Int = numberDaysFromMonday() + 1

    /** Get the one-indexed number of days from Sunday. */
    fun numberFromSunday(): Int = numberDaysFromSunday() + 1

    /** Get the zero-indexed number of days from Monday. */
    fun numberDaysFromMonday(): Int = ordinal

    /** Get the zero-indexed number of days from Sunday. */
    fun numberDaysFromSunday(): Int =
        when (this) {
            Monday -> 1
            Tuesday -> 2
            Wednesday -> 3
            Thursday -> 4
            Friday -> 5
            Saturday -> 6
            Sunday -> 0
        }

    override fun toString(): String =
        when (this) {
            Monday -> "Monday"
            Tuesday -> "Tuesday"
            Wednesday -> "Wednesday"
            Thursday -> "Thursday"
            Friday -> "Friday"
            Saturday -> "Saturday"
            Sunday -> "Sunday"
        }

    fun fmt(): String = toString()

    companion object {
        /** Parse a weekday name. */
        fun fromStr(value: String): Result<Weekday> = parse(value)

        /** Parse a weekday name. */
        fun parse(value: String): Result<Weekday> =
            when (value) {
                "Monday" -> Result.success(Monday)
                "Tuesday" -> Result.success(Tuesday)
                "Wednesday" -> Result.success(Wednesday)
                "Thursday" -> Result.success(Thursday)
                "Friday" -> Result.success(Friday)
                "Saturday" -> Result.success(Saturday)
                "Sunday" -> Result.success(Sunday)
                else -> Result.failure(InvalidVariant())
            }
    }
}
