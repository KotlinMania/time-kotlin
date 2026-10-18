// port-lint: tests tests/integration/utc_date_time.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.ext.days
import io.github.kotlinmania.time.ext.hours
import io.github.kotlinmania.time.ext.nanoseconds
import io.github.kotlinmania.time.ext.seconds
import io.github.kotlinmania.time.ext.weeks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days as stdDays
import kotlin.time.Duration.Companion.seconds as stdSeconds

class UtcDateTimeTest {
    private fun utcDt(
        year: Int,
        month: Month,
        day: Int,
        hour: Int = 0,
        minute: Int = 0,
        second: Int = 0,
        nano: Int = 0,
    ): UtcDateTime =
        UtcDateTime.new(
            Date.fromCalendarDate(year, month, day).getOrThrow(),
            Time.fromHmsNano(hour, minute, second, nano).getOrThrow(),
        )

    private fun utcOrdDt(
        year: Int,
        ordinal: Int,
        hour: Int = 0,
        minute: Int = 0,
        second: Int = 0,
        nano: Int = 0,
    ): UtcDateTime =
        UtcDateTime.new(
            Date.fromOrdinalDate(year, ordinal).getOrThrow(),
            Time.fromHmsNano(hour, minute, second, nano).getOrThrow(),
        )

    @Test
    fun new() {
        val dt = utcDt(2023, Month.December, 18, 10, 13, 44, 250_000_000)
        assertEquals(2023, dt.year())
        assertEquals(250, dt.millisecond())
    }

    @Test
    fun now() {
        assertTrue(UtcDateTime.now().year() >= 2019)
    }

    @Test
    fun toOffset() {
        val minus1 = UtcOffset.fromHms(-1, 0, 0).getOrThrow()
        assertEquals(
            OffsetDateTime.newInOffset(
                Date.fromCalendarDate(1999, Month.December, 31).getOrThrow(),
                Time.fromHms(23, 0, 0).getOrThrow(),
                minus1,
            ),
            utcDt(2000, Month.January, 1, 0, 0).toOffset(minus1),
        )
        assertEquals(
            OffsetDateTime.newInOffset(
                Date.fromOrdinalDate(0, 1).getOrThrow(),
                Time.MIDNIGHT,
                UtcOffset.UTC,
            ),
            utcOrdDt(0, 1, 0, 0).toOffset(UtcOffset.UTC),
        )
    }

    @Test
    fun toOffsetPanic() {
        val plus1 = UtcOffset.fromHms(1, 0, 0).getOrThrow()
        val minus1 = UtcOffset.fromHms(-1, 0, 0).getOrThrow()
        assertFailsWith<ArithmeticException> { UtcDateTime.MAX.toOffset(plus1) }
        assertFailsWith<ArithmeticException> { UtcDateTime.MIN.toOffset(minus1) }
    }

    @Test
    fun checkedToOffset() {
        val minus1 = UtcOffset.fromHms(-1, 0, 0).getOrThrow()
        val plus1 = UtcOffset.fromHms(1, 0, 0).getOrThrow()
        assertEquals(
            1999,
            utcDt(2000, Month.January, 1, 0, 0).checkedToOffset(minus1)?.year(),
        )
        assertNull(UtcDateTime.MAX.checkedToOffset(plus1))
        assertNull(UtcDateTime.MIN.checkedToOffset(minus1))
    }

    @Test
    fun fromUnixTimestamp() {
        assertEquals(
            UtcDateTime.UNIX_EPOCH,
            UtcDateTime.fromUnixTimestamp(0).getOrThrow(),
        )
        assertEquals(
            utcDt(2019, Month.January, 1, 0, 0),
            UtcDateTime.fromUnixTimestamp(1_546_300_800).getOrThrow(),
        )
        assertTrue(UtcDateTime.fromUnixTimestamp(Long.MAX_VALUE).isFailure)
        assertTrue(UtcDateTime.fromUnixTimestamp(Long.MIN_VALUE).isFailure)
    }

