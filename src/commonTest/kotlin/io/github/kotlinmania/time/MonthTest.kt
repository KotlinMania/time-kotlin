// port-lint: ignore - upstream test source is tests/integration/month.rs.
package io.github.kotlinmania.time

import io.github.kotlinmania.time.error.ComponentRange
import io.github.kotlinmania.time.error.InvalidVariant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MonthTest {
    @Test
    fun previous() {
        val cases =
            listOf(
                Month.January to Month.December,
                Month.February to Month.January,
                Month.March to Month.February,
                Month.April to Month.March,
                Month.May to Month.April,
                Month.June to Month.May,
                Month.July to Month.June,
                Month.August to Month.July,
                Month.September to Month.August,
                Month.October to Month.September,
                Month.November to Month.October,
                Month.December to Month.November,
            )

        for ((month, expected) in cases) {
            assertEquals(expected, month.previous())
        }
    }

    @Test
    fun next() {
        val cases =
            listOf(
                Month.January to Month.February,
                Month.February to Month.March,
                Month.March to Month.April,
                Month.April to Month.May,
                Month.May to Month.June,
                Month.June to Month.July,
                Month.July to Month.August,
                Month.August to Month.September,
                Month.September to Month.October,
                Month.October to Month.November,
                Month.November to Month.December,
                Month.December to Month.January,
            )

        for ((month, expected) in cases) {
            assertEquals(expected, month.next())
        }
    }

    @Test
    fun nthNext() {
        val cases =
            listOf(
                Triple(Month.January, 0, Month.January),
                Triple(Month.January, 1, Month.February),
                Triple(Month.January, 2, Month.March),
                Triple(Month.January, 3, Month.April),
                Triple(Month.January, 4, Month.May),
                Triple(Month.January, 5, Month.June),
                Triple(Month.January, 6, Month.July),
                Triple(Month.January, 7, Month.August),
                Triple(Month.January, 8, Month.September),
                Triple(Month.January, 9, Month.October),
                Triple(Month.January, 10, Month.November),
                Triple(Month.January, 11, Month.December),
                Triple(Month.December, 0, Month.December),
                Triple(Month.December, 1, Month.January),
                Triple(Month.December, 2, Month.February),
                Triple(Month.December, 3, Month.March),
                Triple(Month.December, 4, Month.April),
                Triple(Month.December, 5, Month.May),
                Triple(Month.December, 6, Month.June),
                Triple(Month.December, 7, Month.July),
                Triple(Month.December, 8, Month.August),
                Triple(Month.December, 9, Month.September),
                Triple(Month.December, 10, Month.October),
                Triple(Month.December, 11, Month.November),
                Triple(Month.January, 12, Month.January),
                Triple(Month.January, 255, Month.April),
                Triple(Month.December, 12, Month.December),
                Triple(Month.December, 255, Month.March),
            )

        for ((month, count, expected) in cases) {
            assertEquals(expected, month.nthNext(count))
        }
    }

    @Test
    fun nthPrev() {
        val cases =
            listOf(
                Triple(Month.January, 0, Month.January),
                Triple(Month.January, 1, Month.December),
                Triple(Month.January, 2, Month.November),
                Triple(Month.January, 3, Month.October),
                Triple(Month.January, 4, Month.September),
                Triple(Month.January, 5, Month.August),
                Triple(Month.January, 6, Month.July),
                Triple(Month.January, 7, Month.June),
                Triple(Month.January, 8, Month.May),
                Triple(Month.January, 9, Month.April),
                Triple(Month.January, 10, Month.March),
                Triple(Month.January, 11, Month.February),
                Triple(Month.December, 0, Month.December),
                Triple(Month.December, 1, Month.November),
                Triple(Month.December, 2, Month.October),
                Triple(Month.December, 3, Month.September),
                Triple(Month.December, 4, Month.August),
                Triple(Month.December, 5, Month.July),
                Triple(Month.December, 6, Month.June),
                Triple(Month.December, 7, Month.May),
                Triple(Month.December, 8, Month.April),
                Triple(Month.December, 9, Month.March),
                Triple(Month.December, 10, Month.February),
                Triple(Month.December, 11, Month.January),
                Triple(Month.January, 12, Month.January),
                Triple(Month.January, 255, Month.October),
                Triple(Month.December, 12, Month.December),
                Triple(Month.December, 255, Month.September),
            )

        for ((month, count, expected) in cases) {
            assertEquals(expected, month.nthPrev(count))
        }
    }

    @Test
    fun display() {
        val cases =
            listOf(
                Month.January to "January",
                Month.February to "February",
                Month.March to "March",
                Month.April to "April",
                Month.May to "May",
                Month.June to "June",
                Month.July to "July",
                Month.August to "August",
                Month.September to "September",
                Month.October to "October",
                Month.November to "November",
                Month.December to "December",
            )

        for ((month, expected) in cases) {
            assertEquals(expected, month.toString())
        }
    }

    @Test
    fun fromStr() {
        val successCases =
            listOf(
                "January" to Month.January,
                "February" to Month.February,
                "March" to Month.March,
                "April" to Month.April,
                "May" to Month.May,
                "June" to Month.June,
                "July" to Month.July,
                "August" to Month.August,
                "September" to Month.September,
                "October" to Month.October,
                "November" to Month.November,
                "December" to Month.December,
            )

        for ((input, expected) in successCases) {
            assertEquals(expected, Month.parse(input).getOrThrow())
        }

        val failure = Month.parse("foo")
        assertTrue(failure.isFailure)
        assertIs<InvalidVariant>(failure.exceptionOrNull())
    }

    @Test
    fun toInt() {
        val cases =
            listOf(
                Month.January to 1,
                Month.February to 2,
                Month.March to 3,
                Month.April to 4,
                Month.May to 5,
                Month.June to 6,
                Month.July to 7,
                Month.August to 8,
                Month.September to 9,
                Month.October to 10,
                Month.November to 11,
                Month.December to 12,
            )

        for ((month, expected) in cases) {
            assertEquals(expected, Month.from(month))
        }
    }

    @Test
    fun tryFromIntSuccess() {
        val cases =
            listOf(
                1 to Month.January,
                2 to Month.February,
                3 to Month.March,
                4 to Month.April,
                5 to Month.May,
                6 to Month.June,
                7 to Month.July,
                8 to Month.August,
                9 to Month.September,
                10 to Month.October,
                11 to Month.November,
                12 to Month.December,
            )

        for ((input, expected) in cases) {
            assertEquals(expected, Month.tryFrom(input).getOrThrow())
        }
    }

    @Test
    fun tryFromIntError() {
        for (input in listOf(0, 13)) {
            val failure = Month.tryFrom(input)
            assertTrue(failure.isFailure)
            assertEquals("month", assertIs<ComponentRange>(failure.exceptionOrNull()).name())
        }
    }
}
