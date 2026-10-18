package io.github.kotlinmania.time

import io.github.kotlinmania.serdetest.Token
import io.github.kotlinmania.serdetest.assertDeTokens
import io.github.kotlinmania.serdetest.assertDeTokensError
import io.github.kotlinmania.serdetest.assertSerTokensError
import io.github.kotlinmania.serdetest.assertTokens
import io.github.kotlinmania.serdetest.compact
import io.github.kotlinmania.serdetest.readable
import io.github.kotlinmania.time.macros.date
import io.github.kotlinmania.time.macros.datetime
import io.github.kotlinmania.time.macros.offset
import io.github.kotlinmania.time.macros.time
import kotlin.test.Test

class SerdeTest {
    @Test
    fun time() {
        assertTokens(
            Time.MIDNIGHT.compact(),
            Time.compact(),
            listOf(
                Token.Tuple(4),
                Token.U8(0u),
                Token.U8(0u),
                Token.U8(0u),
                Token.U32(0u),
                Token.TupleEnd,
            )
        )
        assertTokens(
            time(23, 58, 59, 123_456_789).compact(),
            Time.compact(),
            listOf(
                Token.Tuple(4),
                Token.U8(23u),
                Token.U8(58u),
                Token.U8(59u),
                Token.U32(123_456_789u),
                Token.TupleEnd,
            )
        )
        assertTokens(
            Time.MIDNIGHT.readable(),
            Time.readable(),
            listOf(Token.BorrowedStr("00:00:00.0"))
        )
        assertTokens(
            time(23, 58, 59, 123_456_789).readable(),
            Time.readable(),
            listOf(Token.BorrowedStr("23:58:59.123456789"))
        )
    }

    @Test
    fun timeError() {
        assertDeTokensError(
            Time.compact(),
            listOf(
                Token.Tuple(4),
                Token.U8(24u),
                Token.U8(0u),
                Token.U8(0u),
                Token.U32(0u),
                Token.TupleEnd,
            ),
            "invalid hour, expected an in-range value"
        )
        assertDeTokensError(
            Time.readable(),
            listOf(Token.BorrowedStr("24:00:00.0")),
            "the 'hour' component could not be parsed"
        )
        assertDeTokensError(
            Time.readable(),
            listOf(Token.BorrowedStr("23-00:00.0")),
            "a character literal was not valid"
        )
        assertDeTokensError(
            Time.readable(),
            listOf(Token.BorrowedStr("0:00:00.0")),
            "the 'hour' component could not be parsed"
        )
        assertDeTokensError(
            Time.readable(),
            listOf(Token.BorrowedStr("00:00:00.0x")),
            "unexpected trailing characters; the end of input was expected"
        )
        assertDeTokensError(
            Time.readable(),
            listOf(Token.Bool(false)),
            "invalid type: Bool(value=false), expected a `Time`"
        )
        assertDeTokensError(
            Time.compact(),
            listOf(Token.Bool(false)),
            "invalid type: Bool(value=false), expected a `Time`"
        )
    }

    @Test
    fun timePartial() {
        assertDeTokensError(Time.compact(), listOf(Token.Tuple(4), Token.TupleEnd), "expected hour")
        assertDeTokensError(Time.compact(), listOf(Token.Tuple(4), Token.U8(0u), Token.TupleEnd), "expected minute")
        assertDeTokensError(Time.compact(), listOf(Token.Tuple(4), Token.U8(0u), Token.U8(0u), Token.TupleEnd), "expected second")
        assertDeTokensError(Time.compact(), listOf(Token.Tuple(4), Token.U8(0u), Token.U8(0u), Token.U8(0u), Token.TupleEnd), "expected nanosecond")

        assertDeTokensError(Time.readable(), listOf(Token.Tuple(4), Token.TupleEnd), "expected hour")
        assertDeTokensError(Time.readable(), listOf(Token.Tuple(4), Token.U8(0u), Token.TupleEnd), "expected minute")
        assertDeTokensError(Time.readable(), listOf(Token.Tuple(4), Token.U8(0u), Token.U8(0u), Token.TupleEnd), "expected second")
        assertDeTokensError(Time.readable(), listOf(Token.Tuple(4), Token.U8(0u), Token.U8(0u), Token.U8(0u), Token.TupleEnd), "expected nanosecond")
    }

