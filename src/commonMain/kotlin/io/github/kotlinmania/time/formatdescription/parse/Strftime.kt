// port-lint: source format_description/parse/strftime.rs
package io.github.kotlinmania.time.formatdescription.parse

import io.github.kotlinmania.time.error.InvalidFormatDescription
import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatdescription.Component
import io.github.kotlinmania.time.formatdescription.OwnedFormatItem
import io.github.kotlinmania.time.formatdescription.modifier.Day
import io.github.kotlinmania.time.formatdescription.modifier.Hour
import io.github.kotlinmania.time.formatdescription.modifier.Minute
import io.github.kotlinmania.time.formatdescription.modifier.Month
import io.github.kotlinmania.time.formatdescription.modifier.MonthRepr
import io.github.kotlinmania.time.formatdescription.modifier.OffsetHour
import io.github.kotlinmania.time.formatdescription.modifier.OffsetMinute
import io.github.kotlinmania.time.formatdescription.modifier.Ordinal
import io.github.kotlinmania.time.formatdescription.modifier.Padding
import io.github.kotlinmania.time.formatdescription.modifier.Period
import io.github.kotlinmania.time.formatdescription.modifier.Second
import io.github.kotlinmania.time.formatdescription.modifier.UnixTimestamp
import io.github.kotlinmania.time.formatdescription.modifier.UnixTimestampPrecision
import io.github.kotlinmania.time.formatdescription.modifier.WeekNumber
import io.github.kotlinmania.time.formatdescription.modifier.WeekNumberRepr
import io.github.kotlinmania.time.formatdescription.modifier.Weekday
import io.github.kotlinmania.time.formatdescription.modifier.WeekdayRepr
import io.github.kotlinmania.time.formatdescription.modifier.Year
import io.github.kotlinmania.time.formatdescription.modifier.YearRange
import io.github.kotlinmania.time.formatdescription.modifier.YearRepr

/**
 * Parse a sequence of items from the `strftime` format description.
 */
fun parseStrftimeBorrowed(s: String): Result<List<BorrowedFormatItem>> {
    val tokens = lexStrftime(s.encodeToByteArray())
    val items = mutableListOf<BorrowedFormatItem>()
    for (tokenRes in tokens) {
        val token = tokenRes.getOrElse { return Result.failure(it) }
        val item = when (token) {
            is StrftimeToken.Literal -> BorrowedFormatItem.Literal(token.value.value)
            is StrftimeToken.ComponentToken ->
                parseStrftimeComponent(token.padding, token.component)
                    .getOrElse { return Result.failure(it) }
        }
        items.add(item)
    }
    return Result.success(items)
}

/**
 * Parse a sequence of items from the `strftime` format description into an [OwnedFormatItem].
 */
fun parseStrftimeOwned(s: String): Result<OwnedFormatItem> =
    parseStrftimeBorrowed(s).map { OwnedFormatItem.from(it) }

private enum class StrftimePadding {
    Default,
    Spaces,
    None,
    Zeroes,
}

private sealed class StrftimeToken {
    class Literal(val value: Spanned<ByteArray>) : StrftimeToken()

    class ComponentToken(
        val padding: Spanned<StrftimePadding>,
        val component: Spanned<Byte>,
    ) : StrftimeToken()
}

private fun lexStrftime(input: ByteArray): List<Result<StrftimeToken>> {
    val result = mutableListOf<Result<StrftimeToken>>()
    var pos = 0

    while (pos < input.size) {
        val b = input[pos]
        val startLoc = Location(pos)
        pos++

        if (b == '%'.code.toByte()) {
            if (pos >= input.size) {
                result.add(
                    Result.failure(
                        InvalidFormatDescription.Expected(
                            what = "valid escape sequence",
                            index = startLoc.byte,
                        ),
                    ),
                )
                break
            }
            val next = input[pos]
            val nextLoc = Location(pos)
            pos++
            if (
                next == '_'.code.toByte() ||
                next == '-'.code.toByte() ||
                next == '0'.code.toByte()
            ) {
                val pad = when (next) {
                    '_'.code.toByte() -> StrftimePadding.Spaces
                    '-'.code.toByte() -> StrftimePadding.None
                    else -> StrftimePadding.Zeroes
                }
                if (pos >= input.size) {
                    break
                }
                val comp = input[pos]
                val compLoc = Location(pos)
                pos++
                result.add(
                    Result.success(
                        StrftimeToken.ComponentToken(
                            padding = pad.spanned(nextLoc.toSelf()),
                            component = comp.spanned(compLoc.toSelf()),
                        ),
                    ),
                )
            } else {
                val span = nextLoc.toSelf()
                result.add(
                    Result.success(
                        StrftimeToken.ComponentToken(
                            padding = StrftimePadding.Default.spanned(span),
                            component = next.spanned(span),
                        ),
                    ),
                )
            }
        } else {
            val sliceStart = startLoc.byte
            var endLoc = startLoc
            while (pos < input.size && input[pos] != '%'.code.toByte()) {
                endLoc = Location(pos)
                pos++
            }
            val slice = input.copyOfRange(sliceStart, pos)
            result.add(Result.success(StrftimeToken.Literal(slice.spanned(startLoc.to(endLoc)))))
        }
    }

    return result
}

