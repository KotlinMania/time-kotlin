// port-lint: source format_description/parse/format_item.rs
package io.github.kotlinmania.time.formatdescription.parse

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

/** A description of how to format and parse one part of a type. */
internal sealed class ParsedFormatItem {
    class Literal(
        val value: ByteArray,
    ) : ParsedFormatItem()

    class Component(
        val value: io.github.kotlinmania.time.formatdescription.Component,
    ) : ParsedFormatItem()

    class Optional(
        val value: List<ParsedFormatItem>,
        val span: Span,
    ) : ParsedFormatItem()

    class First(
        val value: List<List<ParsedFormatItem>>,
        val span: Span,
    ) : ParsedFormatItem()

    fun toBorrowedFormatItem(): Result<BorrowedFormatItem> =
        when (this) {
            is Literal -> {
                Result.success(BorrowedFormatItem.Literal(value))
            }

            is Component -> {
                Result.success(BorrowedFormatItem.Component(value))
            }

            is Optional -> {
                Result.failure(
                    InvalidFormatDescription.NotSupported(
                        what = "optional item",
                        context = "runtime-parsed format descriptions",
                        index = span.start.byte,
                    ),
                )
            }

            is First -> {
                Result.failure(
                    InvalidFormatDescription.NotSupported(
                        what = "'first' item",
                        context = "runtime-parsed format descriptions",
                        index = span.start.byte,
                    ),
                )
            }
        }

    fun toOwnedFormatItem(): OwnedFormatItem =
        when (this) {
            is Literal -> OwnedFormatItem.Literal(value.copyOf())
            is Component -> OwnedFormatItem.Component(value)
            is Optional -> OwnedFormatItem.Optional(value.toOwnedFormatItem())
            is First -> OwnedFormatItem.First(value.map { it.toOwnedFormatItem() })
        }

    fun toStaticBorrowedFormatItem(): BorrowedFormatItem =
        when (this) {
            is Literal -> BorrowedFormatItem.Literal(value.copyOf())
            is Component -> BorrowedFormatItem.Component(value)
            is Optional -> BorrowedFormatItem.Optional(value.toStaticBorrowedFormatItem())
            is First -> BorrowedFormatItem.First(value.map { it.toStaticBorrowedFormatItem() })
        }

    companion object {
        fun fromAst(astItem: AstItem): Result<ParsedFormatItem> =
            when (astItem) {
                is AstItem.Component -> {
                    componentFromAst(astItem.name, astItem.modifiers)
                        .map { Component(it) }
                }

                is AstItem.Literal -> {
                    Result.success(Literal(astItem.value.value))
                }

                is AstItem.EscapedBracket -> {
                    Result.success(Literal(byteArrayOf('['.code.toByte())))
                }

                is AstItem.Optional -> {
                    val items = mutableListOf<ParsedFormatItem>()
                    for (sub in astItem.nestedFormatDescription.items) {
                        val parsed = fromAst(sub).getOrElse { return Result.failure(it) }
                        items.add(parsed)
                    }
                    Result.success(
                        Optional(
                            value = items,
                            span = astItem.openingBracket.to(astItem.closingBracket),
                        ),
                    )
                }

                is AstItem.First -> {
                    val outer = mutableListOf<List<ParsedFormatItem>>()
                    for (nested in astItem.nestedFormatDescriptions) {
                        val inner = mutableListOf<ParsedFormatItem>()
                        for (sub in nested.items) {
                            val parsed = fromAst(sub).getOrElse { return Result.failure(it) }
                            inner.add(parsed)
                        }
                        outer.add(inner)
                    }
                    Result.success(
                        First(
                            value = outer,
                            span = astItem.openingBracket.to(astItem.closingBracket),
                        ),
                    )
                }
            }
    }
}

internal fun List<ParsedFormatItem>.toOwnedFormatItem(): OwnedFormatItem =
    if (size == 1) {
        this[0].toOwnedFormatItem()
    } else {
        OwnedFormatItem.Compound(map { it.toOwnedFormatItem() })
    }