    @Test
    fun date() {
        assertTokens(
            date(-9999, 1).compact(),
            Date.compact(),
            listOf(
                Token.Tuple(2),
                Token.I32(-9999),
                Token.U16(1u),
                Token.TupleEnd,
            )
        )
        assertTokens(
            date(9999, 365).compact(),
            Date.compact(),
            listOf(
                Token.Tuple(2),
                Token.I32(9999),
                Token.U16(365u),
                Token.TupleEnd,
            )
        )
        assertTokens(
            date(-9999, 1).readable(),
            Date.readable(),
            listOf(Token.BorrowedStr("-9999-01-01"))
        )
        assertTokens(
            date(9999, 365).readable(),
            Date.readable(),
            listOf(Token.BorrowedStr("9999-12-31"))
        )
    }

    @Test
    fun dateError() {
        assertDeTokensError(Date.readable(), listOf(Token.Bool(false)), "invalid type: Bool(value=false), expected a `Date`")
        assertDeTokensError(Date.compact(), listOf(Token.Bool(false)), "invalid type: Bool(value=false), expected a `Date`")
    }

    @Test
    fun datePartial() {
        assertDeTokensError(Date.compact(), listOf(Token.Tuple(2), Token.TupleEnd), "expected year")
        assertDeTokensError(Date.compact(), listOf(Token.Tuple(2), Token.I32(9999), Token.TupleEnd), "expected day of year")

        assertDeTokensError(Date.readable(), listOf(Token.Tuple(2), Token.TupleEnd), "expected year")
        assertDeTokensError(Date.readable(), listOf(Token.Tuple(2), Token.I32(9999), Token.TupleEnd), "expected day of year")
    }

    @Test
    fun primitiveDateTime() {
        assertTokens(
            PrimitiveDateTime.new(date(-9999, 1), time(0, 0)).compact(),
            PrimitiveDateTime.compact(),
            listOf(
                Token.Tuple(6),
                Token.I32(-9999),
                Token.U16(1u),
                Token.U8(0u),
                Token.U8(0u),
                Token.U8(0u),
                Token.U32(0u),
                Token.TupleEnd,
            )
        )
        assertTokens(
            PrimitiveDateTime.new(date(9999, 365), time(23, 58, 59, 123_456_789)).compact(),
            PrimitiveDateTime.compact(),
            listOf(
                Token.Tuple(6),
                Token.I32(9999),
                Token.U16(365u),
                Token.U8(23u),
                Token.U8(58u),
                Token.U8(59u),
                Token.U32(123_456_789u),
                Token.TupleEnd,
            )
        )
        assertTokens(
            PrimitiveDateTime.new(date(-9999, 1), time(0, 0)).readable(),
            PrimitiveDateTime.readable(),
            listOf(Token.BorrowedStr("-9999-01-01 00:00:00.0"))
        )
        assertTokens(
            PrimitiveDateTime.new(date(9999, 365), time(23, 58, 59, 123_456_789)).readable(),
            PrimitiveDateTime.readable(),
            listOf(Token.BorrowedStr("9999-12-31 23:58:59.123456789"))
        )
    }

    @Test
    fun primitiveDateTimeError() {
        assertDeTokensError(PrimitiveDateTime.readable(), listOf(Token.Bool(false)), "invalid type: Bool(value=false), expected a `PrimitiveDateTime`")
        assertDeTokensError(PrimitiveDateTime.compact(), listOf(Token.Bool(false)), "invalid type: Bool(value=false), expected a `PrimitiveDateTime`")
        assertDeTokensError(
            PrimitiveDateTime.compact(),
            listOf(Token.Tuple(6), Token.I32(2021), Token.U16(366u), Token.U8(0u), Token.U8(0u), Token.U8(0u), Token.U32(0u), Token.TupleEnd),
            "invalid ordinal, expected an in-range value"
        )
        assertDeTokensError(
            PrimitiveDateTime.compact(),
            listOf(Token.Tuple(6), Token.I32(2021), Token.U16(1u), Token.U8(24u), Token.U8(0u), Token.U8(0u), Token.U32(0u), Token.TupleEnd),
            "invalid hour, expected an in-range value"
        )
    }

