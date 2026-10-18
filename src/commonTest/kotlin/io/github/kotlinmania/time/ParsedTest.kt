// port-lint: tests tests/integration/parsed.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.error.ParseFromDescription
import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatdescription.Component
import io.github.kotlinmania.time.formatdescription.modifier.Day
import io.github.kotlinmania.time.formatdescription.modifier.Hour
import io.github.kotlinmania.time.formatdescription.modifier.Minute
import io.github.kotlinmania.time.formatdescription.modifier.OffsetHour
import io.github.kotlinmania.time.formatdescription.modifier.OffsetMinute
import io.github.kotlinmania.time.formatdescription.modifier.OffsetSecond
import io.github.kotlinmania.time.formatdescription.modifier.Ordinal
import io.github.kotlinmania.time.formatdescription.modifier.Period
import io.github.kotlinmania.time.formatdescription.modifier.Second
import io.github.kotlinmania.time.formatdescription.modifier.Subsecond
import io.github.kotlinmania.time.formatdescription.modifier.WeekNumber
import io.github.kotlinmania.time.formatdescription.modifier.WeekNumberRepr
import io.github.kotlinmania.time.formatdescription.modifier.Year
import io.github.kotlinmania.time.parsing.Parsed
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import io.github.kotlinmania.time.formatdescription.modifier.Month as MonthMod
import io.github.kotlinmania.time.formatdescription.modifier.Weekday as WeekdayMod

class ParsedTest {
    @Test
    fun gettersSetters() {
        run {
            val parsed = Parsed.new()
            parsed.setYear(5)
            assertEquals(5, parsed.year())
        }
        run {
            val parsed = Parsed.new()
            parsed.setYearLastTwo(5u)
            assertEquals(5u, parsed.yearLastTwo())
        }
        run {
            val parsed = Parsed.new()
            parsed.setIsoYear(5)
            assertEquals(5, parsed.isoYear())
        }
        run {
            val parsed = Parsed.new()
            parsed.setIsoYearLastTwo(5u)
            assertEquals(5u, parsed.isoYearLastTwo())
        }
        run {
            val parsed = Parsed.new()
            parsed.setMonth(Month.May)
            assertEquals(Month.May, parsed.month())
        }
        run {
            val parsed = Parsed.new()
            parsed.setSundayWeekNumber(5u)
            assertEquals(5u, parsed.sundayWeekNumber())
        }
        run {
            val parsed = Parsed.new()
            parsed.setMondayWeekNumber(5u)
            assertEquals(5u, parsed.mondayWeekNumber())
        }
        run {
            val parsed = Parsed.new()
            parsed.setIsoWeekNumber(5u)
            assertEquals(5u, parsed.isoWeekNumber())
        }
        run {
            val parsed = Parsed.new()
            parsed.setWeekday(Weekday.Monday)
            assertEquals(Weekday.Monday, parsed.weekday())
        }
        run {
            val parsed = Parsed.new()
            parsed.setOrdinal(5u)
            assertEquals(5u, parsed.ordinal())
        }
        run {
            val parsed = Parsed.new()
            parsed.setDay(5u)
            assertEquals(5u, parsed.day())
        }
        run {
            val parsed = Parsed.new()
            parsed.setHour24(5u)
            assertEquals(5u, parsed.hour24())
        }
        run {
            val parsed = Parsed.new()
            parsed.setHour12(5u)
            assertEquals(5u, parsed.hour12())
        }
        run {
            val parsed = Parsed.new()
            parsed.setHour12IsPm(true)
            assertEquals(true, parsed.hour12IsPm())
        }
        run {
            val parsed = Parsed.new()
            parsed.setMinute(5u)
            assertEquals(5u, parsed.minute())
        }
        run {
            val parsed = Parsed.new()
            parsed.setSecond(5u)
            assertEquals(5u, parsed.second())
        }
        run {
            val parsed = Parsed.new()
            parsed.setSubsecond(5u)
            assertEquals(5u, parsed.subsecond())
        }
        run {
            val parsed = Parsed.new()
            parsed.setOffsetHour(5)
            assertEquals(5, parsed.offsetHour())
        }
        run {
            val parsed = Parsed.new()
            parsed.setOffsetMinuteSigned(-5)
            assertEquals(-5, parsed.offsetMinuteSigned())
        }
        run {
            val parsed = Parsed.new()
            parsed.setOffsetSecondSigned(-5)
            assertEquals(-5, parsed.offsetSecondSigned())
        }
    }

