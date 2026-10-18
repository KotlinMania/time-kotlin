// port-lint: tests tests/integration/parse_format_description.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.error.InvalidFormatDescription
import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatdescription.Component
import io.github.kotlinmania.time.formatdescription.OwnedFormatItem
import io.github.kotlinmania.time.formatdescription.modifier.Day
import io.github.kotlinmania.time.formatdescription.modifier.End
import io.github.kotlinmania.time.formatdescription.modifier.Hour
import io.github.kotlinmania.time.formatdescription.modifier.Ignore
import io.github.kotlinmania.time.formatdescription.modifier.Minute
import io.github.kotlinmania.time.formatdescription.modifier.Month
import io.github.kotlinmania.time.formatdescription.modifier.MonthRepr
import io.github.kotlinmania.time.formatdescription.modifier.OffsetHour
import io.github.kotlinmania.time.formatdescription.modifier.OffsetMinute
import io.github.kotlinmania.time.formatdescription.modifier.OffsetSecond
import io.github.kotlinmania.time.formatdescription.modifier.Ordinal
import io.github.kotlinmania.time.formatdescription.modifier.Padding
import io.github.kotlinmania.time.formatdescription.modifier.Period
import io.github.kotlinmania.time.formatdescription.modifier.Second
import io.github.kotlinmania.time.formatdescription.modifier.Subsecond
import io.github.kotlinmania.time.formatdescription.modifier.SubsecondDigits
import io.github.kotlinmania.time.formatdescription.modifier.TrailingInput
import io.github.kotlinmania.time.formatdescription.modifier.UnixTimestamp
import io.github.kotlinmania.time.formatdescription.modifier.UnixTimestampPrecision
import io.github.kotlinmania.time.formatdescription.modifier.WeekNumber
import io.github.kotlinmania.time.formatdescription.modifier.WeekNumberRepr
import io.github.kotlinmania.time.formatdescription.modifier.Weekday
import io.github.kotlinmania.time.formatdescription.modifier.WeekdayRepr
import io.github.kotlinmania.time.formatdescription.modifier.Year
import io.github.kotlinmania.time.formatdescription.modifier.YearRange
import io.github.kotlinmania.time.formatdescription.modifier.YearRepr
import io.github.kotlinmania.time.formatdescription.parse.parse
import io.github.kotlinmania.time.formatdescription.parse.parseBorrowed
import io.github.kotlinmania.time.formatdescription.parse.parseOwned
import io.github.kotlinmania.time.formatdescription.parse.parseStrftimeBorrowed
import io.github.kotlinmania.time.formatdescription.parse.parseStrftimeOwned

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParseFormatDescriptionTest {
    @Test
    fun empty() {
        assertEquals(emptyList(), parseBorrowed(2, "").getOrThrow())
        assertEquals(OwnedFormatItem.Compound(emptyList()), parseOwned(2, "").getOrThrow())
    }

    @Test
    fun onlyLiteral() {
        val cases =
            listOf(
                "foo bar" to listOf("foo bar"),
                "  leading spaces" to listOf("  leading spaces"),
                "trailing spaces  " to listOf("trailing spaces  "),
                "     " to listOf("     "),
                "[[" to listOf("["),
                "foo[[bar" to listOf("foo", "[", "bar"),
            )
        for ((desc, expected) in cases) {
            assertEquals(
                expected.map { BorrowedFormatItem.Literal(it.encodeToByteArray()) },
                parse(desc).getOrThrow(),
            )
        }
    }

    @Test
    fun simpleComponent() {
        val cases =
            listOf(
                "[day]" to Component.Day(Day.default()),
                "[end]" to Component.End(End.default()),
                "[hour]" to Component.Hour(Hour.default()),
                "[minute]" to Component.Minute(Minute.default()),
                "[month]" to Component.Month(Month.default()),
                "[offset_hour]" to Component.OffsetHour(OffsetHour.default()),
                "[offset_minute]" to Component.OffsetMinute(OffsetMinute.default()),
                "[offset_second]" to Component.OffsetSecond(OffsetSecond.default()),
                "[ordinal]" to Component.Ordinal(Ordinal.default()),
                "[period]" to Component.Period(Period.default()),
                "[second]" to Component.Second(Second.default()),
                "[subsecond]" to Component.Subsecond(Subsecond.default()),
                "[unix_timestamp]" to Component.UnixTimestamp(UnixTimestamp.default()),
                "[weekday]" to Component.Weekday(Weekday.default()),
                "[week_number]" to Component.WeekNumber(WeekNumber.default()),
                "[year]" to Component.Year(Year.default()),
            )
        for ((desc, component) in cases) {
            assertEquals(
                listOf(BorrowedFormatItem.Component(component)),
                parse(desc).getOrThrow(),
            )
        }
    }

    @Test
    fun errors() {
        fun checkErr(desc: String, predicate: (InvalidFormatDescription) -> Boolean) {
            val e1 = parse(desc).exceptionOrNull() as InvalidFormatDescription
            assertTrue(predicate(e1), "Failed borrowed error check for $desc: $e1")
            val e2 = parseOwned(2, desc).exceptionOrNull() as InvalidFormatDescription
            assertTrue(predicate(e2), "Failed owned error check for $desc: $e2")
        }

        checkErr("[ invalid ]") { it is InvalidFormatDescription.InvalidComponentName && it.name == "invalid" && it.index == 2 }
        checkErr("[") { it is InvalidFormatDescription.MissingComponentName && it.index == 0 }
        checkErr("[ ") { it is InvalidFormatDescription.MissingComponentName && it.index == 1 }
        checkErr("[]") { it is InvalidFormatDescription.MissingComponentName && it.index == 0 }
        checkErr("[day sign:mandatory]") { it is InvalidFormatDescription.InvalidModifier && it.value == "sign" && it.index == 5 }
        checkErr("[day sign:]") { it is InvalidFormatDescription.InvalidModifier && it.value.isEmpty() && it.index == 9 }
        checkErr("[day :mandatory]") { it is InvalidFormatDescription.InvalidModifier && it.value.isEmpty() && it.index == 5 }
        checkErr("[day sign:mandatory") { it is InvalidFormatDescription.UnclosedOpeningBracket && it.index == 0 }
        checkErr("[day padding:invalid]") { it is InvalidFormatDescription.InvalidModifier && it.value == "invalid" && it.index == 13 }
        checkErr("[ignore]") { it is InvalidFormatDescription.MissingRequiredModifier && it.name == "count" && it.index == 1 }
        checkErr("[ignore count:70000]") { it is InvalidFormatDescription.InvalidModifier && it.value == "70000" && it.index == 14 }
    }

    @Test
    fun modifiersCoverage() {
        val paddings =
            listOf(
                Padding.Space to "padding:space",
                Padding.Zero to "padding:zero",
                Padding.None to "padding:none",
            )
        for ((pad, str) in paddings) {
            assertEquals(
                listOf(BorrowedFormatItem.Component(Component.Day(Day.default().withPadding(pad)))),
                parse("[day $str]").getOrThrow(),
            )
            assertEquals(
                listOf(BorrowedFormatItem.Component(Component.Minute(Minute.default().withPadding(pad)))),
                parse("[minute $str]").getOrThrow(),
            )
            assertEquals(
                listOf(BorrowedFormatItem.Component(Component.OffsetMinute(OffsetMinute.default().withPadding(pad)))),
                parse("[offset_minute $str]").getOrThrow(),
            )
            assertEquals(
                listOf(BorrowedFormatItem.Component(Component.OffsetSecond(OffsetSecond.default().withPadding(pad)))),
                parse("[offset_second $str]").getOrThrow(),
            )
            assertEquals(
                listOf(BorrowedFormatItem.Component(Component.Ordinal(Ordinal.default().withPadding(pad)))),
                parse("[ordinal $str]").getOrThrow(),
            )
            assertEquals(
                listOf(BorrowedFormatItem.Component(Component.Second(Second.default().withPadding(pad)))),
                parse("[second $str]").getOrThrow(),
            )
        }

        val trailingInputs =
            listOf(
                TrailingInput.Prohibit to "trailing_input:prohibit",
                TrailingInput.Discard to "trailing_input:discard",
            )
        for ((ti, str) in trailingInputs) {
            assertEquals(
                listOf(BorrowedFormatItem.Component(Component.End(End.default().withTrailingInput(ti)))),
                parse("[end $str]").getOrThrow(),
            )
        }

        val subsecondDigits =
            listOf(
                SubsecondDigits.One to "digits:1",
                SubsecondDigits.Two to "digits:2",
                SubsecondDigits.Three to "digits:3",
                SubsecondDigits.Four to "digits:4",
                SubsecondDigits.Five to "digits:5",
                SubsecondDigits.Six to "digits:6",
                SubsecondDigits.Seven to "digits:7",
                SubsecondDigits.Eight to "digits:8",
                SubsecondDigits.Nine to "digits:9",
                SubsecondDigits.OneOrMore to "digits:1+",
            )
        for ((digits, str) in subsecondDigits) {
            assertEquals(
                listOf(BorrowedFormatItem.Component(Component.Subsecond(Subsecond.default().withDigits(digits)))),
                parse("[subsecond $str]").getOrThrow(),
            )
        }

        val ignoreCounts = listOf(1u, 2u, 3u, 10u, 100u, 1000u)
        for (c in ignoreCounts) {
            assertEquals(
                listOf(BorrowedFormatItem.Component(Component.Ignore(Ignore.count(c.toUShort())))),
                parse("[ignore count:$c]").getOrThrow(),
            )
        }

        val precisions =
            listOf(
                UnixTimestampPrecision.Second to "precision:second",
                UnixTimestampPrecision.Millisecond to "precision:millisecond",
                UnixTimestampPrecision.Microsecond to "precision:microsecond",
                UnixTimestampPrecision.Nanosecond to "precision:nanosecond",
            )
        for ((prec, pStr) in precisions) {
            for ((sign, sStr) in listOf(false to "sign:automatic", true to "sign:mandatory")) {
                assertEquals(
                    listOf(
                        BorrowedFormatItem.Component(
                            Component.UnixTimestamp(
                                UnixTimestamp.default().withPrecision(prec).withSignIsMandatory(sign),
                            ),
                        ),
                    ),
                    parse("[unix_timestamp $pStr $sStr]").getOrThrow(),
                )
            }
        }

        for ((range, rStr) in listOf(YearRange.Standard to "range:standard", YearRange.Extended to "range:extended")) {
            assertEquals(
                listOf(BorrowedFormatItem.Component(Component.Year(Year.default().withRange(range)))),
                parse("[year $rStr]").getOrThrow(),
            )
        }
    }

    @Test
    fun backslashEscapeError() {
        for ((desc, expectedIndex) in listOf("\\a" to 1, "\\" to 0)) {
            val eOwned = parseOwned(2, desc).exceptionOrNull() as InvalidFormatDescription.Expected
            assertEquals("valid escape sequence", eOwned.what)
            assertEquals(expectedIndex, eOwned.index)

            val eBorrowed = parseBorrowed(2, desc).exceptionOrNull() as InvalidFormatDescription.Expected
            assertEquals("valid escape sequence", eBorrowed.what)
            assertEquals(expectedIndex, eBorrowed.index)
        }
    }

    @Test
    fun nestedV1Error() {
        val e1 = parseOwned(2, "[optional [[[]]").exceptionOrNull() as InvalidFormatDescription.MissingComponentName
        assertEquals(11, e1.index)

        val e2 = parseOwned(2, "[optional [ [[ ]]").exceptionOrNull() as InvalidFormatDescription.MissingComponentName
        assertEquals(12, e2.index)

        val e3 = parseOwned(2, "[first [a][[[]]").exceptionOrNull() as InvalidFormatDescription.UnclosedOpeningBracket
        assertEquals(0, e3.index)
    }

    @Test
    fun nestedError() {
        val e1 = parse("[optional []]").exceptionOrNull() as InvalidFormatDescription.NotSupported
        assertEquals("optional item", e1.what)
        assertEquals("runtime-parsed format descriptions", e1.context)
        assertEquals(0, e1.index)

        val e2 = parse("[first []]").exceptionOrNull() as InvalidFormatDescription.NotSupported
        assertEquals("'first' item", e2.what)
        assertEquals("runtime-parsed format descriptions", e2.context)
        assertEquals(0, e2.index)

        val e3 = parseOwned(2, "[year [month]]").exceptionOrNull() as InvalidFormatDescription.InvalidModifier
        assertEquals("[", e3.value)
        assertEquals(6, e3.index)

        val e4 = parseOwned(2, "[optional[]]").exceptionOrNull() as InvalidFormatDescription.Expected
        assertEquals("whitespace after `optional`", e4.what)
        assertEquals(8, e4.index)

        val e5 = parseOwned(2, "[first[]]").exceptionOrNull() as InvalidFormatDescription.Expected
        assertEquals("whitespace after `first`", e5.what)
        assertEquals(5, e5.index)

        val e6 = parseOwned(2, "[optional []").exceptionOrNull() as InvalidFormatDescription.UnclosedOpeningBracket
        assertEquals(0, e6.index)

        val e7 = parseOwned(2, "[first []").exceptionOrNull() as InvalidFormatDescription.UnclosedOpeningBracket
        assertEquals(0, e7.index)

        val e8 = parseOwned(2, "[optional [").exceptionOrNull() as InvalidFormatDescription.UnclosedOpeningBracket
        assertEquals(10, e8.index)

        val e9 = parseOwned(2, "[optional [[year").exceptionOrNull() as InvalidFormatDescription.UnclosedOpeningBracket
        assertEquals(11, e9.index)

        val e10 = parseOwned(2, "[optional ").exceptionOrNull() as InvalidFormatDescription.Expected
        assertEquals("opening bracket", e10.what)
        assertEquals(9, e10.index)
    }

    @Test
    fun errorDisplay() {
        val cases =
            listOf(
                "[" to "missing component name at byte index 0",
                "[foo" to "unclosed opening bracket at byte index 0",
                "[foo]" to "invalid component name `foo` at byte index 1",
                "[day bar]" to "invalid modifier `bar` at byte index 5",
                "[]" to "missing component name at byte index 0",
                "[optional []]" to "optional item is not supported in runtime-parsed format descriptions at byte index 0",
                "[ignore]" to "missing required modifier `count` for component at byte index 1",
            )
        for ((desc, expected) in cases) {
            val err = parse(desc).exceptionOrNull() as InvalidFormatDescription
            assertEquals(expected, err.toString())
        }
    }

    @Test
    fun errorDisplayOwned() {
        val err = parseOwned(2, "[optional ").exceptionOrNull() as InvalidFormatDescription
        assertEquals("expected opening bracket at byte index 9", err.toString())
    }

    @Test
    fun rfc3339() {
        assertEquals(
            listOf(
                BorrowedFormatItem.Component(
                    Component.Year(
                        Year.default()
                            .withPadding(Padding.Zero)
                            .withRepr(YearRepr.Full)
                            .withIsoWeekBased(false)
                            .withSignIsMandatory(false),
                    ),
                ),
                BorrowedFormatItem.Literal("-".encodeToByteArray()),
                BorrowedFormatItem.Component(Component.Month(Month.default().withPadding(Padding.Zero))),
                BorrowedFormatItem.Literal("-".encodeToByteArray()),
                BorrowedFormatItem.Component(Component.Day(Day.default().withPadding(Padding.Zero))),
                BorrowedFormatItem.Literal("T".encodeToByteArray()),
                BorrowedFormatItem.Component(
                    Component.Hour(Hour.default().withPadding(Padding.Zero).withIs12HourClock(false)),
                ),
                BorrowedFormatItem.Literal(":".encodeToByteArray()),
                BorrowedFormatItem.Component(Component.Minute(Minute.default().withPadding(Padding.Zero))),
                BorrowedFormatItem.Literal(":".encodeToByteArray()),
                BorrowedFormatItem.Component(Component.Second(Second.default().withPadding(Padding.Zero))),
                BorrowedFormatItem.Literal(".".encodeToByteArray()),
                BorrowedFormatItem.Component(
                    Component.Subsecond(Subsecond.default().withDigits(SubsecondDigits.OneOrMore)),
                ),
                BorrowedFormatItem.Component(
                    Component.OffsetHour(OffsetHour.default().withPadding(Padding.Zero).withSignIsMandatory(true)),
                ),
                BorrowedFormatItem.Literal(":".encodeToByteArray()),
                BorrowedFormatItem.Component(Component.OffsetMinute(OffsetMinute.default().withPadding(Padding.Zero))),
            ),
            parse(
                "[year]-[month repr:numerical]-[day]T[hour]:[minute]:[second].[subsecond][offset_hour sign:mandatory]:[offset_minute]",
            ).getOrThrow(),
        )
    }

    @Test
    fun strftimeEquivalence() {
        val cases =
            listOf(
                "foo" to formatDescription("foo"),
                "%a" to formatDescription("[weekday repr:short]"),
                "%A" to formatDescription("[weekday]"),
                "%b" to formatDescription("[month repr:short]"),
                "%B" to formatDescription("[month repr:long]"),
                "%C" to formatDescription("[year repr:century]"),
                "%d" to formatDescription("[day]"),
                "%e" to formatDescription("[day padding:space]"),
                "%g" to formatDescription("[year repr:last_two base:iso_week]"),
                "%G" to formatDescription("[year base:iso_week]"),
                "%h" to formatDescription("[month repr:short]"),
                "%H" to formatDescription("[hour]"),
                "%I" to formatDescription("[hour repr:12]"),
                "%j" to formatDescription("[ordinal]"),
                "%k" to formatDescription("[hour padding:space]"),
                "%l" to formatDescription("[hour repr:12 padding:space]"),
                "%m" to formatDescription("[month]"),
                "%M" to formatDescription("[minute]"),
                "%n" to formatDescription("\n"),
                "%p" to formatDescription("[period]"),
                "%P" to formatDescription("[period case:lower]"),
                "%s" to formatDescription("[unix_timestamp]"),
                "%S" to formatDescription("[second]"),
                "%t" to formatDescription("\t"),
                "%u" to formatDescription("[weekday repr:monday]"),
                "%U" to formatDescription("[week_number repr:sunday]"),
                "%V" to formatDescription("[week_number]"),
                "%w" to formatDescription("[weekday repr:sunday]"),
                "%W" to formatDescription("[week_number repr:monday]"),
                "%y" to formatDescription("[year repr:last_two]"),
                "%Y" to formatDescription("[year]"),
                "%%" to formatDescription("%"),
            )
        for ((strftime, custom) in cases) {
            val borrowed = parseStrftimeBorrowed(strftime).getOrThrow()
            val owned = parseStrftimeOwned(strftime).getOrThrow()
            assertEquals(custom, borrowed)
            assertEquals(OwnedFormatItem.from(custom), owned)
        }
    }

    @Test
    fun strftimeCompoundEquivalence() {
        val cases =
            listOf(
                "%c" to "[weekday repr:short] [month repr:short] [day padding:space] [hour]:[minute]:[second] [year]",
                "%D" to "[month]/[day]/[year repr:last_two]",
                "%F" to "[year]-[month repr:numerical]-[day]",
                "%r" to "[hour repr:12]:[minute]:[second] [period]",
                "%R" to "[hour]:[minute]",
                "%T" to "[hour]:[minute]:[second]",
                "%x" to "[month]/[day]/[year repr:last_two]",
                "%X" to "[hour]:[minute]:[second]",
                "%z" to "[offset_hour sign:mandatory][offset_minute]",
            )
        for ((strftime, customStr) in cases) {
            val borrowed = parseStrftimeBorrowed(strftime).getOrThrow()
            val owned = parseStrftimeOwned(strftime).getOrThrow()
            val customParsed = parse(customStr).getOrThrow()
            val custom = listOf(BorrowedFormatItem.Compound(customParsed))
            assertEquals(custom, borrowed)
            assertEquals(OwnedFormatItem.from(custom), owned)
        }
    }
}