private fun parseStrftimeComponent(
    padding: Spanned<StrftimePadding>,
    component: Spanned<Byte>,
): Result<BorrowedFormatItem> {
    fun paddingOrDefault(pad: StrftimePadding, default: Padding): Padding =
        when (pad) {
            StrftimePadding.Default -> default
            StrftimePadding.Spaces -> Padding.Space
            StrftimePadding.None -> Padding.None
            StrftimePadding.Zeroes -> Padding.Zero
        }

    fun lit(s: String): BorrowedFormatItem = BorrowedFormatItem.Literal(s.encodeToByteArray())

    val ch = component.value.toUByte().toInt().toChar()
    val item: BorrowedFormatItem = when (ch) {
        '%' -> lit("%")
        'a' -> BorrowedFormatItem.Component(
            Component.Weekday(
                Weekday(
                    repr = WeekdayRepr.Short,
                    oneIndexed = true,
                    caseSensitive = true,
                ),
            ),
        )
        'A' -> BorrowedFormatItem.Component(
            Component.Weekday(
                Weekday(
                    repr = WeekdayRepr.Long,
                    oneIndexed = true,
                    caseSensitive = true,
                ),
            ),
        )
        'b', 'h' -> BorrowedFormatItem.Component(
            Component.Month(
                Month(
                    repr = MonthRepr.Short,
                    padding = Padding.Zero,
                    caseSensitive = true,
                ),
            ),
        )
        'B' -> BorrowedFormatItem.Component(
            Component.Month(
                Month(
                    repr = MonthRepr.Long,
                    padding = Padding.Zero,
                    caseSensitive = true,
                ),
            ),
        )
        'c' -> BorrowedFormatItem.Compound(
            listOf(
                BorrowedFormatItem.Component(
                    Component.Weekday(
                        Weekday(
                            repr = WeekdayRepr.Short,
                            oneIndexed = true,
                            caseSensitive = true,
                        ),
                    ),
                ),
                lit(" "),
                BorrowedFormatItem.Component(
                    Component.Month(
                        Month(
                            repr = MonthRepr.Short,
                            padding = Padding.Zero,
                            caseSensitive = true,
                        ),
                    ),
                ),
                lit(" "),
                BorrowedFormatItem.Component(Component.Day(Day(padding = Padding.Space))),
                lit(" "),
                BorrowedFormatItem.Component(
                    Component.Hour(Hour(padding = Padding.Zero, is12HourClock = false)),
                ),
                lit(":"),
                BorrowedFormatItem.Component(Component.Minute(Minute(padding = Padding.Zero))),
                lit(":"),
                BorrowedFormatItem.Component(Component.Second(Second(padding = Padding.Zero))),
                lit(" "),
                BorrowedFormatItem.Component(
                    Component.Year(
                        Year(
                            padding = Padding.Zero,
                            repr = YearRepr.Full,
                            range = YearRange.Extended,
                            isoWeekBased = false,
                            signIsMandatory = false,
                        ),
                    ),
                ),
            ),
        )
        'C' -> BorrowedFormatItem.Component(
            Component.Year(
                Year(
                    padding = paddingOrDefault(padding.value, Padding.Zero),
                    repr = YearRepr.Century,
                    range = YearRange.Extended,
                    isoWeekBased = false,
                    signIsMandatory = false,
                ),
            ),
        )
        'd' -> BorrowedFormatItem.Component(
            Component.Day(Day(padding = paddingOrDefault(padding.value, Padding.Zero))),
        )
        'D' -> BorrowedFormatItem.Compound(
            listOf(
                BorrowedFormatItem.Component(
                    Component.Month(
                        Month(
                            repr = MonthRepr.Numerical,
                            padding = Padding.Zero,
                            caseSensitive = true,
                        ),
                    ),
                ),
                lit("/"),
                BorrowedFormatItem.Component(Component.Day(Day(padding = Padding.Zero))),
                lit("/"),
                BorrowedFormatItem.Component(
                    Component.Year(
                        Year(
                            padding = Padding.Zero,
                            repr = YearRepr.LastTwo,
                            range = YearRange.Extended,
                            isoWeekBased = false,
                            signIsMandatory = false,
                        ),
                    ),
                ),
            ),
        )
        'e' -> BorrowedFormatItem.Component(
            Component.Day(Day(padding = paddingOrDefault(padding.value, Padding.Space))),
        )
        'F' -> BorrowedFormatItem.Compound(
            listOf(
                BorrowedFormatItem.Component(
                    Component.Year(
                        Year(
                            padding = Padding.Zero,
                            repr = YearRepr.Full,
                            range = YearRange.Extended,
                            isoWeekBased = false,
                            signIsMandatory = false,
                        ),
                    ),
                ),
                lit("-"),
                BorrowedFormatItem.Component(
                    Component.Month(
                        Month(
                            padding = Padding.Zero,
                            repr = MonthRepr.Numerical,
                            caseSensitive = true,
                        ),
                    ),
                ),
                lit("-"),
                BorrowedFormatItem.Component(Component.Day(Day(padding = Padding.Zero))),
            ),
        )
        'g' -> BorrowedFormatItem.Component(
            Component.Year(
                Year(
                    padding = paddingOrDefault(padding.value, Padding.Zero),
                    repr = YearRepr.LastTwo,
                    range = YearRange.Extended,
                    isoWeekBased = true,
                    signIsMandatory = false,
                ),
            ),
        )
        'G' -> BorrowedFormatItem.Component(
            Component.Year(
                Year(
                    padding = Padding.Zero,
                    repr = YearRepr.Full,
                    range = YearRange.Extended,
                    isoWeekBased = true,
                    signIsMandatory = false,
                ),
            ),
        )
        'H' -> BorrowedFormatItem.Component(
            Component.Hour(
                Hour(
                    padding = paddingOrDefault(padding.value, Padding.Zero),
                    is12HourClock = false,
                ),
            ),
        )
        'I' -> BorrowedFormatItem.Component(
            Component.Hour(
                Hour(
                    padding = paddingOrDefault(padding.value, Padding.Zero),
                    is12HourClock = true,
                ),
            ),
        )
        'j' -> BorrowedFormatItem.Component(
            Component.Ordinal(Ordinal(padding = paddingOrDefault(padding.value, Padding.Zero))),
        )
        'k' -> BorrowedFormatItem.Component(
            Component.Hour(
                Hour(
                    padding = paddingOrDefault(padding.value, Padding.Space),
                    is12HourClock = false,
                ),
            ),
        )
        'l' -> BorrowedFormatItem.Component(
            Component.Hour(
                Hour(
                    padding = paddingOrDefault(padding.value, Padding.Space),
                    is12HourClock = true,
                ),
            ),
        )
        'm' -> BorrowedFormatItem.Component(
            Component.Month(
                Month(
                    padding = paddingOrDefault(padding.value, Padding.Zero),
                    repr = MonthRepr.Numerical,
                    caseSensitive = true,
                ),
            ),
        )
        'M' -> BorrowedFormatItem.Component(
            Component.Minute(Minute(padding = paddingOrDefault(padding.value, Padding.Zero))),
        )
        'n' -> lit("\n")
        'O' -> return Result.failure(
            InvalidFormatDescription.NotSupported(
                what = "modifier",
                context = "",
                index = component.span.start.byte,
            ),
        )
        'p' -> BorrowedFormatItem.Component(
            Component.Period(Period(isUppercase = true, caseSensitive = true)),
        )
        'P' -> BorrowedFormatItem.Component(
            Component.Period(Period(isUppercase = false, caseSensitive = true)),
        )
        'r' -> BorrowedFormatItem.Compound(
            listOf(
                BorrowedFormatItem.Component(
                    Component.Hour(Hour(padding = Padding.Zero, is12HourClock = true)),
                ),
                lit(":"),
                BorrowedFormatItem.Component(Component.Minute(Minute(padding = Padding.Zero))),
                lit(":"),
                BorrowedFormatItem.Component(Component.Second(Second(padding = Padding.Zero))),
                lit(" "),
                BorrowedFormatItem.Component(
                    Component.Period(Period(isUppercase = true, caseSensitive = true)),
                ),
            ),
        )
        'R' -> BorrowedFormatItem.Compound(
            listOf(
                BorrowedFormatItem.Component(
                    Component.Hour(Hour(padding = Padding.Zero, is12HourClock = false)),
                ),
                lit(":"),
                BorrowedFormatItem.Component(Component.Minute(Minute(padding = Padding.Zero))),
            ),
        )
        's' -> BorrowedFormatItem.Component(
            Component.UnixTimestamp(
                UnixTimestamp(
                    precision = UnixTimestampPrecision.Second,
                    signIsMandatory = false,
                ),
            ),
        )
        'S' -> BorrowedFormatItem.Component(
            Component.Second(Second(padding = paddingOrDefault(padding.value, Padding.Zero))),
        )
        't' -> lit("\t")
        'T' -> BorrowedFormatItem.Compound(
            listOf(
                BorrowedFormatItem.Component(
                    Component.Hour(Hour(padding = Padding.Zero, is12HourClock = false)),
                ),
                lit(":"),
                BorrowedFormatItem.Component(Component.Minute(Minute(padding = Padding.Zero))),
                lit(":"),
                BorrowedFormatItem.Component(Component.Second(Second(padding = Padding.Zero))),
            ),
        )
        'u' -> BorrowedFormatItem.Component(
            Component.Weekday(
                Weekday(
                    repr = WeekdayRepr.Monday,
                    oneIndexed = true,
                    caseSensitive = true,
                ),
            ),
        )
        'U' -> BorrowedFormatItem.Component(
            Component.WeekNumber(
                WeekNumber(
                    padding = paddingOrDefault(padding.value, Padding.Zero),
                    repr = WeekNumberRepr.Sunday,
                ),
            ),
        )
        'V' -> BorrowedFormatItem.Component(
            Component.WeekNumber(
                WeekNumber(
                    padding = paddingOrDefault(padding.value, Padding.Zero),
                    repr = WeekNumberRepr.Iso,
                ),
            ),
        )
        'w' -> BorrowedFormatItem.Component(
            Component.Weekday(
                Weekday(
                    repr = WeekdayRepr.Sunday,
                    oneIndexed = true,
                    caseSensitive = true,
                ),
            ),
        )
        'W' -> BorrowedFormatItem.Component(
            Component.WeekNumber(
                WeekNumber(
                    padding = paddingOrDefault(padding.value, Padding.Zero),
                    repr = WeekNumberRepr.Monday,
                ),
            ),
        )
        'x' -> BorrowedFormatItem.Compound(
            listOf(
                BorrowedFormatItem.Component(
                    Component.Month(
                        Month(
                            repr = MonthRepr.Numerical,
                            padding = Padding.Zero,
                            caseSensitive = true,
                        ),
                    ),
                ),
                lit("/"),
                BorrowedFormatItem.Component(Component.Day(Day(padding = Padding.Zero))),
                lit("/"),
                BorrowedFormatItem.Component(
                    Component.Year(
                        Year(
                            padding = Padding.Zero,
                            repr = YearRepr.LastTwo,
                            range = YearRange.Extended,
                            isoWeekBased = false,
                            signIsMandatory = false,
                        ),
                    ),
                ),
            ),
        )
        'X' -> BorrowedFormatItem.Compound(
            listOf(
                BorrowedFormatItem.Component(
                    Component.Hour(Hour(padding = Padding.Zero, is12HourClock = false)),
                ),
                lit(":"),
                BorrowedFormatItem.Component(Component.Minute(Minute(padding = Padding.Zero))),
                lit(":"),
                BorrowedFormatItem.Component(Component.Second(Second(padding = Padding.Zero))),
            ),
        )
        'y' -> BorrowedFormatItem.Component(
            Component.Year(
                Year(
                    padding = paddingOrDefault(padding.value, Padding.Zero),
                    repr = YearRepr.LastTwo,
                    range = YearRange.Extended,
                    isoWeekBased = false,
                    signIsMandatory = false,
                ),
            ),
        )
        'Y' -> BorrowedFormatItem.Component(
            Component.Year(
                Year(
                    padding = Padding.Zero,
                    repr = YearRepr.Full,
                    range = YearRange.Extended,
                    isoWeekBased = false,
                    signIsMandatory = false,
                ),
            ),
        )
        'z' -> BorrowedFormatItem.Compound(
            listOf(
                BorrowedFormatItem.Component(
                    Component.OffsetHour(
                        OffsetHour(
                            signIsMandatory = true,
                            padding = Padding.Zero,
                        ),
                    ),
                ),
                BorrowedFormatItem.Component(
                    Component.OffsetMinute(OffsetMinute(padding = Padding.Zero)),
                ),
            ),
        )
        'Z' -> return Result.failure(
            InvalidFormatDescription.NotSupported(
                what = "component",
                context = "",
                index = component.span.start.byte,
            ),
        )
        else -> return Result.failure(
            InvalidFormatDescription.InvalidComponentName(
                name = byteArrayOf(component.value).decodeToString(),
                index = component.span.start.byte,
            ),
        )
    }

    return Result.success(item)
}