    @Test
    fun fromUnixTimestampNanos() {
        assertEquals(
            UtcDateTime.UNIX_EPOCH,
            UtcDateTime.fromUnixTimestampNanos(0).getOrThrow(),
        )
        assertEquals(
            utcDt(2019, Month.January, 1, 0, 0),
            UtcDateTime.fromUnixTimestampNanos(1_546_300_800_000_000_000L).getOrThrow(),
        )
    }

    @Test
    fun unixTimestamp() {
        assertEquals(0L, UtcDateTime.UNIX_EPOCH.unixTimestamp())
    }

    @Test
    fun unixTimestampNanos() {
        assertEquals(0L, UtcDateTime.UNIX_EPOCH.unixTimestampNanos())
    }

    @Test
    fun date() {
        assertEquals(
            Date.fromCalendarDate(2019, Month.January, 1).getOrThrow(),
            utcDt(2019, Month.January, 1, 0, 0).date(),
        )
    }

    @Test
    fun time() {
        assertEquals(
            Time.MIDNIGHT,
            utcDt(2019, Month.January, 1, 0, 0).time(),
        )
    }

    @Test
    fun year() {
        assertEquals(2019, utcDt(2019, Month.January, 1, 0, 0).year())
    }

    @Test
    fun month() {
        assertEquals(Month.January, utcDt(2019, Month.January, 1, 0, 0).month())
    }

    @Test
    fun day() {
        assertEquals(1, utcDt(2019, Month.January, 1, 0, 0).day())
    }

    @Test
    fun ordinal() {
        assertEquals(1, utcDt(2019, Month.January, 1, 0, 0).ordinal())
    }

    @Test
    fun isoWeek() {
        assertEquals(1, utcDt(2019, Month.January, 1, 0, 0).isoWeek())
        assertEquals(1, utcDt(2020, Month.January, 1, 0, 0).isoWeek())
        assertEquals(53, utcDt(2020, Month.December, 31, 0, 0).isoWeek())
        assertEquals(53, utcDt(2021, Month.January, 1, 0, 0).isoWeek())
    }

    @Test
    fun sundayBasedWeek() {
        assertEquals(0, utcDt(2019, Month.January, 1, 0, 0).sundayBasedWeek())
        assertEquals(0, utcDt(2020, Month.January, 1, 0, 0).sundayBasedWeek())
        assertEquals(52, utcDt(2020, Month.December, 31, 0, 0).sundayBasedWeek())
        assertEquals(0, utcDt(2021, Month.January, 1, 0, 0).sundayBasedWeek())
    }

    @Test
    fun mondayBasedWeek() {
        assertEquals(0, utcDt(2019, Month.January, 1, 0, 0).mondayBasedWeek())
        assertEquals(0, utcDt(2020, Month.January, 1, 0, 0).mondayBasedWeek())
        assertEquals(52, utcDt(2020, Month.December, 31, 0, 0).mondayBasedWeek())
        assertEquals(0, utcDt(2021, Month.January, 1, 0, 0).mondayBasedWeek())
    }

    @Test
    fun toCalendarDate() {
        assertEquals(
            Triple(2019, Month.January, 2),
            utcDt(2019, Month.January, 2, 0, 0).toCalendarDate(),
        )
    }

    @Test
    fun toOrdinalDate() {
        assertEquals(
            Pair(2019, 1),
            utcDt(2019, Month.January, 1, 0, 0).toOrdinalDate(),
        )
    }

    @Test
    fun toIsoWeekDate() {
        assertEquals(
            Triple(2019, 1, Weekday.Tuesday),
            utcDt(2019, Month.January, 1, 0, 0).toIsoWeekDate(),
        )
        assertEquals(
            Triple(2019, 40, Weekday.Friday),
            utcDt(2019, Month.October, 4, 0, 0).toIsoWeekDate(),
        )
        assertEquals(
            Triple(2020, 1, Weekday.Wednesday),
            utcDt(2020, Month.January, 1, 0, 0).toIsoWeekDate(),
        )
        assertEquals(
            Triple(2020, 53, Weekday.Thursday),
            utcDt(2020, Month.December, 31, 0, 0).toIsoWeekDate(),
        )
        assertEquals(
            Triple(2020, 53, Weekday.Friday),
            utcDt(2021, Month.January, 1, 0, 0).toIsoWeekDate(),
        )
    }