    @Test
    fun primitiveDateTimePartial() {
        assertDeTokensError(PrimitiveDateTime.compact(), listOf(Token.Tuple(6), Token.TupleEnd), "expected year")
        assertDeTokensError(PrimitiveDateTime.compact(), listOf(Token.Tuple(6), Token.I32(9999), Token.TupleEnd), "expected day of year")
        assertDeTokensError(PrimitiveDateTime.compact(), listOf(Token.Tuple(6), Token.I32(9999), Token.U16(365u), Token.TupleEnd), "expected hour")
        assertDeTokensError(PrimitiveDateTime.compact(), listOf(Token.Tuple(6), Token.I32(9999), Token.U16(365u), Token.U8(23u), Token.TupleEnd), "expected minute")
        assertDeTokensError(PrimitiveDateTime.compact(), listOf(Token.Tuple(6), Token.I32(9999), Token.U16(365u), Token.U8(23u), Token.U8(58u), Token.TupleEnd), "expected second")
        assertDeTokensError(PrimitiveDateTime.compact(), listOf(Token.Tuple(6), Token.I32(9999), Token.U16(365u), Token.U8(23u), Token.U8(58u), Token.U8(59u), Token.TupleEnd), "expected nanosecond")

        assertDeTokensError(PrimitiveDateTime.readable(), listOf(Token.Tuple(6), Token.TupleEnd), "expected year")
        assertDeTokensError(PrimitiveDateTime.readable(), listOf(Token.Tuple(6), Token.I32(9999), Token.TupleEnd), "expected day of year")
        assertDeTokensError(PrimitiveDateTime.readable(), listOf(Token.Tuple(6), Token.I32(9999), Token.U16(365u), Token.TupleEnd), "expected hour")
        assertDeTokensError(PrimitiveDateTime.readable(), listOf(Token.Tuple(6), Token.I32(9999), Token.U16(365u), Token.U8(23u), Token.TupleEnd), "expected minute")
        assertDeTokensError(PrimitiveDateTime.readable(), listOf(Token.Tuple(6), Token.I32(9999), Token.U16(365u), Token.U8(23u), Token.U8(58u), Token.TupleEnd), "expected second")
        assertDeTokensError(PrimitiveDateTime.readable(), listOf(Token.Tuple(6), Token.I32(9999), Token.U16(365u), Token.U8(23u), Token.U8(58u), Token.U8(59u), Token.TupleEnd), "expected nanosecond")
    }

    @Test
    fun offsetDateTime() {
        assertTokens(
            PrimitiveDateTime.new(date(-9999, 1), time(0, 0)).assumeUtc().toOffset(offset(23, 58, 59)).compact(),
            OffsetDateTime.compact(),
            listOf(
                Token.Tuple(9),
                Token.I32(-9999),
                Token.U16(1u),
                Token.U8(23u),
                Token.U8(58u),
                Token.U8(59u),
                Token.U32(0u),
                Token.I8(23),
                Token.I8(58),
                Token.I8(59),
                Token.TupleEnd,
            )
        )
        assertTokens(
            PrimitiveDateTime.new(date(9999, 365), time(23, 58, 59, 123_456_789)).assumeUtc().toOffset(offset(-23, -58, -59)).compact(),
            OffsetDateTime.compact(),
            listOf(
                Token.Tuple(9),
                Token.I32(9999),
                Token.U16(365u),
                Token.U8(0u),
                Token.U8(0u),
                Token.U8(0u),
                Token.U32(123_456_789u),
                Token.I8(-23),
                Token.I8(-58),
                Token.I8(-59),
                Token.TupleEnd,
            )
        )
        assertTokens(
            PrimitiveDateTime.new(date(-9999, 1), time(0, 0)).assumeUtc().toOffset(offset(23, 58, 59)).readable(),
            OffsetDateTime.readable(),
            listOf(Token.BorrowedStr("-9999-01-01 23:58:59.0 +23:58:59"))
        )
        assertTokens(
            PrimitiveDateTime.new(date(9999, 365), time(23, 58, 59, 123_456_789)).assumeUtc().toOffset(offset(-23, -58, -59)).readable(),
            OffsetDateTime.readable(),
            listOf(Token.BorrowedStr("9999-12-31 00:00:00.123456789 -23:58:59"))
        )
    }