internal fun List<ParsedFormatItem>.toStaticBorrowedFormatItem(): BorrowedFormatItem =
    if (size == 1) {
        this[0].toStaticBorrowedFormatItem()
    } else {
        BorrowedFormatItem.Compound(map { it.toStaticBorrowedFormatItem() })
    }

internal fun parseFormatItems(
    astItems: List<Result<AstItem>>,
): List<Result<ParsedFormatItem>> =
    astItems.map { res ->
        res.fold(
            onSuccess = { ParsedFormatItem.fromAst(it) },
            onFailure = { Result.failure(it) },
        )
    }

private fun invalidModifierKey(mod: AstModifier): Result<Nothing> =
    Result.failure(
        InvalidFormatDescription.InvalidModifier(
            value = mod.key.value.decodeToString(),
            index = mod.key.span.start.byte,
        ),
    )

private fun invalidModifierValue(value: Spanned<ByteArray>): Result<Nothing> =
    Result.failure(
        InvalidFormatDescription.InvalidModifier(
            value = value.value.decodeToString(),
            index = value.span.start.byte,
        ),
    )

private fun parsePadding(value: Spanned<ByteArray>): Result<Padding> =
    when (value.value.decodeToString().lowercase()) {
        "space" -> Result.success(Padding.Space)
        "zero" -> Result.success(Padding.Zero)
        "none" -> Result.success(Padding.None)
        else -> invalidModifierValue(value)
    }

private fun parseTrailingInput(value: Spanned<ByteArray>): Result<TrailingInput> =
    when (value.value.decodeToString().lowercase()) {
        "prohibit" -> Result.success(TrailingInput.Prohibit)
        "discard" -> Result.success(TrailingInput.Discard)
        else -> invalidModifierValue(value)
    }

private fun parseHourBase(value: Spanned<ByteArray>): Result<Boolean> =
    when (value.value.decodeToString().lowercase()) {
        "12" -> Result.success(true)
        "24" -> Result.success(false)
        else -> invalidModifierValue(value)
    }

private fun parseBooleanModifier(value: Spanned<ByteArray>): Result<Boolean> =
    when (value.value.decodeToString().lowercase()) {
        "false" -> Result.success(false)
        "true" -> Result.success(true)
        else -> invalidModifierValue(value)
    }

private fun parseMonthRepr(value: Spanned<ByteArray>): Result<MonthRepr> =
    when (value.value.decodeToString().lowercase()) {
        "numerical" -> Result.success(MonthRepr.Numerical)
        "long" -> Result.success(MonthRepr.Long)
        "short" -> Result.success(MonthRepr.Short)
        else -> invalidModifierValue(value)
    }

private fun parseSignBehavior(value: Spanned<ByteArray>): Result<Boolean> =
    when (value.value.decodeToString().lowercase()) {
        "automatic" -> Result.success(false)
        "mandatory" -> Result.success(true)
        else -> invalidModifierValue(value)
    }

private fun parsePeriodCase(value: Spanned<ByteArray>): Result<Boolean> =
    when (value.value.decodeToString().lowercase()) {
        "lower" -> Result.success(false)
        "upper" -> Result.success(true)
        else -> invalidModifierValue(value)
    }

private fun parseSubsecondDigits(value: Spanned<ByteArray>): Result<SubsecondDigits> =
    when (value.value.decodeToString().lowercase()) {
        "1" -> Result.success(SubsecondDigits.One)
        "2" -> Result.success(SubsecondDigits.Two)
        "3" -> Result.success(SubsecondDigits.Three)
        "4" -> Result.success(SubsecondDigits.Four)
        "5" -> Result.success(SubsecondDigits.Five)
        "6" -> Result.success(SubsecondDigits.Six)
        "7" -> Result.success(SubsecondDigits.Seven)
        "8" -> Result.success(SubsecondDigits.Eight)
        "9" -> Result.success(SubsecondDigits.Nine)
        "1+" -> Result.success(SubsecondDigits.OneOrMore)
        else -> invalidModifierValue(value)
    }