    @Test
    fun weekday() {
        assertEquals(Weekday.Tuesday, utcDt(2019, Month.January, 1, 0, 0).weekday())
        assertEquals(Weekday.Friday, utcDt(2019, Month.February, 1, 0, 0).weekday())
        assertEquals(Weekday.Friday, utcDt(2019, Month.March, 1, 0, 0).weekday())
    }

    @Test
    fun toJulianDay() {
        assertEquals(0, utcDt(-4713, Month.November, 24, 0, 0).toJulianDay())
        assertEquals(2_451_545, utcDt(2000, Month.January, 1, 0, 0).toJulianDay())
        assertEquals(2_458_485, utcDt(2019, Month.January, 1, 0, 0).toJulianDay())
        assertEquals(2_458_849, utcDt(2019, Month.December, 31, 0, 0).toJulianDay())
    }

    @Test
    fun asHms() {
        assertEquals(
            Triple(1, 2, 3),
            utcDt(2020, Month.January, 1, 1, 2, 3).asHms(),
        )
    }

    @Test
    fun asHmsMilli() {
        assertEquals(
            listOf(1, 2, 3, 4),
            utcDt(2020, Month.January, 1, 1, 2, 3, 4_000_000).asHmsMilli(),
        )
    }

    @Test
    fun asHmsMicro() {
        assertEquals(
            listOf(1, 2, 3, 4_005),
            utcDt(2020, Month.January, 1, 1, 2, 3, 4_005_000).asHmsMicro(),
        )
    }

    @Test
    fun asHmsNano() {
        assertEquals(
            listOf(1, 2, 3, 4_005_006),
            utcDt(2020, Month.January, 1, 1, 2, 3, 4_005_006).asHmsNano(),
        )
    }

    @Test
    fun hourMinuteSecondSubseconds() {
        val midnight = utcDt(2019, Month.January, 1, 0, 0)
        assertEquals(0, midnight.hour())
        assertEquals(0, midnight.minute())
        assertEquals(0, midnight.second())
        assertEquals(0, midnight.millisecond())
        assertEquals(0, midnight.microsecond())
        assertEquals(0, midnight.nanosecond())

        assertEquals(999, utcDt(2019, Month.January, 1, 23, 59, 59, 999_000_000).millisecond())
        assertEquals(999_999, utcDt(2019, Month.January, 1, 23, 59, 59, 999_999_000).microsecond())
        assertEquals(999_999_999, utcDt(2019, Month.January, 1, 23, 59, 59, 999_999_999).nanosecond())
    }

