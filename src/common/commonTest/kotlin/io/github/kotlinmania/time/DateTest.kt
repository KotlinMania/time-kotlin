// port-lint: tests tests/integration/date.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.ext.days
import io.github.kotlinmania.time.ext.hours
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DateTest {
    @Test
    fun fromCalendarDate() {
        val d1 = Date.fromCalendarDate(2019, Month.January, 1).getOrThrow()
        assertEquals(2019, d1.year())
        assertEquals(Month.January, d1.month())
        assertEquals(1, d1.day())
        assertEquals(1, d1.ordinal())
        assertEquals("2019-01-01", d1.toString())

        val d2 = Date.fromCalendarDate(2019, Month.December, 31).getOrThrow()
        assertEquals(2019, d2.year())
        assertEquals(Month.December, d2.month())
        assertEquals(31, d2.day())
        assertEquals(365, d2.ordinal())
        assertEquals("2019-12-31", d2.toString())

        assertTrue(Date.fromCalendarDate(2019, Month.February, 29).isFailure)
        assertTrue(Date.fromCalendarDate(2020, Month.February, 29).isSuccess)
    }

    @Test
    fun fromOrdinalDate() {
        val d1 = Date.fromOrdinalDate(2019, 1).getOrThrow()
        assertEquals(1, d1.day())
        assertEquals(Month.January, d1.month())

        val d2 = Date.fromOrdinalDate(2019, 365).getOrThrow()
        assertEquals(31, d2.day())
        assertEquals(Month.December, d2.month())

        assertTrue(Date.fromOrdinalDate(2019, 366).isFailure)
        assertTrue(Date.fromOrdinalDate(2020, 366).isSuccess)
    }

    @Test
    fun fromIsoWeekDate() {
        assertTrue(Date.fromIsoWeekDate(2019, 1, Weekday.Monday).isSuccess)
        assertTrue(Date.fromIsoWeekDate(2019, 1, Weekday.Tuesday).isSuccess)
        assertTrue(Date.fromIsoWeekDate(2020, 53, Weekday.Friday).isSuccess)
        assertTrue(Date.fromIsoWeekDate(2019, 53, Weekday.Monday).isFailure)
    }

    @Test
    fun fromJulianDay() {
        assertEquals("2000-01-01", Date.fromJulianDay(2_451_545).getOrThrow().toString())
        assertEquals("2019-01-01", Date.fromJulianDay(2_458_485).getOrThrow().toString())
        assertEquals("2019-12-31", Date.fromJulianDay(2_458_849).getOrThrow().toString())
        assertEquals(2_451_545, Date.fromCalendarDate(2000, Month.January, 1).getOrThrow().toJulianDay())
    }

    @Test
    fun weekday() {
        assertEquals(Weekday.Tuesday, Date.fromCalendarDate(2019, Month.January, 1).getOrThrow().weekday())
        assertEquals(Weekday.Friday, Date.fromCalendarDate(2019, Month.February, 1).getOrThrow().weekday())
        assertEquals(Weekday.Friday, Date.fromCalendarDate(2019, Month.March, 1).getOrThrow().weekday())
        assertEquals(Weekday.Monday, Date.fromCalendarDate(2019, Month.April, 1).getOrThrow().weekday())
        assertEquals(Weekday.Wednesday, Date.fromCalendarDate(2019, Month.May, 1).getOrThrow().weekday())
        assertEquals(Weekday.Saturday, Date.fromCalendarDate(2019, Month.June, 1).getOrThrow().weekday())
        assertEquals(Weekday.Monday, Date.fromCalendarDate(2019, Month.July, 1).getOrThrow().weekday())
        assertEquals(Weekday.Thursday, Date.fromCalendarDate(2019, Month.August, 1).getOrThrow().weekday())
        assertEquals(Weekday.Sunday, Date.fromCalendarDate(2019, Month.September, 1).getOrThrow().weekday())
        assertEquals(Weekday.Tuesday, Date.fromCalendarDate(2019, Month.October, 1).getOrThrow().weekday())
        assertEquals(Weekday.Friday, Date.fromCalendarDate(2019, Month.November, 1).getOrThrow().weekday())
        assertEquals(Weekday.Sunday, Date.fromCalendarDate(2019, Month.December, 1).getOrThrow().weekday())
    }

    @Test
    fun nextAndPreviousDay() {
        val d = Date.fromCalendarDate(2019, Month.January, 1).getOrThrow()
        assertEquals(Date.fromCalendarDate(2019, Month.January, 2).getOrThrow(), d.nextDay())
        assertEquals(Date.fromCalendarDate(2018, Month.December, 31).getOrThrow(), d.previousDay())

        val leapEnd = Date.fromCalendarDate(2020, Month.February, 28).getOrThrow()
        assertEquals(Date.fromCalendarDate(2020, Month.February, 29).getOrThrow(), leapEnd.nextDay())
        assertEquals(Date.fromCalendarDate(2020, Month.March, 1).getOrThrow(), leapEnd.nextDay()?.nextDay())
    }

    @Test
    fun occurrences() {
        val d = Date.fromCalendarDate(2023, Month.June, 28).getOrThrow()
        assertEquals(Date.fromCalendarDate(2023, Month.July, 3).getOrThrow(), d.nextOccurrence(Weekday.Monday))
        assertEquals(Date.fromCalendarDate(2023, Month.June, 26).getOrThrow(), d.prevOccurrence(Weekday.Monday))
        assertEquals(Date.fromCalendarDate(2023, Month.July, 24).getOrThrow(), d.nthNextOccurrence(Weekday.Monday, 4))
        assertEquals(Date.fromCalendarDate(2023, Month.June, 5).getOrThrow(), d.nthPrevOccurrence(Weekday.Monday, 4))
    }

    @Test
    fun weekNumbers() {
        val d2019_01_01 = Date.fromCalendarDate(2019, Month.January, 1).getOrThrow()
        assertEquals(1, d2019_01_01.isoWeek())
        assertEquals(0, d2019_01_01.sundayBasedWeek())
        assertEquals(0, d2019_01_01.mondayBasedWeek())

        val d2020_12_31 = Date.fromCalendarDate(2020, Month.December, 31).getOrThrow()
        assertEquals(53, d2020_12_31.isoWeek())
        assertEquals(52, d2020_12_31.sundayBasedWeek())
        assertEquals(52, d2020_12_31.mondayBasedWeek())
    }

    @Test
    fun arithmetic() {
        val d = Date.fromCalendarDate(2020, Month.December, 31).getOrThrow()
        assertEquals(Date.fromCalendarDate(2021, Month.January, 2).getOrThrow(), d + 2.days())
        assertEquals(Date.fromCalendarDate(2020, Month.December, 29).getOrThrow(), d - 2.days())
        assertEquals(2.days(), (d + 2.days()) - d)

        // Sub-day truncated
        assertEquals(d, d + 23.hours())
    }

    @Test
    fun replacing() {
        val d = Date.fromCalendarDate(2022, Month.February, 18).getOrThrow()
        assertEquals(2019, d.replaceYear(2019).getOrThrow().year())
        assertEquals(Month.January, d.replaceMonth(Month.January).getOrThrow().month())
        assertEquals(1, d.replaceDay(1).getOrThrow().day())
        assertEquals(1, d.replaceOrdinal(1).getOrThrow().ordinal())

        assertTrue(d.replaceDay(0).isFailure)
        assertTrue(d.replaceDay(30).isFailure)
    }

    @Test
    fun dateTimeCombinations() {
        val d = Date.fromCalendarDate(1970, Month.January, 1).getOrThrow()
        val dtMidnight = d.midnight()
        assertEquals(d, dtMidnight.date())
        assertEquals(Time.MIDNIGHT, dtMidnight.time())

        val dtHms = d.withHms(12, 30, 45).getOrThrow()
        assertEquals(12, dtHms.hour())
        assertEquals(30, dtHms.minute())
        assertEquals(45, dtHms.second())
    }
}