    @Test
    fun builderMethods() {
        val parsed =
            Parsed
                .new()
                .withYear(5)
                ?.withYearLastTwo(5u)
                ?.withIsoYear(5)
                ?.withIsoYearLastTwo(5u)
                ?.withMonth(Month.May)
                ?.withSundayWeekNumber(5u)
                ?.withMondayWeekNumber(5u)
                ?.withIsoWeekNumber(5u)
                ?.withWeekday(Weekday.Monday)
                ?.withOrdinal(5u)
                ?.withDay(5u)
                ?.withHour24(5u)
                ?.withHour12(5u)
                ?.withHour12IsPm(true)
                ?.withMinute(5u)
                ?.withSecond(5u)
                ?.withSubsecond(5u)
                ?.withOffsetHour(5)
                ?.withOffsetMinuteSigned(-5)
                ?.withOffsetSecondSigned(-5)!!

        assertEquals(5, parsed.year())
        assertEquals(5u, parsed.yearLastTwo())
        assertEquals(5, parsed.isoYear())
        assertEquals(5u, parsed.isoYearLastTwo())
        assertEquals(Month.May, parsed.month())
        assertEquals(5u, parsed.sundayWeekNumber())
        assertEquals(5u, parsed.mondayWeekNumber())
        assertEquals(5u, parsed.isoWeekNumber())
        assertEquals(Weekday.Monday, parsed.weekday())
        assertEquals(5u, parsed.ordinal())
        assertEquals(5u, parsed.day())
        assertEquals(5u, parsed.hour24())
        assertEquals(5u, parsed.hour12())
        assertEquals(true, parsed.hour12IsPm())
        assertEquals(5u, parsed.minute())
        assertEquals(5u, parsed.second())
        assertEquals(5u, parsed.subsecond())
        assertEquals(5, parsed.offsetHour())
        assertEquals(-5, parsed.offsetMinuteSigned())
        assertEquals(-5, parsed.offsetSecondSigned())

        assertNull(Parsed.new().withOffsetMinuteSigned(100))
        assertNull(Parsed.new().withOffsetSecondSigned(100))
    }

    @Test
    fun singleItemParse() {
        assertTrue(Time.parse("a", BorrowedFormatItem.Literal("a".encodeToByteArray())).isFailure)
        assertTrue(Time.parse("b", BorrowedFormatItem.Literal("a".encodeToByteArray())).isFailure)
    }

    @Test
    fun componentErr() {
        fun assertInvalidComponent(
            componentName: String,
            component: Component,
            input: ByteArray = ByteArray(0),
        ) {
            val parsed = Parsed.new()
            assertEquals(
                Result.failure(ParseFromDescription.InvalidComponent(componentName)),
                parsed.parseComponent(input, component),
            )
        }

        assertInvalidComponent("day", Component.Day(Day.default()))
        assertInvalidComponent("month", Component.Month(MonthMod.default()))
        assertInvalidComponent("ordinal", Component.Ordinal(Ordinal.default()))
        assertInvalidComponent("weekday", Component.Weekday(WeekdayMod.default()))
        assertInvalidComponent("week number", Component.WeekNumber(WeekNumber.default()))
        assertInvalidComponent("year", Component.Year(Year.default()))
        assertInvalidComponent("minute", Component.Minute(Minute.default()))
        assertInvalidComponent("period", Component.Period(Period.default()))
        assertInvalidComponent("second", Component.Second(Second.default()))
        assertInvalidComponent("subsecond", Component.Subsecond(Subsecond.default()))
        assertInvalidComponent("offset hour", Component.OffsetHour(OffsetHour.default()))
        assertInvalidComponent("offset minute", Component.OffsetMinute(OffsetMinute.default()))
        assertInvalidComponent("offset second", Component.OffsetSecond(OffsetSecond.default()))

        assertInvalidComponent(
            "week number",
            Component.WeekNumber(WeekNumber.default().withRepr(WeekNumberRepr.Iso)),
            "00".encodeToByteArray(),
        )
        assertInvalidComponent(
            "hour",
            Component.Hour(Hour.default().withIs12HourClock(true)),
            "00".encodeToByteArray(),
        )
    }
}
