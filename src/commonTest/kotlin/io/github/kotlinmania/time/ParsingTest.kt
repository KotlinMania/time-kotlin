// port-lint: tests tests/integration/parsing.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.error.Parse
import io.github.kotlinmania.time.error.ParseFromDescription
import io.github.kotlinmania.time.error.TryFromParsed
import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatdescription.Component
import io.github.kotlinmania.time.formatdescription.OwnedFormatItem
import io.github.kotlinmania.time.formatdescription.modifier.End
import io.github.kotlinmania.time.formatdescription.modifier.Ignore
import io.github.kotlinmania.time.formatdescription.modifier.Month as MonthMod
import io.github.kotlinmania.time.formatdescription.modifier.MonthRepr
import io.github.kotlinmania.time.formatdescription.modifier.Ordinal
import io.github.kotlinmania.time.formatdescription.modifier.Padding
import io.github.kotlinmania.time.formatdescription.modifier.Period
import io.github.kotlinmania.time.formatdescription.modifier.Subsecond
import io.github.kotlinmania.time.formatdescription.modifier.SubsecondDigits
import io.github.kotlinmania.time.formatdescription.modifier.UnixTimestamp
import io.github.kotlinmania.time.formatdescription.modifier.UnixTimestampPrecision
import io.github.kotlinmania.time.formatdescription.modifier.WeekNumber
import io.github.kotlinmania.time.formatdescription.modifier.WeekNumberRepr
import io.github.kotlinmania.time.formatdescription.modifier.Weekday as WeekdayMod
import io.github.kotlinmania.time.formatdescription.modifier.WeekdayRepr
import io.github.kotlinmania.time.formatdescription.modifier.Year
import io.github.kotlinmania.time.formatdescription.modifier.YearRange
import io.github.kotlinmania.time.formatdescription.modifier.YearRepr
import io.github.kotlinmania.time.formatdescription.parse
import io.github.kotlinmania.time.formatdescription.parseOwned
import io.github.kotlinmania.time.formatdescription.wellknown.Iso8601
import io.github.kotlinmania.time.formatdescription.wellknown.Rfc2822
import io.github.kotlinmania.time.formatdescription.wellknown.Rfc3339
import io.github.kotlinmania.time.macros.date
import io.github.kotlinmania.time.macros.datetime
import io.github.kotlinmania.time.macros.offset
import io.github.kotlinmania.time.macros.time
import io.github.kotlinmania.time.macros.utcDatetime
import io.github.kotlinmania.time.parsing.Parsed
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ParsingTest {
    private fun assertInvalidLiteral(res: TimeResult<*, Parse>) {
        assertTrue(res is TimeResult.Err && res.error is Parse.ParseFromDescriptionErr && (res.error as Parse.ParseFromDescriptionErr).error is ParseFromDescription.InvalidLiteral, "Expected InvalidLiteral, got $res")
    }

    private fun assertInvalidComponent(res: TimeResult<*, Parse>, name: String) {
        assertEquals(TimeResult.Err(Parse.ParseFromDescriptionErr(ParseFromDescription.InvalidComponent(name))), res)
    }

    private fun assertUnexpectedTrailing(res: TimeResult<*, Parse>) {
        assertTrue(res is TimeResult.Err && res.error is Parse.ParseFromDescriptionErr && (res.error as Parse.ParseFromDescriptionErr).error is ParseFromDescription.UnexpectedTrailingCharacters, "Expected UnexpectedTrailingCharacters, got $res")
    }

    private fun assertComponentRange(res: TimeResult<*, Parse>, name: String, conditional: Boolean? = null) {
        assertTrue(res is TimeResult.Err && res.error is Parse.TryFromParsedErr && (res.error as Parse.TryFromParsedErr).error is TryFromParsed.ComponentRangeErr, "Expected ComponentRange, got $res")
        val comp = ((res as TimeResult.Err).error as Parse.TryFromParsedErr).error as TryFromParsed.ComponentRangeErr
        assertEquals(name, comp.error.name())
        if (conditional != null) {
            assertEquals(conditional, comp.error.isConditional())
        }
    }

    @Test
    fun rfc2822() {
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 0, 0), OffsetDateTime.parse("Sat, 02 Jan 2021 03:04:05 GMT", Rfc2822).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 0, 0), OffsetDateTime.parse("Sat, 02 Jan 2021 03:04:05 UT", Rfc2822).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 0, 0), OffsetDateTime.parse("Sat, 02 Jan 2021 03:04:05 +0000", Rfc2822).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 0, 6, 7), OffsetDateTime.parse("Sat, 02 Jan 2021 03:04:05 +0607", Rfc2822).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 0, -6, -7), OffsetDateTime.parse("Sat, 02 Jan 2021 03:04:05 -0607", Rfc2822).unwrap())
        assertEquals(datetime(2021, 12, 31, 23, 59, 59, 999_999_999, 0), OffsetDateTime.parse("Fri, 31 Dec 2021 23:59:60 Z", Rfc2822).unwrap())
        assertEquals(datetime(2021, 12, 31, 23, 59, 59, 999_999_999, 0), OffsetDateTime.parse("Fri, 31 Dec 2021 23:59:60 z", Rfc2822).unwrap())
        assertEquals(datetime(2021, 12, 31, 23, 59, 59, 999_999_999, 0), OffsetDateTime.parse("Fri, 31 Dec 2021 23:59:60 a", Rfc2822).unwrap())
        assertEquals(datetime(2021, 12, 31, 23, 59, 59, 999_999_999, 0), OffsetDateTime.parse("Fri, 31 Dec 2021 23:59:60 A", Rfc2822).unwrap())
        assertEquals(datetime(2021, 12, 31, 17, 52, 59, 999_999_999, -6, -7), OffsetDateTime.parse("Fri, 31 Dec 2021 17:52:60 -0607", Rfc2822).unwrap())
        assertEquals(datetime(2022, 1, 1, 6, 6, 59, 999_999_999, 6, 7), OffsetDateTime.parse("Sat, 01 Jan 2022 06:06:60 +0607", Rfc2822).unwrap())

        assertEquals(utcDatetime(2021, 1, 2, 3, 4, 5), UtcDateTime.parse("Sat, 02 Jan 2021 03:04:05 GMT", Rfc2822).unwrap())
        assertEquals(utcDatetime(2021, 1, 2, 3, 4, 5), UtcDateTime.parse("Sat, 02 Jan 2021 03:04:05 UT", Rfc2822).unwrap())
        assertEquals(utcDatetime(2021, 1, 2, 3, 4, 5), UtcDateTime.parse("Sat, 02 Jan 2021 03:04:05 +0000", Rfc2822).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 0, 6, 7).toUtc(), UtcDateTime.parse("Sat, 02 Jan 2021 03:04:05 +0607", Rfc2822).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 0, -6, -7).toUtc(), UtcDateTime.parse("Sat, 02 Jan 2021 03:04:05 -0607", Rfc2822).unwrap())
        assertEquals(utcDatetime(2021, 12, 31, 23, 59, 59, 999_999_999), UtcDateTime.parse("Fri, 31 Dec 2021 23:59:60 Z", Rfc2822).unwrap())
        assertEquals(utcDatetime(2021, 12, 31, 23, 59, 59, 999_999_999), UtcDateTime.parse("Fri, 31 Dec 2021 23:59:60 z", Rfc2822).unwrap())
        assertEquals(utcDatetime(2021, 12, 31, 23, 59, 59, 999_999_999), UtcDateTime.parse("Fri, 31 Dec 2021 23:59:60 a", Rfc2822).unwrap())
        assertEquals(utcDatetime(2021, 12, 31, 23, 59, 59, 999_999_999), UtcDateTime.parse("Fri, 31 Dec 2021 23:59:60 A", Rfc2822).unwrap())
        assertEquals(datetime(2021, 12, 31, 17, 52, 59, 999_999_999, -6, -7).toUtc(), UtcDateTime.parse("Fri, 31 Dec 2021 17:52:60 -0607", Rfc2822).unwrap())
        assertEquals(datetime(2022, 1, 1, 6, 6, 59, 999_999_999, 6, 7).toUtc(), UtcDateTime.parse("Sat, 01 Jan 2022 06:06:60 +0607", Rfc2822).unwrap())

        assertEquals(date(2021, 1, 2), Date.parse("Sat, 02 Jan 2021 03:04:05 GMT", Rfc2822).unwrap())
        assertEquals(date(2021, 1, 2), Date.parse("Sat, 02 Jan 2021 03:04:05 +0607", Rfc2822).unwrap())
        assertEquals(date(2021, 1, 2), Date.parse("Sat, 02 Jan 2021 03:04:05 -0607", Rfc2822).unwrap())
        assertEquals(date(2021, 1, 2), Date.parse("Sat, 02 Jan 21 03:04:05 -0607", Rfc2822).unwrap())
        assertEquals(date(1971, 1, 2), Date.parse("Sat, 02 Jan 71 03:04:05 -0607", Rfc2822).unwrap())

        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 0, 0), OffsetDateTime.parse("Sat,(\\\n)02 Jan 2021 03:04:05 GMT", Rfc2822).unwrap())
        assertEquals(
            time(3, 4, 5),
            Time.parse(
                "  \t Sat,\r\n (\tfoo012FOO!)(\u0001\u000b\u000e\u007f)(\\\u0000)(\\\u0001\\\t\\(\\)\\\\\\\u007f)(\\\n\\\u000b)02 \r\n  \r\n Jan 2021 03:04:05 GMT",
                Rfc2822
            ).unwrap()
        )
    }

    @Test
    fun issue661() {
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 0, 6, 7), OffsetDateTime.parse("02 Jan 2021 03:04:05 +0607", Rfc2822).unwrap())
        assertEquals(date(2021, 1, 2), Date.parse("02 Jan 2021 03:04:05 +0607", Rfc2822).unwrap())
    }

    @Test
    fun rfc2822Err() {
        assertInvalidComponent(OffsetDateTime.parse(" \r\nM", Rfc2822), "day")
        assertInvalidLiteral(OffsetDateTime.parse("Mon:", Rfc2822))
        assertInvalidComponent(OffsetDateTime.parse("Mon, o2", Rfc2822), "day")
        assertInvalidLiteral(OffsetDateTime.parse("Mon, 02_", Rfc2822))
        assertInvalidComponent(OffsetDateTime.parse("Mon, 02 jxn", Rfc2822), "month")
        assertInvalidLiteral(OffsetDateTime.parse("Mon, 02 Jan_", Rfc2822))
        assertInvalidComponent(OffsetDateTime.parse("Mon, 02 Jan abcd", Rfc2822), "year")
        assertInvalidComponent(OffsetDateTime.parse("Mon, 02 Jan 1899", Rfc2822), "year")
        assertInvalidLiteral(OffsetDateTime.parse("Mon, 02 Jan 2021_", Rfc2822))
        assertInvalidLiteral(OffsetDateTime.parse("Mon, 02 Jan 21_", Rfc2822))
        assertInvalidComponent(OffsetDateTime.parse("Mon, 02 Jan 2021 ab", Rfc2822), "hour")
        assertInvalidLiteral(OffsetDateTime.parse("Mon, 02 Jan 2021 03_", Rfc2822))
        assertInvalidComponent(OffsetDateTime.parse("Mon, 02 Jan 2021 03:ab", Rfc2822), "minute")
        assertInvalidLiteral(OffsetDateTime.parse("Mon, 02 Jan 2021 03:04_", Rfc2822))
        assertInvalidComponent(OffsetDateTime.parse("Mon, 02 Jan 2021 03:04:ab", Rfc2822), "second")
        assertInvalidLiteral(OffsetDateTime.parse("Mon, 02 Jan 2021 03:04:05_", Rfc2822))
        assertInvalidComponent(OffsetDateTime.parse("Mon, 02 Jan 2021 03:04 6", Rfc2822), "offset hour")
        assertInvalidComponent(OffsetDateTime.parse("Mon, 02 Jan 2021 03:04:05 -6", Rfc2822), "offset hour")
        assertInvalidComponent(OffsetDateTime.parse("Mon, 02 Jan 2021 03:04:05 -060", Rfc2822), "offset minute")

        assertComponentRange(OffsetDateTime.parse("Fri, 31 Dec 2021 23:59:61 Z", Rfc2822), "second", false)
        assertComponentRange(OffsetDateTime.parse("Fri, 31 Dec 2021 03:04:60 Z", Rfc2822), "second", true)
        assertComponentRange(OffsetDateTime.parse("Fri, 30 Dec 2021 23:59:60 Z", Rfc2822), "second", true)
    }

    @Test
    fun rfc3339() {
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 0, 0), OffsetDateTime.parse("2021-01-02T03:04:05Z", Rfc3339).unwrap())
        assertEquals(datetime(2021, 12, 31, 23, 59, 59, 999_999_999, 0), OffsetDateTime.parse("2021-12-31T23:59:60Z", Rfc3339).unwrap())
        assertEquals(datetime(2015, 6, 30, 23, 59, 59, 999_999_999, 0), OffsetDateTime.parse("2015-07-01T00:59:60+01:00", Rfc3339).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 100_000_000, 0), OffsetDateTime.parse("2021-01-02T03:04:05.1Z", Rfc3339).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 120_000_000, 0), OffsetDateTime.parse("2021-01-02T03:04:05.12Z", Rfc3339).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 123_000_000, 0), OffsetDateTime.parse("2021-01-02T03:04:05.123Z", Rfc3339).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 123_400_000, 0), OffsetDateTime.parse("2021-01-02T03:04:05.1234Z", Rfc3339).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 123_450_000, 0), OffsetDateTime.parse("2021-01-02T03:04:05.12345Z", Rfc3339).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 123_456_000, 0), OffsetDateTime.parse("2021-01-02T03:04:05.123456Z", Rfc3339).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 123_456_700, 0), OffsetDateTime.parse("2021-01-02T03:04:05.1234567Z", Rfc3339).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 123_456_780, 0), OffsetDateTime.parse("2021-01-02T03:04:05.12345678Z", Rfc3339).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 123_456_789, 0), OffsetDateTime.parse("2021-01-02T03:04:05.123456789Z", Rfc3339).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 123_456_789, -1, -2), OffsetDateTime.parse("2021-01-02T03:04:05.123456789-01:02", Rfc3339).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 123_456_789, 1, 2), OffsetDateTime.parse("2021-01-02T03:04:05.123456789+01:02", Rfc3339).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 123_000_000, 0, -1), OffsetDateTime.parse("2021-01-02T03:04:05.123-00:01", Rfc3339).unwrap())

        assertEquals(date(2021, 1, 2), Date.parse("2021-01-02T03:04:05Z", Rfc3339).unwrap())
        assertEquals(date(2021, 1, 2), Date.parse("2021-01-02T03:04:05.123+01:02", Rfc3339).unwrap())
        assertEquals(date(2021, 1, 2), Date.parse("2021-01-02T03:04:05.123-01:02", Rfc3339).unwrap())

        assertEquals(UtcOffset.UTC, UtcOffset.parse("2021-01-02T03:04:05Z", Rfc3339).unwrap())
        assertEquals(offset(1, 2), UtcOffset.parse("2021-01-02T03:04:05.123+01:02", Rfc3339).unwrap())
        assertEquals(offset(-1, -2), UtcOffset.parse("2021-01-02T03:04:05.123-01:02", Rfc3339).unwrap())
        assertEquals(offset(0, -1), UtcOffset.parse("2021-01-02T03:04:05.123-00:01", Rfc3339).unwrap())

        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 0, 0), OffsetDateTime.parse("2021-01-02 03:04:05Z", Rfc3339).unwrap())
        assertEquals(datetime(2021, 1, 2, 3, 4, 5, 0, 0), OffsetDateTime.parse("2021-01-02$03:04:05Z", Rfc3339).unwrap())
    }

    @Test
    fun rfc3339Err() {
        assertInvalidComponent(PrimitiveDateTime.parse("x", Rfc3339), "year")
        assertInvalidLiteral(PrimitiveDateTime.parse("2021x", Rfc3339))
        assertInvalidComponent(PrimitiveDateTime.parse("2021-x", Rfc3339), "month")
        assertInvalidComponent(PrimitiveDateTime.parse("2021-0", Rfc3339), "month")
        assertInvalidLiteral(PrimitiveDateTime.parse("2021-01x", Rfc3339))
        assertInvalidComponent(PrimitiveDateTime.parse("2021-01-0", Rfc3339), "day")
        assertInvalidComponent(PrimitiveDateTime.parse("2021-01-01", Rfc3339), "separator")
        assertInvalidComponent(PrimitiveDateTime.parse("2021-01-01T0", Rfc3339), "hour")
        assertInvalidLiteral(PrimitiveDateTime.parse("2021-01-01T00x", Rfc3339))
        assertInvalidComponent(PrimitiveDateTime.parse("2021-01-01T00:0", Rfc3339), "minute")
        assertInvalidLiteral(PrimitiveDateTime.parse("2021-01-01T00:00x", Rfc3339))
        assertInvalidComponent(PrimitiveDateTime.parse("2021-01-01T00:00:0", Rfc3339), "second")
        assertInvalidComponent(PrimitiveDateTime.parse("2021-01-01T00:00:00.x", Rfc3339), "subsecond")
        assertInvalidComponent(PrimitiveDateTime.parse("2021-01-01T00:00:00x", Rfc3339), "offset hour")
        assertInvalidComponent(PrimitiveDateTime.parse("2021-01-01T00:00:00+0", Rfc3339), "offset hour")
        assertInvalidLiteral(PrimitiveDateTime.parse("2021-01-01T00:00:00+00x", Rfc3339))
        assertInvalidComponent(PrimitiveDateTime.parse("2021-01-01T00:00:00+00:0", Rfc3339), "offset minute")
        assertInvalidComponent(PrimitiveDateTime.parse("2021-13-01T00:00:00Z", Rfc3339), "month")
        assertComponentRange(PrimitiveDateTime.parse("2021-01-02T03:04:60Z", Rfc3339), "second")
        assertComponentRange(PrimitiveDateTime.parse("2022-01-01T00:59:60+01:00", Rfc3339), "second")
        assertComponentRange(Time.parse("2021-12-31T23:04:60Z", Rfc3339), "second")

        assertUnexpectedTrailing(OffsetDateTime.parse("2021-01-02T03:04:05Z ", Rfc3339))
        assertInvalidComponent(OffsetDateTime.parse("x", Rfc3339), "year")
        assertInvalidLiteral(OffsetDateTime.parse("2021x", Rfc3339))
        assertInvalidComponent(OffsetDateTime.parse("2021-x", Rfc3339), "month")
        assertInvalidComponent(OffsetDateTime.parse("2021-0", Rfc3339), "month")
        assertInvalidLiteral(OffsetDateTime.parse("2021-01x", Rfc3339))
        assertInvalidComponent(OffsetDateTime.parse("2021-01-0", Rfc3339), "day")
        assertInvalidComponent(OffsetDateTime.parse("2021-01-01", Rfc3339), "separator")
        assertInvalidComponent(OffsetDateTime.parse("2021-01-01T0", Rfc3339), "hour")
        assertInvalidLiteral(OffsetDateTime.parse("2021-01-01T00x", Rfc3339))
        assertInvalidComponent(OffsetDateTime.parse("2021-01-01T00:0", Rfc3339), "minute")
        assertInvalidLiteral(OffsetDateTime.parse("2021-01-01T00:00x", Rfc3339))
        assertInvalidComponent(OffsetDateTime.parse("2021-01-01T00:00:0", Rfc3339), "second")
        assertInvalidComponent(OffsetDateTime.parse("2021-01-01T00:00:00.x", Rfc3339), "subsecond")
        assertInvalidComponent(OffsetDateTime.parse("2021-01-01T00:00:00x", Rfc3339), "offset hour")
        assertInvalidComponent(OffsetDateTime.parse("2021-01-01T00:00:00+0", Rfc3339), "offset hour")
        assertInvalidLiteral(OffsetDateTime.parse("2021-01-01T00:00:00+00x", Rfc3339))
        assertInvalidComponent(OffsetDateTime.parse("2021-01-01T00:00:00+00:0", Rfc3339), "offset minute")
        assertInvalidComponent(OffsetDateTime.parse("2021-01-01T00:00:00+24:00", Rfc3339), "offset hour")
        assertComponentRange(OffsetDateTime.parse("2021-01-01T00:00:00+00:60", Rfc3339), "offset minute")
        assertComponentRange(OffsetDateTime.parse("2021-13-01T00:00:00Z", Rfc3339), "month")
        assertComponentRange(OffsetDateTime.parse("2021-12-31T23:59:61Z", Rfc3339), "second", false)
        assertComponentRange(OffsetDateTime.parse("2021-01-02T23:59:60Z", Rfc3339), "second", true)
        assertComponentRange(OffsetDateTime.parse("2021-12-31T03:04:60Z", Rfc3339), "second", true)
        assertComponentRange(OffsetDateTime.parse("2021-12-31T23:59:60+01:00", Rfc3339), "second", true)
    }

    @Test
    fun iso8601() {
        assertEquals(TimeResult.Ok(datetime(2021, 1, 2, 3, 4, 5, 0, 0)), OffsetDateTime.parse("2021-01-02T03:04:05Z", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(datetime(2021, 1, 2, 3, 4, 5, 0, 0)), OffsetDateTime.parse("2021-002T03:04:05Z", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(Date.fromIsoWeekDate(2021, 1u, Weekday.Tuesday).unwrap().withHms(3u, 4u, 5u).unwrap().assumeUtc()), OffsetDateTime.parse("2021-W01-2T03:04:05Z", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(datetime(-2021, 1, 2, 3, 4, 5, 0, 1)), OffsetDateTime.parse("-002021-01-02T03:04:05+01:00", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(datetime(2021, 1, 2, 3, 6, 0, 0, 0)), OffsetDateTime.parse("20210102T03.1Z", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(datetime(2021, 1, 2, 3, 4, 6, 0, 0)), OffsetDateTime.parse("2021002T0304.1Z", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(Date.fromIsoWeekDate(2021, 1u, Weekday.Tuesday).unwrap().withHmsNano(3u, 4u, 5u, 100_000_000u).unwrap().assumeOffset(offset(-1))), OffsetDateTime.parse("2021W012T030405.1-0100", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(datetime(2021, 1, 2, 3, 0, 0, 0, 0)), OffsetDateTime.parse("20210102T03Z", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(datetime(2021, 1, 2, 3, 4, 0, 0, 0)), OffsetDateTime.parse("20210102T0304Z", Iso8601.DEFAULT))

        assertEquals(TimeResult.Ok(utcDatetime(2021, 1, 2, 3, 4, 5)), UtcDateTime.parse("2021-01-02T03:04:05Z", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(utcDatetime(2021, 1, 2, 3, 4, 5)), UtcDateTime.parse("2021-002T03:04:05Z", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(UtcDateTime.new(Date.fromIsoWeekDate(2021, 1u, Weekday.Tuesday).unwrap(), time(3, 4, 5))), UtcDateTime.parse("2021-W01-2T03:04:05Z", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(datetime(-2021, 1, 2, 3, 4, 5, 0, 1).toUtc()), UtcDateTime.parse("-002021-01-02T03:04:05+01:00", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(utcDatetime(2021, 1, 2, 3, 6, 0)), UtcDateTime.parse("20210102T03.1Z", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(utcDatetime(2021, 1, 2, 3, 4, 6)), UtcDateTime.parse("2021002T0304.1Z", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(Date.fromIsoWeekDate(2021, 1u, Weekday.Tuesday).unwrap().withHmsNano(3u, 4u, 5u, 100_000_000u).unwrap().assumeOffset(offset(-1)).toUtc()), UtcDateTime.parse("2021W012T030405.1-0100", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(utcDatetime(2021, 1, 2, 3, 0, 0)), UtcDateTime.parse("20210102T03Z", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(utcDatetime(2021, 1, 2, 3, 4, 0)), UtcDateTime.parse("20210102T0304Z", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(offset(7)), UtcOffset.parse("+07", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(offset(3, 4)), UtcOffset.parse("+0304", Iso8601.DEFAULT))
        assertEquals(TimeResult.Ok(datetime(2022, 7, 22, 12, 52, 50, 349_409_000)), PrimitiveDateTime.parse("2022-07-22T12:52:50.349409", Iso8601.DEFAULT))
    }

    @Test
    fun iso8601Error() {
        assertUnexpectedTrailing(OffsetDateTime.parse("20210102T03:04Z", Iso8601.DEFAULT))
        assertUnexpectedTrailing(OffsetDateTime.parse("20210102T03.", Iso8601.DEFAULT))
        assertUnexpectedTrailing(OffsetDateTime.parse("2021-0102", Iso8601.DEFAULT))
        assertUnexpectedTrailing(OffsetDateTime.parse("2021-01-x", Iso8601.DEFAULT))
        assertUnexpectedTrailing(OffsetDateTime.parse("2021-Wx", Iso8601.DEFAULT))
        assertUnexpectedTrailing(OffsetDateTime.parse("2021-W012", Iso8601.DEFAULT))
        assertUnexpectedTrailing(OffsetDateTime.parse("2021-W01-x", Iso8601.DEFAULT))
        assertUnexpectedTrailing(OffsetDateTime.parse("2021-01-02T03:x", Iso8601.DEFAULT))
        assertUnexpectedTrailing(OffsetDateTime.parse("2021-01-02T03:04x", Iso8601.DEFAULT))
        assertUnexpectedTrailing(OffsetDateTime.parse("2021-01-02T03:04:", Iso8601.DEFAULT))
        assertEquals(TimeResult.Err(Parse.TryFromParsedErr(TryFromParsed.InsufficientInformation)), OffsetDateTime.parse("01:02", Iso8601.DEFAULT))

        assertUnexpectedTrailing(UtcDateTime.parse("20210102T03:04Z", Iso8601.DEFAULT))
        assertUnexpectedTrailing(UtcDateTime.parse("20210102T03.", Iso8601.DEFAULT))
        assertUnexpectedTrailing(UtcDateTime.parse("2021-0102", Iso8601.DEFAULT))
        assertUnexpectedTrailing(UtcDateTime.parse("2021-01-x", Iso8601.DEFAULT))
        assertUnexpectedTrailing(UtcDateTime.parse("2021-Wx", Iso8601.DEFAULT))
        assertUnexpectedTrailing(UtcDateTime.parse("2021-W012", Iso8601.DEFAULT))
        assertUnexpectedTrailing(UtcDateTime.parse("2021-W01-x", Iso8601.DEFAULT))
        assertUnexpectedTrailing(UtcDateTime.parse("2021-01-02T03:x", Iso8601.DEFAULT))
        assertUnexpectedTrailing(UtcDateTime.parse("2021-01-02T03:04x", Iso8601.DEFAULT))
        assertUnexpectedTrailing(UtcDateTime.parse("2021-01-02T03:04:", Iso8601.DEFAULT))
        assertEquals(TimeResult.Err(Parse.TryFromParsedErr(TryFromParsed.InsufficientInformation)), UtcDateTime.parse("01:02", Iso8601.DEFAULT))
    }

    @Test
    fun parseTime() {
        val formatInputOutput = listOf(
            Triple(parse("[hour repr:12] [period]").unwrap(), "01 PM", time(13, 0)),
            Triple(parse("[hour]").unwrap(), "12", time(12, 0)),
            Triple(parse("[hour]:[minute]:[second]").unwrap(), "13:02:03", time(13, 2, 3)),
            Triple(parse("[hour repr:12]:[minute] [period]").unwrap(), "01:02 PM", time(13, 2)),
            Triple(parse("[hour]:[minute]").unwrap(), "01:02", time(1, 2)),
            Triple(parse("[hour repr:12]:[minute] [period]").unwrap(), "01:02 AM", time(1, 2)),
            Triple(parse("[hour]:[minute]").unwrap(), "01:02", time(1, 2)),
            Triple(parse("[hour repr:12] [period]").unwrap(), "12 AM", time(0, 0)),
            Triple(parse("[hour repr:12] [period]").unwrap(), "12 PM", time(12, 0)),
        )

        for ((fd, input, output) in formatInputOutput) {
            assertEquals(output, Time.parse(input, fd).unwrap())
            assertEquals(output, Time.parse(input, OwnedFormatItem.fromBorrowed(fd)).unwrap())
            assertEquals(output, Time.parse(input, listOf(OwnedFormatItem.fromBorrowed(fd))).unwrap())
        }
    }

    @Test
    fun parseTimeErr() {
        assertEquals(TimeResult.Err(TryFromParsed.InsufficientInformation), Time.tryFromParsed(Parsed.new()))
        assertEquals(TimeResult.Err(Parse.TryFromParsedErr(TryFromParsed.InsufficientInformation)), Time.parse("", parse("").unwrap()))
        assertEquals(TimeResult.Err(Parse.TryFromParsedErr(TryFromParsed.InsufficientInformation)), Time.parse("12:34", parse("[hour]:[second]").unwrap()))
        assertEquals(TimeResult.Err(Parse.TryFromParsedErr(TryFromParsed.InsufficientInformation)), Time.parse("12:34", parse("[hour]:[subsecond]").unwrap()))
        assertInvalidComponent(Time.parse("13 PM", parse("[hour repr:12] [period]").unwrap()), "hour")
        assertUnexpectedTrailing(Time.parse(" ", parse("").unwrap()))
        assertInvalidComponent(Time.parse("a", parse("[subsecond digits:1]").unwrap()), "subsecond")
        assertInvalidComponent(Time.parse("1a", parse("[subsecond digits:2]").unwrap()), "subsecond")
        assertInvalidComponent(Time.parse("1a", parseOwned("[subsecond digits:2]", 2).unwrap()), "subsecond")
        assertInvalidComponent(Time.parse("1a", listOf(parseOwned("[subsecond digits:2]", 2).unwrap())), "subsecond")
        assertInvalidComponent(Time.parse("12a", parse("[subsecond digits:3]").unwrap()), "subsecond")
        assertInvalidComponent(Time.parse("123a", parse("[subsecond digits:4]").unwrap()), "subsecond")
        assertInvalidComponent(Time.parse("1234a", parse("[subsecond digits:5]").unwrap()), "subsecond")
        assertInvalidComponent(Time.parse("12345a", parse("[subsecond digits:6]").unwrap()), "subsecond")
        assertInvalidComponent(Time.parse("123456a", parse("[subsecond digits:7]").unwrap()), "subsecond")
        assertInvalidComponent(Time.parse("1234567a", parse("[subsecond digits:8]").unwrap()), "subsecond")
        assertInvalidComponent(Time.parse("12345678a", parse("[subsecond digits:9]").unwrap()), "subsecond")
    }

    @Test
    fun parseDate() {
        val formatInputOutput = listOf(
            Triple(parse("[year]-[month]-[day]").unwrap(), "2021-01-02", date(2021, 1, 2)),
            Triple(parse("[year repr:century range:standard][year repr:last_two]-[month]-[day]").unwrap(), "2021-01-02", date(2021, 1, 2)),
            Triple(parse("[year]-[ordinal]").unwrap(), "2021-002", date(2021, 2)),
            Triple(parse("[year base:iso_week]-W[week_number]-[weekday repr:monday]").unwrap(), "2020-W53-6", date(2021, 1, 2)),
            Triple(parse("[year]-W[week_number repr:monday]-[weekday repr:monday]").unwrap(), "2021-W00-6", date(2021, 1, 2)),
            Triple(parse("[year]-W[week_number repr:sunday]-[weekday repr:sunday]").unwrap(), "2021-W00-6", date(2021, 1, 2)),
            Triple(parse("[year]-W[week_number repr:sunday]-[weekday repr:sunday]").unwrap(), "2023-W01-1", date(2023, 1, 2)),
            Triple(parse("[year]-W[week_number repr:sunday]-[weekday repr:sunday]").unwrap(), "2022-W00-7", date(2022, 1, 2)),
            Triple(parse("[year]-W[week_number repr:sunday]-[weekday repr:sunday]").unwrap(), "2026-W00-5", date(2026, 1, 2)),
            Triple(parse("[year]-W[week_number repr:sunday]-[weekday repr:sunday]").unwrap(), "2025-W00-4", date(2025, 1, 2)),
            Triple(parse("[year]-W[week_number repr:sunday]-[weekday repr:sunday]").unwrap(), "2019-W00-3", date(2019, 1, 2)),
            Triple(parse("[year]-W[week_number repr:sunday]-[weekday repr:sunday]").unwrap(), "2018-W01-2", date(2018, 1, 2)),
            Triple(parse("[year padding:space]-W[week_number repr:sunday padding:none]-[weekday repr:sunday]").unwrap(), " 201-W01-2", date(201, 1, 6)),
        )

        for ((fd, input, output) in formatInputOutput) {
            assertEquals(output, Date.parse(input, fd).unwrap())
            assertEquals(output, Date.parse(input, OwnedFormatItem.fromBorrowed(fd)).unwrap())
        }
    }

    @Test
    fun parseDateErr() {
        assertEquals(TimeResult.Err(TryFromParsed.InsufficientInformation), Date.tryFromParsed(Parsed.new()))
        assertEquals(TimeResult.Err(Parse.TryFromParsedErr(TryFromParsed.InsufficientInformation)), Date.parse("", parse("").unwrap()))
        assertInvalidComponent(Date.parse("a", parse("[year]").unwrap()), "year")
        assertInvalidComponent(Date.parse("0001", parse("[year sign:mandatory]").unwrap()), "year")
        assertInvalidComponent(Date.parse("0a", parse("[year repr:last_two]").unwrap()), "year")
        assertComponentRange(Date.parse("2021-366", parse("[year]-[ordinal]").unwrap()), "ordinal")
        assertInvalidComponent(Date.parse("2021-12-32", parse("[year]-[month]-[day]").unwrap()), "day")
        assertComponentRange(Date.parse("2021-02-30", parse("[year]-[month]-[day]").unwrap()), "day")
        assertComponentRange(Date.parse("2019-W53-1", parse("[year base:iso_week]-W[week_number]-[weekday repr:monday]").unwrap()), "week")
        assertInvalidComponent(Date.parse("2021-W54-1", parse("[year base:iso_week]-W[week_number]-[weekday repr:monday]").unwrap()), "week number")
        assertComponentRange(Date.parse("2019-W53-1", parse("[year]-W[week_number repr:sunday]-[weekday repr:monday]").unwrap()), "ordinal")
        assertInvalidComponent(Date.parse("2021-W54-1", parse("[year]-W[week_number repr:sunday]-[weekday repr:monday]").unwrap()), "week number")
        assertComponentRange(Date.parse("2019-W53-1", parse("[year]-W[week_number repr:monday]-[weekday repr:monday]").unwrap()), "ordinal")
        assertInvalidComponent(Date.parse("2021-W54-1", parse("[year]-W[week_number repr:monday]-[weekday repr:monday]").unwrap()), "week number")
        assertInvalidComponent(Date.parse("Ja", parse("[month repr:short]").unwrap()), "month")
        assertInvalidComponent(Date.parse("  2a21", parse("[year padding:space]").unwrap()), "year")
    }

    @Test
    fun parseOffset() {
        assertEquals(TimeResult.Ok(offset(0, -1)), UtcOffset.parse("-00:01", parse("[offset_hour sign:mandatory]:[offset_minute]").unwrap()))
        assertEquals(TimeResult.Ok(offset(0, 0, -1)), UtcOffset.parse("-00:00:01", parse("[offset_hour sign:mandatory]:[offset_minute]:[offset_second]").unwrap()))
    }

    @Test
    fun parseOffsetErr() {
        assertEquals(TimeResult.Err(Parse.TryFromParsedErr(TryFromParsed.InsufficientInformation)), UtcOffset.parse("", parse("").unwrap()))
        assertInvalidComponent(UtcOffset.parse("01", parse("[offset_hour sign:mandatory]").unwrap()), "offset hour")
        assertInvalidComponent(UtcOffset.parse("24", parse("[offset_hour]").unwrap()), "offset hour")
        assertInvalidComponent(UtcOffset.parse("00:60", parse("[offset_hour]:[offset_minute]").unwrap()), "offset minute")
        assertInvalidComponent(UtcOffset.parse("00:00:60", parse("[offset_hour]:[offset_minute]:[offset_second]").unwrap()), "offset second")
    }

    @Test
    fun parsePrimitiveDateTime() {
        assertEquals(TimeResult.Ok(datetime(2023, 7, 27, 23, 0)), PrimitiveDateTime.parse("2023-07-27 23", parse("[year]-[month]-[day] [hour]").unwrap()))
    }

    @Test
    fun parsePrimitiveDateTimeErr() {
        assertEquals(TimeResult.Err(Parse.TryFromParsedErr(TryFromParsed.InsufficientInformation)), PrimitiveDateTime.parse("", parse("").unwrap()))
        assertInvalidComponent(PrimitiveDateTime.parse("2021-001 13 PM", parse("[year]-[ordinal] [hour repr:12] [period]").unwrap()), "hour")
        assertUnexpectedTrailing(PrimitiveDateTime.parse("2023-07-27 23:30", parse("[year]-[month]-[day] [hour]").unwrap()))
    }

    @Test
    fun parseOffsetDateTimeErr() {
        assertEquals(TimeResult.Err(Parse.TryFromParsedErr(TryFromParsed.InsufficientInformation)), OffsetDateTime.parse("", parse("").unwrap()))
        assertInvalidComponent(OffsetDateTime.parse("x", parse("[year]").unwrap()), "year")
        assertInvalidComponent(OffsetDateTime.parse("2021-001 12 PM +25", parse("[year]-[ordinal] [hour repr:12] [period] [offset_hour sign:mandatory]").unwrap()), "offset hour")
    }

    @Test
    fun parseUtcDateTime() {
        assertEquals(TimeResult.Ok(utcDatetime(2023, 7, 27, 23, 0)), UtcDateTime.parse("2023-07-27 23", parse("[year]-[month]-[day] [hour]").unwrap()))
    }

    @Test
    fun parseUtcDateTimeErr() {
        assertEquals(TimeResult.Err(Parse.TryFromParsedErr(TryFromParsed.InsufficientInformation)), UtcDateTime.parse("", parse("").unwrap()))
        assertInvalidComponent(UtcDateTime.parse("2021-001 13 PM", parse("[year]-[ordinal] [hour repr:12] [period]").unwrap()), "hour")
        assertUnexpectedTrailing(UtcDateTime.parse("2023-07-27 23:30", parse("[year]-[month]-[day] [hour]").unwrap()))
        assertInvalidComponent(UtcDateTime.parse("x", parse("[year]").unwrap()), "year")
        assertInvalidComponent(UtcDateTime.parse("2021-001 12 PM +25", parse("[year]-[ordinal] [hour repr:12] [period] [offset_hour sign:mandatory]").unwrap()), "offset hour")
    }

    @Test
    fun parseComponents() {
        fun parseComp(component: Component, input: String): Parsed {
            val parsed = Parsed.new()
            parsed.parseComponent(input.encodeToByteArray(), component).unwrap()
            return parsed
        }

        assertEquals(2021, parseComp(Component.Year(Year.default().withPadding(Padding.Zero).withRepr(YearRepr.Full).withRange(YearRange.Extended).withIsoWeekBased(false).withSignIsMandatory(false)), "2021").year())
        run {
            val p = parseComp(Component.Year(Year.default().withPadding(Padding.Zero).withRepr(YearRepr.Century).withRange(YearRange.Extended).withIsoWeekBased(false).withSignIsMandatory(false)), "20")
            assertEquals(20, p.yearCentury())
            assertEquals(false, p.yearCenturyIsNegative())
        }
        assertEquals(21u, parseComp(Component.Year(Year.default().withPadding(Padding.Zero).withRepr(YearRepr.LastTwo).withRange(YearRange.Extended).withIsoWeekBased(false).withSignIsMandatory(false)), "21").yearLastTwo())
        assertEquals(2021, parseComp(Component.Year(Year.default().withPadding(Padding.Zero).withRepr(YearRepr.Full).withRange(YearRange.Extended).withIsoWeekBased(true).withSignIsMandatory(false)), "2021").isoYear())
        run {
            val p = parseComp(Component.Year(Year.default().withPadding(Padding.Zero).withRepr(YearRepr.Century).withRange(YearRange.Extended).withIsoWeekBased(true).withSignIsMandatory(false)), "20")
            assertEquals(20, p.isoYearCentury())
            assertEquals(false, p.isoYearCenturyIsNegative())
        }
        assertEquals(21u, parseComp(Component.Year(Year.default().withPadding(Padding.Zero).withRepr(YearRepr.LastTwo).withRange(YearRange.Extended).withIsoWeekBased(true).withSignIsMandatory(false)), "21").isoYearLastTwo())
        assertEquals(Month.January, parseComp(Component.Month(MonthMod.default().withPadding(Padding.Space).withRepr(MonthRepr.Numerical)), " 1").month())
        assertEquals(Month.January, parseComp(Component.Month(MonthMod.default().withPadding(Padding.None).withRepr(MonthRepr.Short).withCaseSensitive(true)), "Jan").month())
        assertEquals(Month.January, parseComp(Component.Month(MonthMod.default().withPadding(Padding.None).withRepr(MonthRepr.Short).withCaseSensitive(false)), "jAn").month())
        assertEquals(Month.January, parseComp(Component.Month(MonthMod.default().withPadding(Padding.None).withRepr(MonthRepr.Long).withCaseSensitive(true)), "January").month())
        assertEquals(Month.January, parseComp(Component.Month(MonthMod.default().withPadding(Padding.None).withRepr(MonthRepr.Long).withCaseSensitive(false)), "jAnUaRy").month())
        assertEquals(12u, parseComp(Component.Ordinal(Ordinal.default().withPadding(Padding.Zero)), "012").ordinal())
        assertEquals(Weekday.Sunday, parseComp(Component.Weekday(WeekdayMod.default().withRepr(WeekdayRepr.Short).withOneIndexed(false).withCaseSensitive(true)), "Sun").weekday())
        assertEquals(Weekday.Sunday, parseComp(Component.Weekday(WeekdayMod.default().withRepr(WeekdayRepr.Short).withOneIndexed(false).withCaseSensitive(false)), "sUn").weekday())
        assertEquals(Weekday.Sunday, parseComp(Component.Weekday(WeekdayMod.default().withRepr(WeekdayRepr.Long).withOneIndexed(false).withCaseSensitive(true)), "Sunday").weekday())
        assertEquals(Weekday.Sunday, parseComp(Component.Weekday(WeekdayMod.default().withRepr(WeekdayRepr.Long).withOneIndexed(false).withCaseSensitive(false)), "sUnDaY").weekday())
        assertEquals(Weekday.Sunday, parseComp(Component.Weekday(WeekdayMod.default().withRepr(WeekdayRepr.Sunday).withOneIndexed(false)), "0").weekday())
        assertEquals(Weekday.Sunday, parseComp(Component.Weekday(WeekdayMod.default().withRepr(WeekdayRepr.Sunday).withOneIndexed(true)), "1").weekday())
        assertEquals(Weekday.Sunday, parseComp(Component.Weekday(WeekdayMod.default().withRepr(WeekdayRepr.Monday).withOneIndexed(false)), "6").weekday())
        assertEquals(Weekday.Sunday, parseComp(Component.Weekday(WeekdayMod.default().withRepr(WeekdayRepr.Monday).withOneIndexed(true)), "7").weekday())
        assertEquals(2u, parseComp(Component.WeekNumber(WeekNumber.default().withPadding(Padding.None).withRepr(WeekNumberRepr.Sunday)), "2").sundayWeekNumber())
        assertEquals(2u, parseComp(Component.WeekNumber(WeekNumber.default().withPadding(Padding.None).withRepr(WeekNumberRepr.Monday)), "2").mondayWeekNumber())
        assertEquals(2u, parseComp(Component.WeekNumber(WeekNumber.default().withPadding(Padding.None).withRepr(WeekNumberRepr.Iso)), "2").isoWeekNumber())
        assertEquals(100_000_000u, parseComp(Component.Subsecond(Subsecond.default().withDigits(SubsecondDigits.One)), "1").subsecond())
        assertEquals(120_000_000u, parseComp(Component.Subsecond(Subsecond.default().withDigits(SubsecondDigits.Two)), "12").subsecond())
        assertEquals(123_000_000u, parseComp(Component.Subsecond(Subsecond.default().withDigits(SubsecondDigits.Three)), "123").subsecond())
        assertEquals(123_400_000u, parseComp(Component.Subsecond(Subsecond.default().withDigits(SubsecondDigits.Four)), "1234").subsecond())
        assertEquals(123_450_000u, parseComp(Component.Subsecond(Subsecond.default().withDigits(SubsecondDigits.Five)), "12345").subsecond())
        assertEquals(123_456_000u, parseComp(Component.Subsecond(Subsecond.default().withDigits(SubsecondDigits.Six)), "123456").subsecond())
        assertEquals(123_456_700u, parseComp(Component.Subsecond(Subsecond.default().withDigits(SubsecondDigits.Seven)), "1234567").subsecond())
        assertEquals(123_456_780u, parseComp(Component.Subsecond(Subsecond.default().withDigits(SubsecondDigits.Eight)), "12345678").subsecond())
        assertEquals(123_456_789u, parseComp(Component.Subsecond(Subsecond.default().withDigits(SubsecondDigits.Nine)), "123456789").subsecond())
        assertEquals(123_456_789u, parseComp(Component.Subsecond(Subsecond.default().withDigits(SubsecondDigits.OneOrMore)), "123456789").subsecond())
        assertEquals(false, parseComp(Component.Period(Period.default().withIsUppercase(false).withCaseSensitive(true)), "am").hour12IsPm())
        assertEquals(false, parseComp(Component.Period(Period.default().withIsUppercase(false).withCaseSensitive(false)), "aM").hour12IsPm())

        run {
            val parsed = Parsed.new()
            val result = parsed.parseComponent("abcdef".encodeToByteArray(), Component.Ignore(Ignore.count(3u))).unwrap()
            assertContentEquals("def".encodeToByteArray(), result)
        }
        run {
            val parsed = Parsed.new()
            val result = parsed.parseComponent("abcdef".encodeToByteArray(), Component.Ignore(Ignore.count(7u)))
            assertEquals(TimeResult.Err(ParseFromDescription.InvalidComponent("ignore")), result)
        }
        assertEquals(I128.fromLong(1_234_567_890L) * I128.fromLong(1_000_000_000L), parseComp(Component.UnixTimestamp(UnixTimestamp.default().withPrecision(UnixTimestampPrecision.Second).withSignIsMandatory(false)), "1234567890").unixTimestampNanos())
        assertEquals(I128.fromLong(1_234_567_890_123L) * I128.fromLong(1_000_000L), parseComp(Component.UnixTimestamp(UnixTimestamp.default().withPrecision(UnixTimestampPrecision.Millisecond).withSignIsMandatory(false)), "1234567890123").unixTimestampNanos())
        assertEquals(I128.fromLong(1_234_567_890_123_456L) * I128.fromLong(1_000L), parseComp(Component.UnixTimestamp(UnixTimestamp.default().withPrecision(UnixTimestampPrecision.Microsecond).withSignIsMandatory(false)), "1234567890123456").unixTimestampNanos())
        assertEquals(I128.fromLong(1_234_567_890_123_456_789L), parseComp(Component.UnixTimestamp(UnixTimestamp.default().withPrecision(UnixTimestampPrecision.Nanosecond).withSignIsMandatory(false)), "1234567890123456789").unixTimestampNanos())
        assertEquals(I128.fromLong(-1_234_567_890_123_456_789L), parseComp(Component.UnixTimestamp(UnixTimestamp.default().withPrecision(UnixTimestampPrecision.Nanosecond).withSignIsMandatory(false)), "-1234567890123456789").unixTimestampNanos())
    }

    @Test
    fun parseOptional() {
        run {
            val parsed = Parsed.new()
            val remaining = parsed.parseItem(
                "2021-01-02".encodeToByteArray(),
                BorrowedFormatItem.Optional(BorrowedFormatItem.Compound(parse("[year]-[month]-[day]").unwrap()))
            ).unwrap()
            assertTrue(remaining.isEmpty())
            assertEquals(2021, parsed.year())
            assertEquals(Month.January, parsed.month())
            assertEquals(2u, parsed.day())
        }
        run {
            val parsed = Parsed.new()
            val remaining = parsed.parseItem(
                "2021-01-02".encodeToByteArray(),
                OwnedFormatItem.fromBorrowed(BorrowedFormatItem.Optional(BorrowedFormatItem.Compound(parse("[year]-[month]-[day]").unwrap())))
            ).unwrap()
            assertTrue(remaining.isEmpty())
            assertEquals(2021, parsed.year())
            assertEquals(Month.January, parsed.month())
            assertEquals(2u, parsed.day())
        }
        run {
            val parsed = Parsed.new()
            val remaining = parsed.parseItem(
                "2021-01".encodeToByteArray(),
                BorrowedFormatItem.Optional(BorrowedFormatItem.Compound(parse("[year]-[month]-[day]").unwrap()))
            ).unwrap()
            assertContentEquals("2021-01".encodeToByteArray(), remaining)
            assertNull(parsed.year())
            assertNull(parsed.month())
            assertNull(parsed.day())
        }
        run {
            val parsed = Parsed.new()
            val remaining = parsed.parseItem(
                "2021-01".encodeToByteArray(),
                OwnedFormatItem.fromBorrowed(BorrowedFormatItem.Optional(BorrowedFormatItem.Compound(parse("[year]-[month]-[day]").unwrap())))
            ).unwrap()
            assertContentEquals("2021-01".encodeToByteArray(), remaining)
            assertNull(parsed.year())
            assertNull(parsed.month())
            assertNull(parsed.day())
        }
    }

    @Test
    fun parseFirst() {
        run {
            val parsed = Parsed.new()
            val remaining = parsed.parseItem(
                "2021-01-02".encodeToByteArray(),
                BorrowedFormatItem.First(listOf(BorrowedFormatItem.Compound(parse("[year]-[month]-[day]").unwrap())))
            ).unwrap()
            assertTrue(remaining.isEmpty())
            assertEquals(2021, parsed.year())
            assertEquals(Month.January, parsed.month())
            assertEquals(2u, parsed.day())
        }
        run {
            val parsed = Parsed.new()
            val remaining = parsed.parseItem(
                "2021-01-02".encodeToByteArray(),
                OwnedFormatItem.fromBorrowed(BorrowedFormatItem.First(listOf(BorrowedFormatItem.Compound(parse("[year]-[month]-[day]").unwrap()))))
            ).unwrap()
            assertTrue(remaining.isEmpty())
            assertEquals(2021, parsed.year())
            assertEquals(Month.January, parsed.month())
            assertEquals(2u, parsed.day())
        }
        run {
            val parsed = Parsed.new()
            val remaining = parsed.parseItem("2021-01-02".encodeToByteArray(), BorrowedFormatItem.First(emptyList())).unwrap()
            assertContentEquals("2021-01-02".encodeToByteArray(), remaining)
            assertNull(parsed.year())
            assertNull(parsed.month())
            assertNull(parsed.day())
        }
        run {
            val parsed = Parsed.new()
            val remaining = parsed.parseItem("2021-01-02".encodeToByteArray(), OwnedFormatItem.First(emptyList())).unwrap()
            assertContentEquals("2021-01-02".encodeToByteArray(), remaining)
            assertNull(parsed.year())
            assertNull(parsed.month())
            assertNull(parsed.day())
        }
        run {
            val parsed = Parsed.new()
            val remaining = parsed.parseItem(
                "2021-01-02".encodeToByteArray(),
                BorrowedFormatItem.First(
                    listOf(
                        BorrowedFormatItem.Compound(parse("[period]").unwrap()),
                        BorrowedFormatItem.Compound(parse("x").unwrap()),
                        BorrowedFormatItem.Compound(parse("[year]-[month]-[day]").unwrap()),
                    )
                )
            ).unwrap()
            assertTrue(remaining.isEmpty())
            assertEquals(2021, parsed.year())
            assertEquals(Month.January, parsed.month())
            assertEquals(2u, parsed.day())
        }
        run {
            val parsed = Parsed.new()
            val remaining = parsed.parseItem(
                "2021-01-02".encodeToByteArray(),
                OwnedFormatItem.fromBorrowed(
                    BorrowedFormatItem.First(
                        listOf(
                            BorrowedFormatItem.Compound(parse("[period]").unwrap()),
                            BorrowedFormatItem.Compound(parse("x").unwrap()),
                            BorrowedFormatItem.Compound(parse("[year]-[month]-[day]").unwrap()),
                        )
                    )
                )
            ).unwrap()
            assertTrue(remaining.isEmpty())
            assertEquals(2021, parsed.year())
            assertEquals(Month.January, parsed.month())
            assertEquals(2u, parsed.day())
        }
        run {
            val parsed = Parsed.new()
            val err = parsed.parseItem(
                "2021-01-02".encodeToByteArray(),
                BorrowedFormatItem.First(
                    listOf(
                        BorrowedFormatItem.Compound(parse("[period]").unwrap()),
                        BorrowedFormatItem.Compound(parse("x").unwrap()),
                    )
                )
            ).unwrapErr()
            assertEquals(ParseFromDescription.InvalidComponent("period"), err)
        }
        run {
            val parsed = Parsed.new()
            val err = parsed.parseItem(
                "2021-01-02".encodeToByteArray(),
                OwnedFormatItem.fromBorrowed(
                    BorrowedFormatItem.First(
                        listOf(
                            BorrowedFormatItem.Compound(parse("[period]").unwrap()),
                            BorrowedFormatItem.Compound(parse("x").unwrap()),
                        )
                    )
                )
            ).unwrapErr()
            assertEquals(ParseFromDescription.InvalidComponent("period"), err)
        }
    }

    @Test
    fun parseUnixTimestamp() {
        assertEquals(datetime(2009, 2, 13, 23, 31, 30, 0, 0), OffsetDateTime.parse("1234567890", parse("[unix_timestamp]").unwrap()).unwrap())
        assertEquals(datetime(2009, 2, 13, 23, 31, 30, 123_000_000, 0), OffsetDateTime.parse("1234567890123", parse("[unix_timestamp precision:millisecond]").unwrap()).unwrap())
        assertEquals(datetime(2009, 2, 13, 23, 31, 30, 123_456_000, 0), OffsetDateTime.parse("1234567890123456", parse("[unix_timestamp precision:microsecond]").unwrap()).unwrap())
        assertEquals(datetime(2009, 2, 13, 23, 31, 30, 123_456_789, 0), OffsetDateTime.parse("1234567890123456789", parse("[unix_timestamp precision:nanosecond]").unwrap()).unwrap())
    }

    @Test
    fun parseUnixTimestampErr() {
        assertInvalidComponent(OffsetDateTime.parse("1234567890", parse("[unix_timestamp sign:mandatory]").unwrap()), "unix_timestamp")
        assertInvalidComponent(OffsetDateTime.parse("a", parse("[unix_timestamp precision:second]").unwrap()), "unix_timestamp")
        assertInvalidComponent(OffsetDateTime.parse("a", parse("[unix_timestamp precision:millisecond]").unwrap()), "unix_timestamp")
        assertInvalidComponent(OffsetDateTime.parse("a", parse("[unix_timestamp precision:microsecond]").unwrap()), "unix_timestamp")
        assertInvalidComponent(OffsetDateTime.parse("a", parse("[unix_timestamp precision:nanosecond]").unwrap()), "unix_timestamp")
    }

    @Test
    fun issue601() {
        val date = OffsetDateTime.parse("1234567890.123", parse("[unix_timestamp].[subsecond digits:3]").unwrap())
        assertEquals(TimeResult.Ok(datetime(2009, 2, 13, 23, 31, 30, 123_000_000, 0)), date)
    }

    @Test
    fun end() {
        val parsed = Parsed.new()
        val remaining = parsed.parseItem(ByteArray(0), BorrowedFormatItem.Component(Component.End(End.default()))).unwrap()
        assertTrue(remaining.isEmpty())

        assertEquals(TimeResult.Ok(time(0, 0)), Time.parse("00:00", parse("[hour]:[minute][end]").unwrap()))
        assertEquals(TimeResult.Ok(time(0, 0)), Time.parse("00:00abcdef", parse("[hour]:[minute][end trailing_input:discard]").unwrap()))
        assertEquals(TimeResult.Ok(time(0, 0)), Time.parse("00:00:00", parseOwned("[hour]:[minute][optional [[end]]]:[second]", 2).unwrap()))
        assertUnexpectedTrailing(Time.parse("00:00:00", parseOwned("[hour]:[minute][end]:[second]", 2).unwrap()))
        assertInvalidLiteral(Time.parse("00:00:00", parseOwned("[hour]:[minute][end trailing_input:discard]:[second]", 2).unwrap()))
    }
}