    @Test
    fun offsetDateTimeError() {
        assertDeTokensError(OffsetDateTime.readable(), listOf(Token.Bool(false)), "invalid type: Bool(value=false), expected an `OffsetDateTime`")
        assertDeTokensError(OffsetDateTime.compact(), listOf(Token.Bool(false)), "invalid type: Bool(value=false), expected an `OffsetDateTime`")
        assertDeTokensError(
            OffsetDateTime.compact(),
            listOf(Token.Tuple(9), Token.I32(2021), Token.U16(366u), Token.U8(0u), Token.U8(0u), Token.U8(0u), Token.U32(0u), Token.I8(0), Token.I8(0), Token.I8(0), Token.TupleEnd),
            "invalid ordinal, expected an in-range value"
        )
        assertDeTokensError(
            OffsetDateTime.compact(),
            listOf(Token.Tuple(9), Token.I32(2021), Token.U16(1u), Token.U8(24u), Token.U8(0u), Token.U8(0u), Token.U32(0u), Token.I8(0), Token.I8(0), Token.I8(0), Token.TupleEnd),
            "invalid hour, expected an in-range value"
        )
        assertDeTokensError(
            OffsetDateTime.compact(),
            listOf(Token.Tuple(9), Token.I32(2021), Token.U16(1u), Token.U8(0u), Token.U8(0u), Token.U8(0u), Token.U32(0u), Token.I8(26), Token.I8(0), Token.I8(0), Token.TupleEnd),
            "invalid offset hour, expected an in-range value"
        )
        assertDeTokensError(
            OffsetDateTime.compact(),
            listOf(Token.Tuple(9), Token.I32(2021), Token.U16(365u), Token.U8(23u), Token.U8(59u), Token.U8(60u), Token.U32(0u), Token.I8(0), Token.I8(0), Token.I8(0), Token.TupleEnd),
            "invalid second, expected an in-range value"
        )
        assertDeTokensError(
            OffsetDateTime.readable(),
            listOf(Token.BorrowedStr("2021-12-31 23:59:60.0 +00:00:00")),
            "second was not in range"
        )
    }

