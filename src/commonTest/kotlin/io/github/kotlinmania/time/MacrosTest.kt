// port-lint: tests tests/integration/macros.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatdescription.Component
import io.github.kotlinmania.time.formatdescription.modifier.Day
import io.github.kotlinmania.time.formatdescription.modifier.End
import io.github.kotlinmania.time.formatdescription.modifier.Ignore
import io.github.kotlinmania.time.formatdescription.modifier.Month
import io.github.kotlinmania.time.formatdescription.modifier.MonthRepr
import io.github.kotlinmania.time.formatdescription.modifier.OffsetMinute
import io.github.kotlinmania.time.formatdescription.modifier.OffsetSecond
import io.github.kotlinmania.time.formatdescription.modifier.Ordinal
import io.github.kotlinmania.time.formatdescription.modifier.Padding
import io.github.kotlinmania.time.formatdescription.modifier.Period
import io.github.kotlinmania.time.formatdescription.modifier.TrailingInput
import io.github.kotlinmania.time.formatdescription.modifier.UnixTimestamp
import io.github.kotlinmania.time.formatdescription.modifier.UnixTimestampPrecision
import io.github.kotlinmania.time.formatdescription.modifier.WeekNumber
import io.github.kotlinmania.time.formatdescription.modifier.WeekNumberRepr
import io.github.kotlinmania.time.formatdescription.modifier.Weekday
import io.github.kotlinmania.time.formatdescription.modifier.WeekdayRepr
import io.github.kotlinmania.time.formatdescription.modifier.Year
import io.github.kotlinmania.time.formatdescription.modifier.YearRepr
import io.github.kotlinmania.time.macros.date
import io.github.kotlinmania.time.macros.formatDescription
import io.github.kotlinmania.time.macros.time
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MacrosTest {
    @Test
    fun nontrivialString() {
        assertTrue(formatDescription("").isEmpty())
    }

    @Test
    fun formatDescriptionVersion() {
        assertEquals(
            listOf(BorrowedFormatItem.Literal("[".encodeToByteArray())),
            formatDescription(version = 1, "[["),
        )
        assertEquals(
            listOf(BorrowedFormatItem.Literal("\\\\".encodeToByteArray())),
            formatDescription(version = 1, "\\\\"),
        )
        assertEquals(
            listOf(BorrowedFormatItem.Literal("\\".encodeToByteArray())),
            formatDescription(version = 2, "\\\\"),
        )
    }

    @Test
    fun nestedV1() {
        assertEquals(
            listOf(BorrowedFormatItem.Optional(BorrowedFormatItem.Literal("[".encodeToByteArray()))),
            formatDescription(version = 1, "[optional [[[]]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.Optional(
                    BorrowedFormatItem.Compound(
                        listOf(
                            BorrowedFormatItem.Literal(" ".encodeToByteArray()),
                            BorrowedFormatItem.Literal("[".encodeToByteArray()),
                            BorrowedFormatItem.Literal(" ".encodeToByteArray()),
                        ),
                    ),
                ),
            ),
            formatDescription(version = 1, "[optional [ [[ ]]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.First(
                    listOf(
                        BorrowedFormatItem.Literal("a".encodeToByteArray()),
                        BorrowedFormatItem.Literal("[".encodeToByteArray()),
                    ),
                ),
            ),
            formatDescription(version = 1, "[first [a][[[]]"),
        )
    }

    @Test
    fun optional() {
        assertEquals(
            listOf(
                BorrowedFormatItem.Optional(
                    BorrowedFormatItem.Compound(
                        listOf(
                            BorrowedFormatItem.Literal(":".encodeToByteArray()),
                            BorrowedFormatItem.Component(Component.Year(Year.default())),
                        ),
                    ),
                ),
            ),
            formatDescription(version = 2, "[optional [:[year]]]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.Optional(
                    BorrowedFormatItem.Component(Component.Year(Year.default())),
                ),
            ),
            formatDescription(version = 2, "[optional [[year]]]"),
        )
        assertEquals(
            listOf(BorrowedFormatItem.Optional(BorrowedFormatItem.Literal("[".encodeToByteArray()))),
            formatDescription(version = 2, "[optional [\\[]]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.Optional(
                    BorrowedFormatItem.Compound(
                        listOf(
                            BorrowedFormatItem.Literal(" ".encodeToByteArray()),
                            BorrowedFormatItem.Literal("[".encodeToByteArray()),
                            BorrowedFormatItem.Literal(" ".encodeToByteArray()),
                        ),
                    ),
                ),
            ),
            formatDescription(version = 2, "[optional [ \\[ ]]"),
        )
    }

    @Test
    fun first() {
        assertEquals(
            listOf(BorrowedFormatItem.First(listOf(BorrowedFormatItem.Literal("a".encodeToByteArray())))),
            formatDescription(version = 2, "[first [a]]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.First(
                    listOf(
                        BorrowedFormatItem.Literal("a".encodeToByteArray()),
                        BorrowedFormatItem.Literal("b".encodeToByteArray()),
                    ),
                ),
            ),
            formatDescription(version = 2, "[first [a] [b]]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.First(
                    listOf(
                        BorrowedFormatItem.Literal("a".encodeToByteArray()),
                        BorrowedFormatItem.Literal("b".encodeToByteArray()),
                    ),
                ),
            ),
            formatDescription(version = 2, "[first [a][b]]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.First(
                    listOf(
                        BorrowedFormatItem.Literal("a".encodeToByteArray()),
                        BorrowedFormatItem.Literal("[".encodeToByteArray()),
                    ),
                ),
            ),
            formatDescription(version = 2, "[first [a][\\[]]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.First(
                    listOf(
                        BorrowedFormatItem.Literal("a".encodeToByteArray()),
                        BorrowedFormatItem.Compound(
                            listOf(
                                BorrowedFormatItem.Literal("[".encodeToByteArray()),
                                BorrowedFormatItem.Literal("[".encodeToByteArray()),
                            ),
                        ),
                    ),
                ),
            ),
            formatDescription(version = 2, "[first [a][\\[\\[]]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.First(
                    listOf(
                        BorrowedFormatItem.Component(
                            Component.Period(
                                Period.default().withIsUppercase(true).withCaseSensitive(true),
                            ),
                        ),
                        BorrowedFormatItem.Component(
                            Component.Period(
                                Period.default().withIsUppercase(false).withCaseSensitive(true),
                            ),
                        ),
                    ),
                ),
            ),
            formatDescription(
                version = 2,
                "[first [[period case:upper]] [[period case:lower]] ]",
            ),
        )
    }

    @Test
    fun backslashEscape() {
        assertEquals(
            listOf(BorrowedFormatItem.Optional(BorrowedFormatItem.Literal("]".encodeToByteArray()))),
            formatDescription(version = 2, "[optional [\\]]]"),
        )
        assertEquals(
            listOf(BorrowedFormatItem.Optional(BorrowedFormatItem.Literal("[".encodeToByteArray()))),
            formatDescription(version = 2, "[optional [\\[]]"),
        )
        assertEquals(
            listOf(BorrowedFormatItem.Optional(BorrowedFormatItem.Literal("\\".encodeToByteArray()))),
            formatDescription(version = 2, "[optional [\\\\]]"),
        )
        assertEquals(
            listOf(BorrowedFormatItem.Literal("\\".encodeToByteArray())),
            formatDescription(version = 2, "\\\\"),
        )
        assertEquals(
            listOf(BorrowedFormatItem.Literal("[".encodeToByteArray())),
            formatDescription(version = 2, "\\["),
        )
        assertEquals(
            listOf(BorrowedFormatItem.Literal("]".encodeToByteArray())),
            formatDescription(version = 2, "\\]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.Literal("foo".encodeToByteArray()),
                BorrowedFormatItem.Literal("\\".encodeToByteArray()),
            ),
            formatDescription(version = 2, "foo\\\\"),
        )
    }

    @Test
    fun formatDescriptionCoverage() {
        assertEquals(
            listOf(
                BorrowedFormatItem.Component(Component.Day(Day.default().withPadding(Padding.Space))),
                BorrowedFormatItem.Component(Component.Day(Day.default().withPadding(Padding.Zero))),
                BorrowedFormatItem.Component(Component.Day(Day.default().withPadding(Padding.None))),
            ),
            formatDescription("[day padding:space][day padding:zero][day padding:none]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.Component(Component.OffsetMinute(OffsetMinute.default().withPadding(Padding.Space))),
                BorrowedFormatItem.Component(Component.OffsetMinute(OffsetMinute.default().withPadding(Padding.Zero))),
                BorrowedFormatItem.Component(Component.OffsetMinute(OffsetMinute.default().withPadding(Padding.None))),
            ),
            formatDescription(
                "[offset_minute padding:space][offset_minute padding:zero][offset_minute padding:none]",
            ),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.Component(Component.OffsetSecond(OffsetSecond.default().withPadding(Padding.Space))),
                BorrowedFormatItem.Component(Component.OffsetSecond(OffsetSecond.default().withPadding(Padding.Zero))),
                BorrowedFormatItem.Component(Component.OffsetSecond(OffsetSecond.default().withPadding(Padding.None))),
            ),
            formatDescription(
                "[offset_second padding:space][offset_second padding:zero][offset_second padding:none]",
            ),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.Component(Component.Ordinal(Ordinal.default().withPadding(Padding.Space))),
                BorrowedFormatItem.Component(Component.Ordinal(Ordinal.default().withPadding(Padding.Zero))),
                BorrowedFormatItem.Component(Component.Ordinal(Ordinal.default().withPadding(Padding.None))),
            ),
            formatDescription("[ordinal padding:space][ordinal padding:zero][ordinal padding:none]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.Component(
                    Component.Month(
                        Month.default().withRepr(MonthRepr.Numerical).withPadding(Padding.Zero),
                    ),
                ),
            ),
            formatDescription("[month repr:numerical]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.Component(
                    Component.WeekNumber(
                        WeekNumber.default().withPadding(Padding.Zero).withRepr(WeekNumberRepr.Iso),
                    ),
                ),
            ),
            formatDescription("[week_number repr:iso ]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.Component(
                    Component.Weekday(
                        Weekday.default().withRepr(WeekdayRepr.Long).withOneIndexed(true),
                    ),
                ),
            ),
            formatDescription("[weekday repr:long one_indexed:true]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.Component(
                    Component.Year(
                        Year
                            .default()
                            .withRepr(YearRepr.Full)
                            .withIsoWeekBased(false)
                            .withPadding(Padding.Zero)
                            .withSignIsMandatory(false),
                    ),
                ),
            ),
            formatDescription("[year repr:full base:calendar]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.Literal("[".encodeToByteArray()),
                BorrowedFormatItem.Literal(" ".encodeToByteArray()),
            ),
            formatDescription("[[ "),
        )
        assertEquals(
            listOf(BorrowedFormatItem.Component(Component.Ignore(Ignore.count(2u)))),
            formatDescription("[ignore count:2]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.Component(
                    Component.UnixTimestamp(
                        UnixTimestamp
                            .default()
                            .withPrecision(UnixTimestampPrecision.Nanosecond)
                            .withSignIsMandatory(true),
                    ),
                ),
            ),
            formatDescription("[unix_timestamp precision:nanosecond sign:mandatory]"),
        )
        assertEquals(
            listOf(BorrowedFormatItem.Component(Component.End(End.default()))),
            formatDescription("[end]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.Component(
                    Component.End(End.default().withTrailingInput(TrailingInput.Prohibit)),
                ),
            ),
            formatDescription("[end trailing_input:prohibit]"),
        )
        assertEquals(
            listOf(
                BorrowedFormatItem.Component(
                    Component.End(End.default().withTrailingInput(TrailingInput.Discard)),
                ),
            ),
            formatDescription("[end trailing_input:discard]"),
        )
    }

    @Test
    fun dateCoverage() {
        assertEquals(Date.fromOrdinalDate(2000, 1).getOrThrow(), date(2000, 1))
        assertEquals(Date.fromOrdinalDate(2018, 365).getOrThrow(), date(2019, 1, io.github.kotlinmania.time.Weekday.Monday))
        assertEquals(Date.fromOrdinalDate(2022, 1).getOrThrow(), date(2021, 52, io.github.kotlinmania.time.Weekday.Saturday))
        assertEquals(Date.fromOrdinalDate(2021, 239).getOrThrow(), date(2021, 34, io.github.kotlinmania.time.Weekday.Friday))
    }

    @Test
    fun timeCoverage() {
        assertEquals(Time.MIDNIGHT, time(0, 0, 0))
        assertEquals(Time.fromHms(12, 0, 0).getOrThrow(), time(12, 0, 0))
    }
}