private fun parseUnixTimestampPrecision(value: Spanned<ByteArray>): Result<UnixTimestampPrecision> =
    when (value.value.decodeToString().lowercase()) {
        "second" -> Result.success(UnixTimestampPrecision.Second)
        "millisecond" -> Result.success(UnixTimestampPrecision.Millisecond)
        "microsecond" -> Result.success(UnixTimestampPrecision.Microsecond)
        "nanosecond" -> Result.success(UnixTimestampPrecision.Nanosecond)
        else -> invalidModifierValue(value)
    }

private fun parseWeekNumberRepr(value: Spanned<ByteArray>): Result<WeekNumberRepr> =
    when (value.value.decodeToString().lowercase()) {
        "iso" -> Result.success(WeekNumberRepr.Iso)
        "sunday" -> Result.success(WeekNumberRepr.Sunday)
        "monday" -> Result.success(WeekNumberRepr.Monday)
        else -> invalidModifierValue(value)
    }

private fun parseWeekdayRepr(value: Spanned<ByteArray>): Result<WeekdayRepr> =
    when (value.value.decodeToString().lowercase()) {
        "short" -> Result.success(WeekdayRepr.Short)
        "long" -> Result.success(WeekdayRepr.Long)
        "sunday" -> Result.success(WeekdayRepr.Sunday)
        "monday" -> Result.success(WeekdayRepr.Monday)
        else -> invalidModifierValue(value)
    }

private fun parseYearBase(value: Spanned<ByteArray>): Result<Boolean> =
    when (value.value.decodeToString().lowercase()) {
        "calendar" -> Result.success(false)
        "iso_week" -> Result.success(true)
        else -> invalidModifierValue(value)
    }

private fun parseYearRepr(value: Spanned<ByteArray>): Result<YearRepr> =
    when (value.value.decodeToString().lowercase()) {
        "full" -> Result.success(YearRepr.Full)
        "century" -> Result.success(YearRepr.Century)
        "last_two" -> Result.success(YearRepr.LastTwo)
        else -> invalidModifierValue(value)
    }

private fun parseYearRange(value: Spanned<ByteArray>): Result<YearRange> =
    when (value.value.decodeToString().lowercase()) {
        "standard" -> Result.success(YearRange.Standard)
        "extended" -> Result.success(YearRange.Extended)
        else -> invalidModifierValue(value)
    }

private fun parseNonZeroU16(value: Spanned<ByteArray>): Result<UShort> {
    val str = value.value.decodeToString()
    val parsed = str.toUShortOrNull()
    return if (parsed != null && parsed > 0u) {
        Result.success(parsed)
    } else {
        invalidModifierValue(value)
    }
}