    @Test
    fun offsetDateTimePartial() {
        assertDeTokensError(OffsetDateTime.compact(), listOf(Token.Tuple(9), Token.TupleEnd), "expected year")
        assertDeTokensError(OffsetDateTime.compact(), listOf(Token.Tuple(9), Token.I32(9999), Token.TupleEnd), "expected day of year")
        assertDeTokensError(OffsetDateTime.compact(), listOf(Token.Tuple(9), Token.I32(9999), Token.U16(365u), Token.TupleEnd), "expected hour")
        assertDeTokensError(OffsetDateTime.compact(), listOf(Token.Tuple(9), Token.I32(9999), Token.U16(365u), Token.U8(0u), Token.TupleEnd), "expected minute")
        assertDeTokensError(OffsetDateTime.compact(), listOf(Token.Tuple(9), Token.I32(9999), Token.U16(365u), Token.U8(0u), Token.U8(0u), Token.TupleEnd), "expected second")
        assertDeTokensError(OffsetDateTime.compact(), listOf(Token.Tuple(9), Token.I32(9999), Token.U16(365u), Token.U8(0u), Token.U8(0u), Token.U8(0u), Token.TupleEnd), "expected nanosecond")
        assertDeTokensError(OffsetDateTime.compact(), listOf(Token.Tuple(9), Token.I32(9999), Token.U16(365u), Token.U8(0u), Token.U8(0u), Token.U8(0u), Token.U32(123_456_789u), Token.TupleEnd), "expected offset hours")
        assertDeTokensError(OffsetDateTime.compact(), listOf(Token.Tuple(9), Token.I32(9999), Token.U16(365u), Token.U8(0u), Token.U8(0u), Token.U8(0u), Token.U32(123_456_789u), Token.I8(-23), Token.TupleEnd), "expected offset minutes")
        assertDeTokensError(OffsetDateTime.compact(), listOf(Token.Tuple(9), Token.I32(9999), Token.U16(365u), Token.U8(0u), Token.U8(0u), Token.U8(0u), Token.U32(123_456_789u), Token.I8(-23), Token.I8(-58), Token.TupleEnd), "expected offset seconds")

        assertDeTokensError(OffsetDateTime.readable(), listOf(Token.Tuple(9), Token.TupleEnd), "expected year")
        assertDeTokensError(OffsetDateTime.readable(), listOf(Token.Tuple(9), Token.I32(9999), Token.TupleEnd), "expected day of year")
        assertDeTokensError(OffsetDateTime.readable(), listOf(Token.Tuple(9), Token.I32(9999), Token.U16(365u), Token.TupleEnd), "expected hour")
        assertDeTokensError(OffsetDateTime.readable(), listOf(Token.Tuple(9), Token.I32(9999), Token.U16(365u), Token.U8(0u), Token.TupleEnd), "expected minute")
        assertDeTokensError(OffsetDateTime.readable(), listOf(Token.Tuple(9), Token.I32(9999), Token.U16(365u), Token.U8(0u), Token.U8(0u), Token.TupleEnd), "expected second")
        assertDeTokensError(OffsetDateTime.readable(), listOf(Token.Tuple(9), Token.I32(9999), Token.U16(365u), Token.U8(0u), Token.U8(0u), Token.U8(0u), Token.TupleEnd), "expected nanosecond")
        assertDeTokensError(OffsetDateTime.readable(), listOf(Token.Tuple(9), Token.I32(9999), Token.U16(365u), Token.U8(0u), Token.U8(0u), Token.U8(0u), Token.U32(123_456_789u), Token.TupleEnd), "expected offset hours")
        assertDeTokensError(OffsetDateTime.readable(), listOf(Token.Tuple(9), Token.I32(9999), Token.U16(365u), Token.U8(0u), Token.U8(0u), Token.U8(0u), Token.U32(123_456_789u), Token.I8(-23), Token.TupleEnd), "expected offset minutes")
        assertDeTokensError(OffsetDateTime.readable(), listOf(Token.Tuple(9), Token.I32(9999), Token.U16(365u), Token.U8(0u), Token.U8(0u), Token.U8(0u), Token.U32(123_456_789u), Token.I8(-23), Token.I8(-58), Token.TupleEnd), "expected offset seconds")
    }

    @Test
    fun utcOffset() {
        assertTokens(
            offset(-23, -58, -59).compact(),
            UtcOffset.compact(),
            listOf(Token.Tuple(3), Token.I8(-23), Token.I8(-58), Token.I8(-59), Token.TupleEnd)
        )
        assertTokens(
            offset(23, 58, 59).compact(),
            UtcOffset.compact(),
            listOf(Token.Tuple(3), Token.I8(23), Token.I8(58), Token.I8(59), Token.TupleEnd)
        )
        assertTokens(
            offset(-23, -58, -59).readable(),
            UtcOffset.readable(),
            listOf(Token.BorrowedStr("-23:58:59"))
        )
        assertTokens(
            offset(23, 58, 59).readable(),
            UtcOffset.readable(),
            listOf(Token.BorrowedStr("+23:58:59"))
        )
    }

    @Test
    fun utcOffsetError() {
        assertDeTokensError(UtcOffset.readable(), listOf(Token.Bool(false)), "invalid type: Bool(value=false), expected a `UtcOffset`")
        assertDeTokensError(UtcOffset.compact(), listOf(Token.Bool(false)), "invalid type: Bool(value=false), expected a `UtcOffset`")
        assertDeTokensError(
            UtcOffset.compact(),
            listOf(Token.Tuple(3), Token.I8(26), Token.I8(0), Token.I8(0), Token.TupleEnd),
            "invalid offset hour, expected an in-range value"
        )
    }

    @Test
    fun utcOffsetPartial() {
        assertDeTokensError(UtcOffset.compact(), listOf(Token.Tuple(0), Token.TupleEnd), "expected offset hours")
        assertDeTokensError(UtcOffset.readable(), listOf(Token.Tuple(0), Token.TupleEnd), "expected offset hours")

        val value1 = offset(23)
        assertDeTokens(value1, UtcOffset.compact(), listOf(Token.Tuple(1), Token.I8(23), Token.TupleEnd))
        assertDeTokens(value1, UtcOffset.readable(), listOf(Token.Tuple(1), Token.I8(23), Token.TupleEnd))

        val value2 = offset(23, 58)
        assertDeTokens(value2, UtcOffset.compact(), listOf(Token.Tuple(2), Token.I8(23), Token.I8(58), Token.TupleEnd))
        assertDeTokens(value2, UtcOffset.readable(), listOf(Token.Tuple(2), Token.I8(23), Token.I8(58), Token.TupleEnd))
    }