    @Test
    fun replaceMethods() {
        assertEquals(
            utcDt(2020, Month.January, 1, 12, 0),
            utcDt(2020, Month.January, 1, 5, 0).replaceTime(Time.fromHms(12, 0, 0).getOrThrow()),
        )
        assertEquals(
            utcDt(2020, Month.January, 30, 12, 0),
            utcDt(2020, Month.January, 1, 12, 0).replaceDate(
                Date.fromCalendarDate(2020, Month.January, 30).getOrThrow(),
            ),
        )
        assertEquals(
            utcDt(2019, Month.February, 18, 12, 0),
            utcDt(2022, Month.February, 18, 12, 0).replaceYear(2019).getOrThrow(),
        )
        assertTrue(utcDt(2022, Month.February, 18, 12, 0).replaceYear(-1_000_000_000).isFailure)
        assertTrue(utcDt(2022, Month.February, 18, 12, 0).replaceYear(1_000_000_000).isFailure)

        assertEquals(
            utcDt(2022, Month.January, 18, 12, 0),
            utcDt(2022, Month.February, 18, 12, 0).replaceMonth(Month.January).getOrThrow(),
        )
        assertTrue(utcDt(2022, Month.January, 30, 12, 0).replaceMonth(Month.February).isFailure)

        assertEquals(
            utcDt(2022, Month.February, 1, 12, 0),
            utcDt(2022, Month.February, 18, 12, 0).replaceDay(1).getOrThrow(),
        )
        assertTrue(utcDt(2022, Month.February, 18, 12, 0).replaceDay(0).isFailure)
        assertTrue(utcDt(2022, Month.February, 18, 12, 0).replaceDay(30).isFailure)

        assertEquals(
            utcOrdDt(2022, 1, 12, 0),
            utcDt(2022, Month.February, 18, 12, 0).replaceOrdinal(1).getOrThrow(),
        )
        assertEquals(
            utcOrdDt(2024, 366, 12, 0),
            utcDt(2024, Month.February, 29, 12, 0).replaceOrdinal(366).getOrThrow(),
        )
        assertTrue(utcOrdDt(2022, 49, 12, 0).replaceOrdinal(0).isFailure)
        assertTrue(utcOrdDt(2022, 49, 12, 0).replaceOrdinal(366).isFailure)
        assertTrue(utcOrdDt(2022, 49, 12, 0).replaceOrdinal(367).isFailure)

        val base = utcDt(2022, Month.February, 18, 1, 2, 3, 4_005_006)
        assertEquals(
            utcDt(2022, Month.February, 18, 7, 2, 3, 4_005_006),
            base.replaceHour(7).getOrThrow(),
        )
        assertTrue(base.replaceHour(24).isFailure)

        assertEquals(
            utcDt(2022, Month.February, 18, 1, 7, 3, 4_005_006),
            base.replaceMinute(7).getOrThrow(),
        )
        assertTrue(base.replaceMinute(60).isFailure)

        assertEquals(
            utcDt(2022, Month.February, 18, 1, 2, 7, 4_005_006),
            base.replaceSecond(7).getOrThrow(),
        )
        assertTrue(base.replaceSecond(60).isFailure)

        assertEquals(
            utcDt(2022, Month.February, 18, 1, 2, 3, 7_000_000),
            base.replaceMillisecond(7).getOrThrow(),
        )
        assertTrue(base.replaceMillisecond(1_000).isFailure)

        assertEquals(
            utcDt(2022, Month.February, 18, 1, 2, 3, 7_008_000),
            base.replaceMicrosecond(7_008).getOrThrow(),
        )
        assertTrue(base.replaceMicrosecond(1_000_000).isFailure)

        assertEquals(
            utcDt(2022, Month.February, 18, 1, 2, 3, 7_008_009),
            base.replaceNanosecond(7_008_009).getOrThrow(),
        )
        assertTrue(base.replaceNanosecond(1_000_000_000).isFailure)
    }

    @Test
    fun truncateMethods() {
        val sample = utcDt(2021, Month.November, 12, 17, 47, 53, 123_456_789)
        val midnight = utcDt(2021, Month.November, 12, 0, 0)

        assertEquals(midnight, sample.truncateToDay())
        assertEquals(midnight, midnight.truncateToDay())

        assertEquals(utcDt(2021, Month.November, 12, 17, 0), sample.truncateToHour())
        assertEquals(midnight, midnight.truncateToHour())

        assertEquals(utcDt(2021, Month.November, 12, 17, 47), sample.truncateToMinute())
        assertEquals(midnight, midnight.truncateToMinute())

        assertEquals(utcDt(2021, Month.November, 12, 17, 47, 53), sample.truncateToSecond())
        assertEquals(midnight, midnight.truncateToSecond())

        assertEquals(utcDt(2021, Month.November, 12, 17, 47, 53, 123_000_000), sample.truncateToMillisecond())
        assertEquals(midnight, midnight.truncateToMillisecond())

        assertEquals(utcDt(2021, Month.November, 12, 17, 47, 53, 123_456_000), sample.truncateToMicrosecond())
        assertEquals(midnight, midnight.truncateToMicrosecond())
    }