private fun componentFromAst(
    name: Spanned<ByteArray>,
    modifiers: List<AstModifier>,
): Result<Component> {
    val nameStr = name.value.decodeToString()
    return when (nameStr.lowercase()) {
        "day" -> {
            var padding: Padding? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "padding" -> padding = parsePadding(mod.value).getOrElse { return Result.failure(it) }
                    else -> return invalidModifierKey(mod)
                }
            }
            Result.success(Component.Day(Day(padding = padding ?: Padding.default())))
        }

        "end" -> {
            var trailingInput: TrailingInput? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "trailing_input" -> {
                        trailingInput = parseTrailingInput(mod.value).getOrElse { return Result.failure(it) }
                    }

                    else -> {
                        return invalidModifierKey(mod)
                    }
                }
            }
            Result.success(Component.End(End(trailingInput = trailingInput ?: TrailingInput.default())))
        }

        "hour" -> {
            var padding: Padding? = null
            var is12HourClock: Boolean? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "padding" -> padding = parsePadding(mod.value).getOrElse { return Result.failure(it) }
                    "repr" -> is12HourClock = parseHourBase(mod.value).getOrElse { return Result.failure(it) }
                    else -> return invalidModifierKey(mod)
                }
            }
            Result.success(
                Component.Hour(
                    Hour(
                        padding = padding ?: Padding.default(),
                        is12HourClock = is12HourClock ?: false,
                    ),
                ),
            )
        }

        "ignore" -> {
            var count: UShort? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "count" -> count = parseNonZeroU16(mod.value).getOrElse { return Result.failure(it) }
                    else -> return invalidModifierKey(mod)
                }
            }
            if (count == null) {
                return Result.failure(
                    InvalidFormatDescription.MissingRequiredModifier(
                        name = "count",
                        index = name.span.start.byte,
                    ),
                )
            }
            Result.success(Component.Ignore(Ignore(count = count)))
        }

        "minute" -> {
            var padding: Padding? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "padding" -> padding = parsePadding(mod.value).getOrElse { return Result.failure(it) }
                    else -> return invalidModifierKey(mod)
                }
            }
            Result.success(Component.Minute(Minute(padding = padding ?: Padding.default())))
        }

        "month" -> {
            var padding: Padding? = null
            var repr: MonthRepr? = null
            var caseSensitive: Boolean? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "padding" -> {
                        padding = parsePadding(mod.value).getOrElse { return Result.failure(it) }
                    }

                    "repr" -> {
                        repr = parseMonthRepr(mod.value).getOrElse { return Result.failure(it) }
                    }

                    "case_sensitive" -> {
                        caseSensitive = parseBooleanModifier(mod.value).getOrElse { return Result.failure(it) }
                    }

                    else -> {
                        return invalidModifierKey(mod)
                    }
                }
            }
            Result.success(
                Component.Month(
                    Month(
                        padding = padding ?: Padding.default(),
                        repr = repr ?: MonthRepr.default(),
                        caseSensitive = caseSensitive ?: true,
                    ),
                ),
            )
        }

        "offset_hour" -> {
            var signIsMandatory: Boolean? = null
            var padding: Padding? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "sign" -> signIsMandatory = parseSignBehavior(mod.value).getOrElse { return Result.failure(it) }
                    "padding" -> padding = parsePadding(mod.value).getOrElse { return Result.failure(it) }
                    else -> return invalidModifierKey(mod)
                }
            }
            Result.success(
                Component.OffsetHour(
                    OffsetHour(
                        signIsMandatory = signIsMandatory ?: false,
                        padding = padding ?: Padding.default(),
                    ),
                ),
            )
        }

        "offset_minute" -> {
            var padding: Padding? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "padding" -> padding = parsePadding(mod.value).getOrElse { return Result.failure(it) }
                    else -> return invalidModifierKey(mod)
                }
            }
            Result.success(Component.OffsetMinute(OffsetMinute(padding = padding ?: Padding.default())))
        }

        "offset_second" -> {
            var padding: Padding? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "padding" -> padding = parsePadding(mod.value).getOrElse { return Result.failure(it) }
                    else -> return invalidModifierKey(mod)
                }
            }
            Result.success(Component.OffsetSecond(OffsetSecond(padding = padding ?: Padding.default())))
        }

        "ordinal" -> {
            var padding: Padding? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "padding" -> padding = parsePadding(mod.value).getOrElse { return Result.failure(it) }
                    else -> return invalidModifierKey(mod)
                }
            }
            Result.success(Component.Ordinal(Ordinal(padding = padding ?: Padding.default())))
        }

        "period" -> {
            var isUppercase: Boolean? = null
            var caseSensitive: Boolean? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "case" -> {
                        isUppercase = parsePeriodCase(mod.value).getOrElse { return Result.failure(it) }
                    }

                    "case_sensitive" -> {
                        caseSensitive = parseBooleanModifier(mod.value).getOrElse { return Result.failure(it) }
                    }

                    else -> {
                        return invalidModifierKey(mod)
                    }
                }
            }
            Result.success(
                Component.Period(
                    Period(
                        isUppercase = isUppercase ?: true,
                        caseSensitive = caseSensitive ?: true,
                    ),
                ),
            )
        }

        "second" -> {
            var padding: Padding? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "padding" -> padding = parsePadding(mod.value).getOrElse { return Result.failure(it) }
                    else -> return invalidModifierKey(mod)
                }
            }
            Result.success(Component.Second(Second(padding = padding ?: Padding.default())))
        }

        "subsecond" -> {
            var digits: SubsecondDigits? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "digits" -> digits = parseSubsecondDigits(mod.value).getOrElse { return Result.failure(it) }
                    else -> return invalidModifierKey(mod)
                }
            }
            Result.success(Component.Subsecond(Subsecond(digits = digits ?: SubsecondDigits.default())))
        }

        "unix_timestamp" -> {
            var precision: UnixTimestampPrecision? = null
            var signIsMandatory: Boolean? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "precision" -> {
                        precision = parseUnixTimestampPrecision(mod.value).getOrElse { return Result.failure(it) }
                    }

                    "sign" -> {
                        signIsMandatory = parseSignBehavior(mod.value).getOrElse { return Result.failure(it) }
                    }

                    else -> {
                        return invalidModifierKey(mod)
                    }
                }
            }
            Result.success(
                Component.UnixTimestamp(
                    UnixTimestamp(
                        precision = precision ?: UnixTimestampPrecision.default(),
                        signIsMandatory = signIsMandatory ?: false,
                    ),
                ),
            )
        }

        "weekday" -> {
            var repr: WeekdayRepr? = null
            var oneIndexed: Boolean? = null
            var caseSensitive: Boolean? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "repr" -> {
                        repr = parseWeekdayRepr(mod.value).getOrElse { return Result.failure(it) }
                    }

                    "one_indexed" -> {
                        oneIndexed = parseBooleanModifier(mod.value).getOrElse { return Result.failure(it) }
                    }

                    "case_sensitive" -> {
                        caseSensitive = parseBooleanModifier(mod.value).getOrElse { return Result.failure(it) }
                    }

                    else -> {
                        return invalidModifierKey(mod)
                    }
                }
            }
            Result.success(
                Component.Weekday(
                    Weekday(
                        repr = repr ?: WeekdayRepr.default(),
                        oneIndexed = oneIndexed ?: true,
                        caseSensitive = caseSensitive ?: true,
                    ),
                ),
            )
        }

        "week_number" -> {
            var padding: Padding? = null
            var repr: WeekNumberRepr? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "padding" -> padding = parsePadding(mod.value).getOrElse { return Result.failure(it) }
                    "repr" -> repr = parseWeekNumberRepr(mod.value).getOrElse { return Result.failure(it) }
                    else -> return invalidModifierKey(mod)
                }
            }
            Result.success(
                Component.WeekNumber(
                    WeekNumber(
                        padding = padding ?: Padding.default(),
                        repr = repr ?: WeekNumberRepr.default(),
                    ),
                ),
            )
        }

        "year" -> {
            var padding: Padding? = null
            var repr: YearRepr? = null
            var range: YearRange? = null
            var isoWeekBased: Boolean? = null
            var signIsMandatory: Boolean? = null
            for (mod in modifiers) {
                when (
                    mod.key.value
                        .decodeToString()
                        .lowercase()
                ) {
                    "padding" -> padding = parsePadding(mod.value).getOrElse { return Result.failure(it) }
                    "repr" -> repr = parseYearRepr(mod.value).getOrElse { return Result.failure(it) }
                    "range" -> range = parseYearRange(mod.value).getOrElse { return Result.failure(it) }
                    "base" -> isoWeekBased = parseYearBase(mod.value).getOrElse { return Result.failure(it) }
                    "sign" -> signIsMandatory = parseSignBehavior(mod.value).getOrElse { return Result.failure(it) }
                    else -> return invalidModifierKey(mod)
                }
            }
            Result.success(
                Component.Year(
                    Year(
                        padding = padding ?: Padding.default(),
                        repr = repr ?: YearRepr.default(),
                        range = range ?: YearRange.default(),
                        isoWeekBased = isoWeekBased ?: false,
                        signIsMandatory = signIsMandatory ?: false,
                    ),
                ),
            )
        }

        else -> {
            Result.failure(
                InvalidFormatDescription.InvalidComponentName(
                    name = nameStr,
                    index = name.span.start.byte,
                ),
            )
        }
    }
}
