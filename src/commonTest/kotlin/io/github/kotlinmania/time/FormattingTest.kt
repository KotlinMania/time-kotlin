// port-lint: tests tests/integration/formatting.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.error.Format
import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatdescription.OwnedFormatItem
import io.github.kotlinmania.time.formatdescription.parse
import io.github.kotlinmania.time.formatdescription.wellknown.Config
import io.github.kotlinmania.time.formatdescription.wellknown.DateKind
import io.github.kotlinmania.time.formatdescription.wellknown.Iso8601
import io.github.kotlinmania.time.formatdescription.wellknown.OffsetPrecision
import io.github.kotlinmania.time.formatdescription.wellknown.Rfc2822
import io.github.kotlinmania.time.formatdescription.wellknown.Rfc3339
import io.github.kotlinmania.time.formatdescription.wellknown.TimePrecision
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FormattingTest {
    @Test
    fun rfc2822() {
        assertEquals(
            "Sat, 02 Jan 2021 03:04:05 +0000",
            datetime(2021, Month.January, 2, 3, 4, 5, 0, 0).format(Rfc2822).getOrThrow(),
        )
        assertEquals(
            "Sat, 02 Jan 2021 03:04:05 +0000",
            utcDatetime(2021, Month.January, 2, 3, 4, 5).format(Rfc2822).getOrThrow(),
        )
        assertEquals(
            "Sat, 02 Jan 2021 03:04:05 +0607",
            datetime(2021, Month.January, 2, 3, 4, 5, 0, 6, 7).format(Rfc2822).getOrThrow(),
        )
        assertEquals(
            "Sat, 02 Jan 2021 03:04:05 -0607",
            datetime(2021, Month.January, 2, 3, 4, 5, 0, -6, -7).format(Rfc2822).getOrThrow(),
        )

        assertEquals(
            Result.failure(Format.InvalidComponent("year")),
            datetime(1885, Month.January, 1, 1, 1, 1, 0, 0).format(Rfc2822),
        )
        assertEquals(
            Result.failure(Format.InvalidComponent("year")),
            utcDatetime(1885, Month.January, 1, 1, 1, 1).format(Rfc2822),
        )
        assertEquals(
            Result.failure(Format.InvalidComponent("offset_second")),
            datetime(2000, Month.January, 1, 0, 0, 0, 0, 0, 0, 1).format(Rfc2822),
        )
    }

    @Test
    fun rfc3339() {
        assertEquals(
            "2021-01-02T03:04:05Z",
            datetime(2021, Month.January, 2, 3, 4, 5, 0, 0).format(Rfc3339).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05.1Z",
            datetime(2021, Month.January, 2, 3, 4, 5, 100_000_000, 0).format(Rfc3339).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05.12Z",
            datetime(2021, Month.January, 2, 3, 4, 5, 120_000_000, 0).format(Rfc3339).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05.123Z",
            datetime(2021, Month.January, 2, 3, 4, 5, 123_000_000, 0).format(Rfc3339).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05.1234Z",
            datetime(2021, Month.January, 2, 3, 4, 5, 123_400_000, 0).format(Rfc3339).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05.12345Z",
            datetime(2021, Month.January, 2, 3, 4, 5, 123_450_000, 0).format(Rfc3339).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05.123456Z",
            datetime(2021, Month.January, 2, 3, 4, 5, 123_456_000, 0).format(Rfc3339).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05.1234567Z",
            datetime(2021, Month.January, 2, 3, 4, 5, 123_456_700, 0).format(Rfc3339).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05.12345678Z",
            datetime(2021, Month.January, 2, 3, 4, 5, 123_456_780, 0).format(Rfc3339).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05.123456789Z",
            datetime(2021, Month.January, 2, 3, 4, 5, 123_456_789, 0).format(Rfc3339).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05.123456789-01:02",
            datetime(2021, Month.January, 2, 3, 4, 5, 123_456_789, -1, -2).format(Rfc3339).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05.123456789+01:02",
            datetime(2021, Month.January, 2, 3, 4, 5, 123_456_789, 1, 2).format(Rfc3339).getOrThrow(),
        )

        assertEquals(
            Result.failure(Format.InvalidComponent("year")),
            datetime(-1, Month.January, 1, 0, 0, 0, 0, 0).format(Rfc3339),
        )
        assertEquals(
            Result.failure(Format.InvalidComponent("offset_second")),
            datetime(0, Month.January, 1, 0, 0, 0, 0, 0, 0, 1).format(Rfc3339),
        )
    }

    @Test
    fun iso8601() {
        val dt = datetime(2021, Month.January, 2, 3, 4, 5, 0, 0)

        assertEquals(
            "-123456-01-02T03:04:05.000000000Z",
            datetime(-123_456, Month.January, 2, 3, 4, 5, 0, 0).format(
                Iso8601(Config.DEFAULT.setYearIsSixDigits(true).encode()),
            ).getOrThrow(),
        )
        assertEquals(
            "-123456-002T03:04:05.000000000Z",
            datetime(-123_456, Month.January, 2, 3, 4, 5, 0, 0).format(
                Iso8601(Config.DEFAULT.setDateKind(DateKind.Ordinal).setYearIsSixDigits(true).encode()),
            ).getOrThrow(),
        )
        assertEquals(
            "-123456-W01-4T03:04:05.000000000Z",
            datetime(-123_456, Month.January, 2, 3, 4, 5, 0, 0).format(
                Iso8601(Config.DEFAULT.setDateKind(DateKind.Week).setYearIsSixDigits(true).encode()),
            ).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05.000000000+01:00",
            datetime(2021, Month.January, 2, 3, 4, 5, 0, 1).format(Iso8601.DEFAULT).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05.000000000+01",
            datetime(2021, Month.January, 2, 3, 4, 5, 0, 1).format(
                Iso8601(Config.DEFAULT.setOffsetPrecision(OffsetPrecision.Hour).encode()),
            ).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05.000000000Z",
            dt.format(Iso8601(Config.DEFAULT.encode())).getOrThrow(),
        )
        assertEquals(
            "20210102T030405.000000000Z",
            dt.format(Iso8601(Config.DEFAULT.setUseSeparators(false).encode())).getOrThrow(),
        )
        assertEquals(
            "+002021-01-02T03:04:05.000000000Z",
            dt.format(Iso8601(Config.DEFAULT.setYearIsSixDigits(true).encode())).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03Z",
            dt.format(Iso8601(Config.DEFAULT.setTimePrecision(TimePrecision.Hour(null)).encode())).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04Z",
            dt.format(Iso8601(Config.DEFAULT.setTimePrecision(TimePrecision.Minute(null)).encode())).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05Z",
            dt.format(Iso8601(Config.DEFAULT.setTimePrecision(TimePrecision.Second(null)).encode())).getOrThrow(),
        )
        assertEquals(
            "2021-002T03:04:05.000000000Z",
            dt.format(Iso8601(Config.DEFAULT.setDateKind(DateKind.Ordinal).encode())).getOrThrow(),
        )
        assertEquals(
            "2020-W53-6T03:04:05.000000000Z",
            dt.format(Iso8601(Config.DEFAULT.setDateKind(DateKind.Week).encode())).getOrThrow(),
        )

        assertEquals(
            Result.failure(Format.InvalidComponent("year")),
            datetime(10_000, Month.January, 1, 0, 0, 0, 0, 0).format(Iso8601.DEFAULT),
        )
        assertEquals(
            Result.failure(Format.InvalidComponent("offset_second")),
            datetime(2021, Month.January, 2, 3, 4, 5, 0, 0, 0, 1).format(Iso8601.DEFAULT),
        )
        assertEquals(
            Result.failure(Format.InvalidComponent("offset_minute")),
            datetime(2021, Month.January, 2, 3, 4, 5, 0, 0, 1).format(
                Iso8601(Config.DEFAULT.setOffsetPrecision(OffsetPrecision.Hour).encode()),
            ),
        )
    }

    @Test
    fun iso8601Issue678() {
        val dt = datetime(2021, Month.January, 2, 3, 4, 5, 999_999_999, 0)
        assertEquals(
            "2021-01-02T03:04:05.999999999Z",
            dt.format(Iso8601(Config.DEFAULT.setTimePrecision(TimePrecision.Second(9u)).encode())).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05.999999Z",
            dt.format(Iso8601(Config.DEFAULT.setTimePrecision(TimePrecision.Second(6u)).encode())).getOrThrow(),
        )
        assertEquals(
            "2021-01-02T03:04:05.999Z",
            dt.format(Iso8601(Config.DEFAULT.setTimePrecision(TimePrecision.Second(3u)).encode())).getOrThrow(),
        )
    }

    @Test
    fun formatTime() {
        val t = time(13, 2, 3, 456_789_012)
        val formatOutput =
            listOf(
                formatDescription("[hour]") to "13",
                formatDescription("[hour repr:12]") to "01",
                formatDescription("[hour repr:12 padding:none]") to "1",
                formatDescription("[hour repr:12 padding:space]") to " 1",
                formatDescription("[hour repr:24]") to "13",
                formatDescription("[hour repr:24 padding:none]") to "13",
                formatDescription("[hour repr:24 padding:space]") to "13",
                formatDescription("[minute]") to "02",
                formatDescription("[minute padding:none]") to "2",
                formatDescription("[minute padding:space]") to " 2",
                formatDescription("[minute padding:zero]") to "02",
                formatDescription("[period]") to "PM",
                formatDescription("[period case:upper]") to "PM",
                formatDescription("[period case:lower]") to "pm",
                formatDescription("[second]") to "03",
                formatDescription("[second padding:none]") to "3",
                formatDescription("[second padding:space]") to " 3",
                formatDescription("[second padding:zero]") to "03",
                formatDescription("[subsecond]") to "456789012",
                formatDescription("[subsecond digits:1]") to "4",
                formatDescription("[subsecond digits:2]") to "45",
                formatDescription("[subsecond digits:3]") to "456",
                formatDescription("[subsecond digits:4]") to "4567",
                formatDescription("[subsecond digits:5]") to "45678",
                formatDescription("[subsecond digits:6]") to "456789",
                formatDescription("[subsecond digits:7]") to "4567890",
                formatDescription("[subsecond digits:8]") to "45678901",
                formatDescription("[subsecond digits:9]") to "456789012",
                formatDescription("[subsecond digits:1+]") to "456789012",
            )

        for ((fd, output) in formatOutput) {
            assertEquals(output, t.format(fd).getOrThrow())
            val sb1 = StringBuilder()
            assertTrue(t.formatInto(sb1, fd).isSuccess)
            assertEquals(output, sb1.toString())

            val owned = OwnedFormatItem.fromBorrowedSlice(fd)
            assertEquals(output, t.format(owned).getOrThrow())
            val sb2 = StringBuilder()
            assertTrue(t.formatInto(sb2, owned).isSuccess)
            assertEquals(output, sb2.toString())
        }

        assertEquals("01AM", time(1, 2, 3).format(formatDescription("[hour repr:12][period]")).getOrThrow())
        assertEquals("12am", Time.MIDNIGHT.format(formatDescription("[hour repr:12][period case:lower]")).getOrThrow())
        assertEquals("0", Time.MIDNIGHT.format(formatDescription("[subsecond digits:1+]")).getOrThrow())
        assertEquals("01", time(0, 0, 0, 10_000_000).format(formatDescription("[subsecond digits:1+]")).getOrThrow())
        assertEquals("001", time(0, 0, 0, 1_000_000).format(formatDescription("[subsecond digits:1+]")).getOrThrow())
        assertEquals("0001", time(0, 0, 0, 100_000).format(formatDescription("[subsecond digits:1+]")).getOrThrow())
        assertEquals("00001", time(0, 0, 0, 10_000).format(formatDescription("[subsecond digits:1+]")).getOrThrow())
        assertEquals("000001", time(0, 0, 0, 1_000).format(formatDescription("[subsecond digits:1+]")).getOrThrow())
        assertEquals("0000001", time(0, 0, 0, 100).format(formatDescription("[subsecond digits:1+]")).getOrThrow())
        assertEquals("00000001", time(0, 0, 0, 10).format(formatDescription("[subsecond digits:1+]")).getOrThrow())
        assertEquals("000000001", time(0, 0, 0, 1).format(formatDescription("[subsecond digits:1+]")).getOrThrow())
    }

    @Test
    fun displayTime() {
        assertEquals("0:00:00.0", time(0, 0).toString())
        assertEquals("23:59:00.0", time(23, 59).toString())
        assertEquals("23:59:59.0", time(23, 59, 59).toString())
        assertEquals("0:00:01.0", time(0, 0, 1).toString())
        assertEquals("0:00:00.1", time(0, 0, 0, 100_000_000).toString())
        assertEquals("0:00:00.01", time(0, 0, 0, 10_000_000).toString())
        assertEquals("0:00:00.001", time(0, 0, 0, 1_000_000).toString())
        assertEquals("0:00:00.0001", time(0, 0, 0, 100_000).toString())
        assertEquals("0:00:00.00001", time(0, 0, 0, 10_000).toString())
        assertEquals("0:00:00.000001", time(0, 0, 0, 1_000).toString())
        assertEquals("0:00:00.0000001", time(0, 0, 0, 100).toString())
        assertEquals("0:00:00.00000001", time(0, 0, 0, 10).toString())
        assertEquals("0:00:00.000000001", time(0, 0, 0, 1).toString())
    }

    @Test
    fun formatDate() {
        val d = date(2019, Month.December, 31)
        val formatOutput =
            listOf(
                formatDescription("[day]") to "31",
                formatDescription("[month]") to "12",
                formatDescription("[month repr:short]") to "Dec",
                formatDescription("[month repr:long]") to "December",
                formatDescription("[ordinal]") to "365",
                formatDescription("[weekday]") to "Tuesday",
                formatDescription("[weekday repr:short]") to "Tue",
                formatDescription("[weekday repr:sunday]") to "3",
                formatDescription("[weekday repr:sunday one_indexed:false]") to "2",
                formatDescription("[weekday repr:monday]") to "2",
                formatDescription("[weekday repr:monday one_indexed:false]") to "1",
                formatDescription("[week_number]") to "01",
                formatDescription("[week_number padding:none]") to "1",
                formatDescription("[week_number padding:space]") to " 1",
                formatDescription("[week_number repr:sunday]") to "52",
                formatDescription("[week_number repr:monday]") to "52",
                formatDescription("[year]") to "2019",
                formatDescription("[year base:iso_week]") to "2020",
                formatDescription("[year sign:mandatory]") to "+2019",
                formatDescription("[year base:iso_week sign:mandatory]") to "+2020",
                formatDescription("[year repr:century]") to "20",
                formatDescription("[year repr:last_two]") to "19",
                formatDescription("[year base:iso_week repr:last_two]") to "20",
                formatDescription("[year range:standard]") to "2019",
                formatDescription("[year range:standard repr:century]") to "20",
                formatDescription("[year range:standard repr:last_two]") to "19",
            )

        for ((fd, output) in formatOutput) {
            assertEquals(output, d.format(fd).getOrThrow())
            val sb1 = StringBuilder()
            assertTrue(d.formatInto(sb1, fd).isSuccess)
            val owned = OwnedFormatItem.fromBorrowedSlice(fd)
            assertEquals(output, d.format(owned).getOrThrow())
            val sb2 = StringBuilder()
            assertTrue(d.formatInto(sb2, owned).isSuccess)
        }
    }

    @Test
    fun formatDateErr() {
        val e1 = date(10_000, Month.January, 1).format(formatDescription("[year range:standard]")).exceptionOrNull()
        assertTrue(e1 is Format.ComponentRange && e1.err.name() == "year")

        val e2 = date(10_000, Month.January, 1).format(formatDescription("[year repr:century range:standard]")).exceptionOrNull()
        assertTrue(e2 is Format.ComponentRange && e2.err.name() == "year")
    }

    @Test
    fun displayDate() {
        assertEquals("2019-01-01", date(2019, Month.January, 1).toString())
        assertEquals("2019-12-31", date(2019, Month.December, 31).toString())
        assertEquals("-4713-11-24", date(-4713, Month.November, 24).toString())
        assertEquals("-0001-01-01", date(-1, Month.January, 1).toString())

        assertEquals("+10000-01-01", date(10_000, Month.January, 1).toString())
        assertEquals("+100000-01-01", date(100_000, Month.January, 1).toString())
        assertEquals("-10000-01-01", date(-10_000, Month.January, 1).toString())
        assertEquals("-100000-01-01", date(-100_000, Month.January, 1).toString())
    }

    @Test
    fun formatOffset() {
        val valueFormatOutput =
            listOf(
                Triple(offset(1, 2, 3), formatDescription("[offset_hour sign:automatic]"), "01"),
                Triple(offset(1, 2, 3), formatDescription("[offset_hour sign:mandatory]"), "+01"),
                Triple(offset(-1, -2, -3), formatDescription("[offset_hour sign:automatic]"), "-01"),
                Triple(offset(-1, -2, -3), formatDescription("[offset_hour sign:mandatory]"), "-01"),
                Triple(offset(1, 2, 3), formatDescription("[offset_minute]"), "02"),
                Triple(offset(1, 2, 3), formatDescription("[offset_second]"), "03"),
            )

        for ((value, fd, output) in valueFormatOutput) {
            assertEquals(output, value.format(fd).getOrThrow())
            assertEquals(output, value.format(OwnedFormatItem.fromBorrowedSlice(fd)).getOrThrow())
        }
    }

    @Test
    fun displayOffset() {
        assertEquals("+00:00:00", UtcOffset.UTC.toString())
        assertEquals("+00:00:01", offset(0, 0, 1).toString())
        assertEquals("-00:00:01", offset(0, 0, -1).toString())
        assertEquals("+01:00:00", offset(1).toString())
        assertEquals("-01:00:00", offset(-1).toString())
        assertEquals("+23:59:00", offset(23, 59).toString())
        assertEquals("-23:59:00", offset(-23, -59).toString())
        assertEquals("+23:59:59", offset(23, 59, 59).toString())
        assertEquals("-23:59:59", offset(-23, -59, -59).toString())
    }

    @Test
    fun formatPdt() {
        val fd = formatDescription("[year]-[month]-[day] [hour]:[minute]:[second].[subsecond]")
        val pdt = datetime(1970, Month.January, 1, 0, 0)
        assertEquals("1970-01-01 00:00:00.0", pdt.format(fd).getOrThrow())
        assertEquals("1970-01-01 00:00:00.0", pdt.format(OwnedFormatItem.fromBorrowedSlice(fd)).getOrThrow())
    }

    @Test
    fun displayPdt() {
        assertEquals("1970-01-01 0:00:00.0", datetime(1970, Month.January, 1, 0, 0).toString())
        assertEquals("1970-01-01 0:00:01.0", datetime(1970, Month.January, 1, 0, 0, 1).toString())
    }

    @Test
    fun formatOdt() {
        val fd =
            parse(
                "[year]-[month]-[day] [hour]:[minute]:[second].[subsecond digits:1+] [offset_hour sign:mandatory]:[offset_minute]:[offset_second]",
            ).getOrThrow()
        val odt = datetime(1970, Month.January, 1, 0, 0, 0, 0, 0)
        assertEquals("1970-01-01 00:00:00.0 +00:00:00", odt.format(fd).getOrThrow())
        assertEquals("1970-01-01 00:00:00.0 +00:00:00", odt.format(OwnedFormatItem.fromBorrowedSlice(fd)).getOrThrow())
    }

    @Test
    fun displayOdt() {
        assertEquals("1970-01-01 0:00:00.0 +00:00:00", datetime(1970, Month.January, 1, 0, 0, 0, 0, 0).toString())
    }

    @Test
    fun formatUdt() {
        val fd = formatDescription("[year]-[month]-[day] [hour]:[minute]:[second].[subsecond]")
        val udt = utcDatetime(1970, Month.January, 1, 0, 0)
        assertEquals("1970-01-01 00:00:00.0", udt.format(fd).getOrThrow())
        assertEquals("1970-01-01 00:00:00.0", udt.format(OwnedFormatItem.fromBorrowedSlice(fd)).getOrThrow())
    }

    @Test
    fun displayUdt() {
        assertEquals("1970-01-01 0:00:00.0 +00", utcDatetime(1970, Month.January, 1, 0, 0).toString())
    }

    @Test
    fun insufficientTypeInformation() {
        assertTrue(Time.MIDNIGHT.format(formatDescription("[year]")).exceptionOrNull() is Format.InsufficientTypeInformation)
        assertTrue(
            Time.MIDNIGHT.format(
                BorrowedFormatItem.First(listOf(BorrowedFormatItem.Compound(formatDescription("[year]")))),
            ).exceptionOrNull() is Format.InsufficientTypeInformation,
        )
    }

    @Test
    fun first() {
        assertEquals("", Time.MIDNIGHT.format(BorrowedFormatItem.First(emptyList())).getOrThrow())
        assertEquals(
            "00",
            Time.MIDNIGHT.format(
                BorrowedFormatItem.First(listOf(BorrowedFormatItem.Compound(formatDescription("[hour]")))),
            ).getOrThrow(),
        )
        assertEquals("", Time.MIDNIGHT.format(OwnedFormatItem.First(emptyList())).getOrThrow())
        assertEquals(
            "00",
            Time.MIDNIGHT.format(
                OwnedFormatItem.from(
                    BorrowedFormatItem.First(listOf(BorrowedFormatItem.Compound(formatDescription("[hour]")))),
                ),
            ).getOrThrow(),
        )
    }

    @Test
    fun ignore() {
        assertEquals("", Time.MIDNIGHT.format(formatDescription("[ignore count:2]")).getOrThrow())
    }

    @Test
    fun end() {
        assertEquals("", Time.MIDNIGHT.format(formatDescription("[end]")).getOrThrow())
    }

    @Test
    fun unixTimestamp() {
        val dt = datetime(2009, Month.February, 13, 23, 31, 30, 123_456_789, 0)
        assertEquals("1234567890", dt.format(formatDescription("[unix_timestamp]")).getOrThrow())
        assertEquals("+1234567890", dt.format(formatDescription("[unix_timestamp sign:mandatory]")).getOrThrow())
        assertEquals("1234567890123", dt.format(formatDescription("[unix_timestamp precision:millisecond]")).getOrThrow())
        assertEquals("1234567890123456", dt.format(formatDescription("[unix_timestamp precision:microsecond]")).getOrThrow())
        assertEquals("1234567890123456789", dt.format(formatDescription("[unix_timestamp precision:nanosecond]")).getOrThrow())
        assertEquals("-1", datetime(1969, Month.December, 31, 23, 59, 59, 0, 0).format(formatDescription("[unix_timestamp]")).getOrThrow())
    }
}