    @Test
    fun duration() {
        assertTokens(
            Duration.MIN.compact(),
            Duration.compact(),
            listOf(Token.Tuple(2), Token.I64(Long.MIN_VALUE), Token.I32(-999_999_999), Token.TupleEnd)
        )
        assertTokens(
            Duration.MAX.compact(),
            Duration.compact(),
            listOf(Token.Tuple(2), Token.I64(Long.MAX_VALUE), Token.I32(999_999_999), Token.TupleEnd)
        )
        assertTokens(Duration.MIN.readable(), Duration.readable(), listOf(Token.BorrowedStr("-9223372036854775808.999999999")))
        assertTokens(Duration.MAX.readable(), Duration.readable(), listOf(Token.BorrowedStr("9223372036854775807.999999999")))
        assertTokens(Duration.ZERO.readable(), Duration.readable(), listOf(Token.BorrowedStr("0.000000000")))
        assertTokens(Duration.nanoseconds(123).readable(), Duration.readable(), listOf(Token.BorrowedStr("0.000000123")))
        assertTokens(Duration.nanoseconds(-123).readable(), Duration.readable(), listOf(Token.BorrowedStr("-0.000000123")))
    }

    @Test
    fun durationError() {
        assertDeTokensError(Duration.readable(), listOf(Token.BorrowedStr("x")), "invalid value: Str(value=x), expected a decimal point")
        assertDeTokensError(Duration.readable(), listOf(Token.BorrowedStr("x.0")), "invalid value: Str(value=x), expected seconds")
        assertDeTokensError(Duration.readable(), listOf(Token.BorrowedStr("0.x")), "invalid value: Str(value=x), expected nanoseconds")
        assertDeTokensError(Duration.readable(), listOf(Token.Bool(false)), "invalid type: Bool(value=false), expected a `Duration`")
        assertDeTokensError(Duration.compact(), listOf(Token.Bool(false)), "invalid type: Bool(value=false), expected a `Duration`")
    }

    @Test
    fun durationPartial() {
        assertDeTokensError(Duration.compact(), listOf(Token.Tuple(2), Token.TupleEnd), "expected seconds")
        assertDeTokensError(Duration.compact(), listOf(Token.Tuple(2), Token.I64(Long.MAX_VALUE), Token.TupleEnd), "expected nanoseconds")

        assertDeTokensError(Duration.readable(), listOf(Token.Tuple(2), Token.TupleEnd), "expected seconds")
        assertDeTokensError(Duration.readable(), listOf(Token.Tuple(2), Token.I64(Long.MAX_VALUE), Token.TupleEnd), "expected nanoseconds")
    }

    @Test
    fun weekday() {
        assertTokens(Weekday.Monday.compact(), Weekday.compact(), listOf(Token.U8(1u)))
        assertTokens(Weekday.Tuesday.compact(), Weekday.compact(), listOf(Token.U8(2u)))
        assertTokens(Weekday.Wednesday.compact(), Weekday.compact(), listOf(Token.U8(3u)))
        assertTokens(Weekday.Thursday.compact(), Weekday.compact(), listOf(Token.U8(4u)))
        assertTokens(Weekday.Friday.compact(), Weekday.compact(), listOf(Token.U8(5u)))
        assertTokens(Weekday.Saturday.compact(), Weekday.compact(), listOf(Token.U8(6u)))
        assertTokens(Weekday.Sunday.compact(), Weekday.compact(), listOf(Token.U8(7u)))

        assertTokens(Weekday.Monday.readable(), Weekday.readable(), listOf(Token.BorrowedStr("Monday")))
        assertTokens(Weekday.Tuesday.readable(), Weekday.readable(), listOf(Token.BorrowedStr("Tuesday")))
        assertTokens(Weekday.Wednesday.readable(), Weekday.readable(), listOf(Token.BorrowedStr("Wednesday")))
        assertTokens(Weekday.Thursday.readable(), Weekday.readable(), listOf(Token.BorrowedStr("Thursday")))
        assertTokens(Weekday.Friday.readable(), Weekday.readable(), listOf(Token.BorrowedStr("Friday")))
        assertTokens(Weekday.Saturday.readable(), Weekday.readable(), listOf(Token.BorrowedStr("Saturday")))
        assertTokens(Weekday.Sunday.readable(), Weekday.readable(), listOf(Token.BorrowedStr("Sunday")))
    }

