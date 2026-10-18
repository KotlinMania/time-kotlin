// port-lint: tests tests/integration/offset_date_time.rs
package io.github.kotlinmania.time

import kotlin.test.Test
import kotlin.test.assertEquals

class OffsetDateTimeTest {
    @Test
    fun newInOffsetAndAccessors() {
        val d = Date.fromCalendarDate(2023, Month.December, 18).getOrThrow()
        val t = Time.fromHmsMilli(10, 13, 44, 250).getOrThrow()
        val offset = UtcOffset.fromHms(-4, 0, 0).getOrThrow()
        val dt = OffsetDateTime.newInOffset(d, t, offset)

        assertEquals(2023, dt.year())
        assertEquals(Month.December, dt.month())
        assertEquals(18, dt.day())
        assertEquals(10, dt.hour())
        assertEquals(13, dt.minute())
        assertEquals(44, dt.second())
        assertEquals(250, dt.millisecond())
        assertEquals(-4, dt.offset().wholeHours())
    }

    @Test
    fun toOffset() {
        val d = Date.fromCalendarDate(2000, Month.January, 1).getOrThrow()
        val t = Time.fromHms(0, 0, 0).getOrThrow()
        val utc = OffsetDateTime.newInOffset(d, t, UtcOffset.UTC)

        val offsetMinus1 = UtcOffset.fromHms(-1, 0, 0).getOrThrow()
        val converted = utc.toOffset(offsetMinus1)
        assertEquals(1999, converted.year())
        assertEquals(Month.December, converted.month())
        assertEquals(31, converted.day())
        assertEquals(23, converted.hour())

        // Sydney (+11) to New York (-5)
        val sydneyOffset = UtcOffset.fromHms(11, 0, 0).getOrThrow()
        val nyOffset = UtcOffset.fromHms(-5, 0, 0).getOrThrow()
        val laOffset = UtcOffset.fromHms(-8, 0, 0).getOrThrow()

        val sydney = OffsetDateTime.newInOffset(d, t, sydneyOffset)
        val newYork = sydney.toOffset(nyOffset)
        val losAngeles = sydney.toOffset(laOffset)

        assertEquals(0, sydney.hour())
        assertEquals(1, sydney.day())
        assertEquals(8, newYork.hour())
        assertEquals(31, newYork.day())
        assertEquals(5, losAngeles.hour())
        assertEquals(31, losAngeles.day())
    }

    @Test
    fun unixTimestamp() {
        val epoch = OffsetDateTime.UNIX_EPOCH
        assertEquals(0L, epoch.unixTimestamp())
        assertEquals(0L, epoch.unixTimestampNanos())

        val dt = OffsetDateTime.fromUnixTimestamp(0).getOrThrow()
        assertEquals(epoch, dt)

        val oneDay = OffsetDateTime.fromUnixTimestamp(86_400).getOrThrow()
        assertEquals(Date.fromCalendarDate(1970, Month.January, 2).getOrThrow(), oneDay.date())
        assertEquals(86_400L, oneDay.unixTimestamp())
    }

    @Test
    fun equalityAndOrdering() {
        val d1 = Date.fromCalendarDate(2000, Month.January, 1).getOrThrow()
        val t1 = Time.fromHms(0, 0, 0).getOrThrow()
        val utc = OffsetDateTime.newInOffset(d1, t1, UtcOffset.UTC)

        val d2 = Date.fromCalendarDate(1999, Month.December, 31).getOrThrow()
        val t2 = Time.fromHms(19, 0, 0).getOrThrow()
        val minus5 = UtcOffset.fromHms(-5, 0, 0).getOrThrow()
        val ny = OffsetDateTime.newInOffset(d2, t2, minus5)

        // Same moment in time
        assertEquals(utc, ny)
        assertEquals(0, utc.compareTo(ny))
    }
}