    @Test
    fun arithmetic() {
        assertEquals(
            utcDt(2019, Month.January, 6, 0, 0),
            utcDt(2019, Month.January, 1, 0, 0) + 5.days(),
        )
        assertEquals(
            utcDt(2020, Month.January, 1, 0, 0),
            utcDt(2019, Month.December, 31, 0, 0) + 1.days(),
        )
        assertEquals(
            utcDt(2020, Month.January, 1, 0, 0, 1),
            utcDt(2019, Month.December, 31, 23, 59, 59) + 2.seconds(),
        )
        assertEquals(
            utcDt(2019, Month.December, 31, 23, 59, 59),
            utcDt(2020, Month.January, 1, 0, 0, 1) + (-2).seconds(),
        )
        assertEquals(
            utcDt(2000, Month.January, 1, 0, 0),
            utcDt(1999, Month.December, 31, 23, 0) + 1.hours(),
        )

        // StdDuration
        assertEquals(
            utcDt(2019, Month.January, 6, 0, 0),
            utcDt(2019, Month.January, 1, 0, 0) + 5.stdDays,
        )
        assertEquals(
            utcDt(2020, Month.January, 1, 0, 0, 1),
            utcDt(2019, Month.December, 31, 23, 59, 59) + 2.stdSeconds,
        )
        assertEquals(
            utcDt(2019, Month.January, 1, 0, 0),
            utcDt(2019, Month.January, 6, 0, 0) - 5.stdDays,
        )
        assertEquals(
            utcDt(2019, Month.December, 31, 23, 59, 59),
            utcDt(2020, Month.January, 1, 0, 0, 1) - 2.stdSeconds,
        )

        // Sub self and OffsetDateTime
        assertEquals(
            1.days(),
            utcDt(2019, Month.January, 2, 0, 0) - utcDt(2019, Month.January, 1, 0, 0),
        )
        assertEquals(
            (-1).days(),
            utcDt(2019, Month.January, 1, 0, 0) - utcDt(2019, Month.January, 2, 0, 0),
        )
        assertEquals(
            1.days(),
            utcDt(2019, Month.January, 2, 0, 0) -
                OffsetDateTime.newUtc(Date.fromCalendarDate(2019, Month.January, 1).getOrThrow(), Time.MIDNIGHT),
        )
    }

    @Test
    fun checkedAndSaturatingArithmetic() {
        val sample = utcDt(2021, Month.October, 25, 14, 1, 53, 450_000_000)
        assertEquals(
            utcDt(2021, Month.October, 25, 14, 1, 53, 450_000_005),
            sample.checkedAdd(5.nanoseconds()),
        )
        assertEquals(
            utcDt(2021, Month.October, 27, 14, 1, 53, 450_000_000),
            sample.checkedAdd(2.days()),
        )
        assertEquals(
            utcDt(2021, Month.November, 1, 14, 1, 53, 450_000_000),
            sample.checkedAdd(1.weeks()),
        )

        // Underflow / overflow at MIN / MAX
        assertNull(UtcDateTime.MIN.checkedAdd((-1).nanoseconds()))
        assertNull(UtcDateTime.MIN.checkedAdd(Duration.MIN))
        assertNull(UtcDateTime.MAX.checkedAdd(1.nanoseconds()))
        assertNull(UtcDateTime.MAX.checkedAdd(Duration.MAX))

        assertNull(UtcDateTime.MIN.checkedSub(1.nanoseconds()))
        assertNull(UtcDateTime.MIN.checkedSub(Duration.MAX))
        assertNull(UtcDateTime.MAX.checkedSub((-1).nanoseconds()))
        assertNull(UtcDateTime.MAX.checkedSub(Duration.MIN))

        assertEquals(UtcDateTime.MAX, UtcDateTime.MAX.checkedSub(Duration.ZERO))
        assertEquals(UtcDateTime.MIN, UtcDateTime.MIN.checkedSub(Duration.ZERO))

        assertEquals(UtcDateTime.MIN, UtcDateTime.MIN.saturatingAdd((-10).days()))
        assertEquals(UtcDateTime.MAX, UtcDateTime.MAX.saturatingAdd(10.days()))
        assertEquals(UtcDateTime.MIN, UtcDateTime.MIN.saturatingSub(10.days()))
        assertEquals(UtcDateTime.MAX, UtcDateTime.MAX.saturatingSub((-10).days()))
    }

    @Test
    fun toOffsetRegression() {
        val offset = UtcOffset.fromHms(24, 59, 0).getOrThrow()
        val value = utcDt(0, Month.January, 1, 23, 59).toOffset(offset)
        assertEquals(
            OffsetDateTime.newInOffset(
                Date.fromCalendarDate(0, Month.January, 3).getOrThrow(),
                Time.fromHms(0, 58, 0).getOrThrow(),
                offset,
            ),
            value,
        )
    }
}