    @Test
    fun weekdayError() {
        assertDeTokensError(Weekday.compact(), listOf(Token.U8(0u)), "invalid value: Unsigned(value=0), expected a value in the range 1..=7")
        assertDeTokensError(Weekday.readable(), listOf(Token.BorrowedStr("NotADay")), "invalid value: Str(value=NotADay), expected a `Weekday`")
        assertDeTokensError(Weekday.readable(), listOf(Token.Bool(false)), "invalid type: Bool(value=false), expected a `Weekday`")
        assertDeTokensError(Weekday.compact(), listOf(Token.Bool(false)), "invalid type: Bool(value=false), expected a `Weekday`")
    }

    @Test
    fun month() {
        assertTokens(Month.January.compact(), Month.compact(), listOf(Token.U8(1u)))
        assertTokens(Month.February.compact(), Month.compact(), listOf(Token.U8(2u)))
        assertTokens(Month.March.compact(), Month.compact(), listOf(Token.U8(3u)))
        assertTokens(Month.April.compact(), Month.compact(), listOf(Token.U8(4u)))
        assertTokens(Month.May.compact(), Month.compact(), listOf(Token.U8(5u)))
        assertTokens(Month.June.compact(), Month.compact(), listOf(Token.U8(6u)))
        assertTokens(Month.July.compact(), Month.compact(), listOf(Token.U8(7u)))
        assertTokens(Month.August.compact(), Month.compact(), listOf(Token.U8(8u)))
        assertTokens(Month.September.compact(), Month.compact(), listOf(Token.U8(9u)))
        assertTokens(Month.October.compact(), Month.compact(), listOf(Token.U8(10u)))
        assertTokens(Month.November.compact(), Month.compact(), listOf(Token.U8(11u)))
        assertTokens(Month.December.compact(), Month.compact(), listOf(Token.U8(12u)))

        assertTokens(Month.January.readable(), Month.readable(), listOf(Token.BorrowedStr("January")))
        assertTokens(Month.February.readable(), Month.readable(), listOf(Token.BorrowedStr("February")))
        assertTokens(Month.March.readable(), Month.readable(), listOf(Token.BorrowedStr("March")))
        assertTokens(Month.April.readable(), Month.readable(), listOf(Token.BorrowedStr("April")))
        assertTokens(Month.May.readable(), Month.readable(), listOf(Token.BorrowedStr("May")))
        assertTokens(Month.June.readable(), Month.readable(), listOf(Token.BorrowedStr("June")))
        assertTokens(Month.July.readable(), Month.readable(), listOf(Token.BorrowedStr("July")))
        assertTokens(Month.August.readable(), Month.readable(), listOf(Token.BorrowedStr("August")))
        assertTokens(Month.September.readable(), Month.readable(), listOf(Token.BorrowedStr("September")))
        assertTokens(Month.October.readable(), Month.readable(), listOf(Token.BorrowedStr("October")))
        assertTokens(Month.November.readable(), Month.readable(), listOf(Token.BorrowedStr("November")))
        assertTokens(Month.December.readable(), Month.readable(), listOf(Token.BorrowedStr("December")))
    }

    @Test
    fun monthError() {
        assertDeTokensError(Month.compact(), listOf(Token.U8(0u)), "invalid value: Unsigned(value=0), expected a value in the range 1..=12")
        assertDeTokensError(Month.readable(), listOf(Token.BorrowedStr("NotAMonth")), "invalid value: Str(value=NotAMonth), expected a `Month`")
        assertDeTokensError(Month.readable(), listOf(Token.Bool(false)), "invalid type: Bool(value=false), expected a `Month`")
        assertDeTokensError(Month.compact(), listOf(Token.Bool(false)), "invalid type: Bool(value=false), expected a `Month`")
    }
}
