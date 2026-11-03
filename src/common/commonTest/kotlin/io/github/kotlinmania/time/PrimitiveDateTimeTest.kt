// port-lint: tests tests/integration/primitive_date_time.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.ext.hours
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimitiveDateTimeTest {
    @Test
    fun newAndAccessors() {
        val d = Date.fromCalendarDate(2019, Month.January, 1).getOrThrow()
        val t = Time.fromHms(1, 2, 3).getOrThrow()
        val dt = PrimitiveDateTime.new(d, t)

        assertEquals(d, dt.date())
        assertEquals(t, dt.time())
        assertEquals(2019, dt.year())
        assertEquals(Month.January, dt.month())
        assertEquals(1, dt.day())
        assertEquals(1, dt.ordinal())
        assertEquals(1, dt.hour())
        assertEquals(2, dt.minute())
        assertEquals(3, dt.second())
        assertEquals(0, dt.nanosecond())
        assertEquals(Weekday.Tuesday, dt.weekday())
        assertEquals(1, dt.isoWeek())
    }

    @Test
    fun arithmetic() {
        val d = Date.fromCalendarDate(2020, Month.December, 31).getOrThrow()
        val t = Time.fromHms(23, 0, 0).getOrThrow()
        val dt = PrimitiveDateTime.new(d, t)

        val nextHour = dt + 1.hours()
        assertEquals(Date.fromCalendarDate(2021, Month.January, 1).getOrThrow(), nextHour.date())
        assertEquals(Time.MIDNIGHT, nextHour.time())

        val back = nextHour - 1.hours()
        assertEquals(dt, back)

        assertEquals(2.hours(), (dt + 2.hours()) - dt)
    }

    @Test
    fun replacing() {
        val d = Date.fromCalendarDate(2020, Month.January, 1).getOrThrow()
        val t = Time.fromHms(0, 0, 0).getOrThrow()
        val dt = PrimitiveDateTime.new(d, t)

        assertEquals(2021, dt.replaceYear(2021).getOrThrow().year())
        assertEquals(Month.February, dt.replaceMonth(Month.February).getOrThrow().month())
        assertEquals(15, dt.replaceDay(15).getOrThrow().day())
        assertEquals(12, dt.replaceHour(12).getOrThrow().hour())
        assertEquals(30, dt.replaceMinute(30).getOrThrow().minute())
        assertEquals(45, dt.replaceSecond(45).getOrThrow().second())
    }

    @Test
    fun ordering() {
        val d1 = Date.fromCalendarDate(2020, Month.January, 1).getOrThrow()
        val d2 = Date.fromCalendarDate(2020, Month.January, 2).getOrThrow()
        val t1 = Time.fromHms(0, 0, 0).getOrThrow()
        val t2 = Time.fromHms(12, 0, 0).getOrThrow()

        val dt1 = PrimitiveDateTime.new(d1, t1)
        val dt2 = PrimitiveDateTime.new(d1, t2)
        val dt3 = PrimitiveDateTime.new(d2, t1)

        assertTrue(dt1 < dt2)
        assertTrue(dt2 < dt3)
        assertEquals(dt1, PrimitiveDateTime.new(d1, t1))
    }
}
