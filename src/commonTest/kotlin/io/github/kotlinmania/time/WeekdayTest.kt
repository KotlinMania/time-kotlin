// port-lint: ignore - upstream test source is tests/integration/weekday.rs.
package io.github.kotlinmania.time

import io.github.kotlinmania.time.error.InvalidVariant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WeekdayTest {
    @Test
    fun previous() {
        val cases =
            listOf(
                Weekday.Sunday to Weekday.Saturday,
                Weekday.Monday to Weekday.Sunday,
                Weekday.Tuesday to Weekday.Monday,
                Weekday.Wednesday to Weekday.Tuesday,
                Weekday.Thursday to Weekday.Wednesday,
                Weekday.Friday to Weekday.Thursday,
                Weekday.Saturday to Weekday.Friday,
            )

        for ((current, expected) in cases) {
            assertEquals(expected, current.previous())
        }
    }

    @Test
    fun next() {
        val cases =
            listOf(
                Weekday.Sunday to Weekday.Monday,
                Weekday.Monday to Weekday.Tuesday,
                Weekday.Tuesday to Weekday.Wednesday,
                Weekday.Wednesday to Weekday.Thursday,
                Weekday.Thursday to Weekday.Friday,
                Weekday.Friday to Weekday.Saturday,
                Weekday.Saturday to Weekday.Sunday,
            )

        for ((current, expected) in cases) {
            assertEquals(expected, current.next())
        }
    }

    @Test
    fun nthNext() {
        val cases =
            listOf(
                Triple(Weekday.Sunday, 0, Weekday.Sunday),
                Triple(Weekday.Sunday, 1, Weekday.Monday),
                Triple(Weekday.Sunday, 2, Weekday.Tuesday),
                Triple(Weekday.Sunday, 3, Weekday.Wednesday),
                Triple(Weekday.Sunday, 4, Weekday.Thursday),
                Triple(Weekday.Sunday, 5, Weekday.Friday),
                Triple(Weekday.Sunday, 6, Weekday.Saturday),
                Triple(Weekday.Monday, 0, Weekday.Monday),
                Triple(Weekday.Monday, 1, Weekday.Tuesday),
                Triple(Weekday.Monday, 2, Weekday.Wednesday),
                Triple(Weekday.Monday, 3, Weekday.Thursday),
                Triple(Weekday.Monday, 4, Weekday.Friday),
                Triple(Weekday.Monday, 5, Weekday.Saturday),
                Triple(Weekday.Monday, 6, Weekday.Sunday),
                Triple(Weekday.Sunday, 7, Weekday.Sunday),
                Triple(Weekday.Sunday, 255, Weekday.Wednesday),
                Triple(Weekday.Monday, 7, Weekday.Monday),
                Triple(Weekday.Monday, 255, Weekday.Thursday),
            )

        for ((current, count, expected) in cases) {
            assertEquals(expected, current.nthNext(count))
        }
    }

    @Test
    fun nthPrev() {
        val cases =
            listOf(
                Triple(Weekday.Sunday, 0, Weekday.Sunday),
                Triple(Weekday.Sunday, 1, Weekday.Saturday),
                Triple(Weekday.Sunday, 2, Weekday.Friday),
                Triple(Weekday.Sunday, 3, Weekday.Thursday),
                Triple(Weekday.Sunday, 4, Weekday.Wednesday),
                Triple(Weekday.Sunday, 5, Weekday.Tuesday),
                Triple(Weekday.Sunday, 6, Weekday.Monday),
                Triple(Weekday.Monday, 0, Weekday.Monday),
                Triple(Weekday.Monday, 1, Weekday.Sunday),
                Triple(Weekday.Monday, 2, Weekday.Saturday),
                Triple(Weekday.Monday, 3, Weekday.Friday),
                Triple(Weekday.Monday, 4, Weekday.Thursday),
                Triple(Weekday.Monday, 5, Weekday.Wednesday),
                Triple(Weekday.Monday, 6, Weekday.Tuesday),
                Triple(Weekday.Sunday, 7, Weekday.Sunday),
                Triple(Weekday.Sunday, 255, Weekday.Thursday),
                Triple(Weekday.Monday, 7, Weekday.Monday),
                Triple(Weekday.Monday, 255, Weekday.Friday),
            )

        for ((current, count, expected) in cases) {
            assertEquals(expected, current.nthPrev(count))
        }
    }

    @Test
    fun numberFromMonday() {
        val cases =
            listOf(
                Weekday.Monday to 1,
                Weekday.Tuesday to 2,
                Weekday.Wednesday to 3,
                Weekday.Thursday to 4,
                Weekday.Friday to 5,
                Weekday.Saturday to 6,
                Weekday.Sunday to 7,
            )

        for ((weekday, expected) in cases) {
            assertEquals(expected, weekday.numberFromMonday())
        }
    }

    @Test
    fun numberFromSunday() {
        val cases =
            listOf(
                Weekday.Sunday to 1,
                Weekday.Monday to 2,
                Weekday.Tuesday to 3,
                Weekday.Wednesday to 4,
                Weekday.Thursday to 5,
                Weekday.Friday to 6,
                Weekday.Saturday to 7,
            )

        for ((weekday, expected) in cases) {
            assertEquals(expected, weekday.numberFromSunday())
        }
    }

    @Test
    fun numberDaysFromMonday() {
        val cases =
            listOf(
                Weekday.Monday to 0,
                Weekday.Tuesday to 1,
                Weekday.Wednesday to 2,
                Weekday.Thursday to 3,
                Weekday.Friday to 4,
                Weekday.Saturday to 5,
                Weekday.Sunday to 6,
            )

        for ((weekday, expected) in cases) {
            assertEquals(expected, weekday.numberDaysFromMonday())
        }
    }

    @Test
    fun numberDaysFromSunday() {
        val cases =
            listOf(
                Weekday.Sunday to 0,
                Weekday.Monday to 1,
                Weekday.Tuesday to 2,
                Weekday.Wednesday to 3,
                Weekday.Thursday to 4,
                Weekday.Friday to 5,
                Weekday.Saturday to 6,
            )

        for ((weekday, expected) in cases) {
            assertEquals(expected, weekday.numberDaysFromSunday())
        }
    }

    @Test
    fun display() {
        val cases =
            listOf(
                Weekday.Monday to "Monday",
                Weekday.Tuesday to "Tuesday",
                Weekday.Wednesday to "Wednesday",
                Weekday.Thursday to "Thursday",
                Weekday.Friday to "Friday",
                Weekday.Saturday to "Saturday",
                Weekday.Sunday to "Sunday",
            )

        for ((weekday, expected) in cases) {
            assertEquals(expected, weekday.toString())
        }
    }

    @Test
    fun fromStr() {
        val successCases =
            listOf(
                "Monday" to Weekday.Monday,
                "Tuesday" to Weekday.Tuesday,
                "Wednesday" to Weekday.Wednesday,
                "Thursday" to Weekday.Thursday,
                "Friday" to Weekday.Friday,
                "Saturday" to Weekday.Saturday,
                "Sunday" to Weekday.Sunday,
            )

        for ((input, expected) in successCases) {
            assertEquals(expected, Weekday.parse(input).getOrThrow())
        }

        val failure = Weekday.parse("foo")
        assertTrue(failure.isFailure)
        assertIs<InvalidVariant>(failure.exceptionOrNull())
    }
}
