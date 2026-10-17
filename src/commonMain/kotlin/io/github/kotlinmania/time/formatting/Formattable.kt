// port-lint: source formatting/formattable.rs
package io.github.kotlinmania.time.formatting

import io.github.kotlinmania.time.error.Format
import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatdescription.OwnedFormatItem
import io.github.kotlinmania.time.formatdescription.wellknown.Iso8601
import io.github.kotlinmania.time.formatdescription.wellknown.Rfc2822
import io.github.kotlinmania.time.formatdescription.wellknown.Rfc3339
import io.github.kotlinmania.time.formatdescription.wellknown.iso8601.formatDate
import io.github.kotlinmania.time.formatdescription.wellknown.iso8601.formatOffset
import io.github.kotlinmania.time.formatdescription.wellknown.iso8601.formatTime
import kotlin.math.abs

/**
 * A type that describes a format.
 *
 * Implementors of [Formattable] are format descriptions.
 */
interface Formattable

/** Format the item into the provided output, returning the number of bytes written. */
internal fun <V, State> Formattable.formatInto(
    output: Appendable,
    value: V,
    provider: ComponentProvider<V, State>,
    state: State,
): Result<Int> =
    when (this) {
        is BorrowedFormatItem -> formatBorrowedInto(this, output, value, provider, state)
        is OwnedFormatItem -> formatOwnedInto(this, output, value, provider, state)
        is Rfc2822 -> formatRfc2822Into(output, value, provider, state)
        is Rfc3339 -> formatRfc3339Into(output, value, provider, state)
        is Iso8601 -> formatIso8601Into(this, output, value, provider, state)
        else -> Result.failure(Format.InsufficientTypeInformation())
    }

/** Format the item directly to a `String`. */
internal fun <V, State> Formattable.formatValue(
    value: V,
    provider: ComponentProvider<V, State>,
    state: State,
): Result<String> {
    val sb = StringBuilder()
    formatInto(sb, value, provider, state).getOrElse { return Result.failure(it) }
    return Result.success(sb.toString())
}

private fun <V, State> formatBorrowedInto(
    item: BorrowedFormatItem,
    output: Appendable,
    value: V,
    provider: ComponentProvider<V, State>,
    state: State,
): Result<Int> =
    when (item) {
        is BorrowedFormatItem.Literal -> write(output, item.bytes)
        is BorrowedFormatItem.Component -> formatComponent(output, item.component, value, provider, state)
        is BorrowedFormatItem.Compound -> formatBorrowedListInto(item.items, output, value, provider, state)
        is BorrowedFormatItem.Optional -> formatBorrowedInto(item.item, output, value, provider, state)
        is BorrowedFormatItem.First ->
            if (item.items.isEmpty()) {
                Result.success(0)
            } else {
                formatBorrowedInto(item.items[0], output, value, provider, state)
            }
    }

internal fun <V, State> formatBorrowedListInto(
    items: List<BorrowedFormatItem>,
    output: Appendable,
    value: V,
    provider: ComponentProvider<V, State>,
    state: State,
): Result<Int> {
    var bytes = 0
    for (sub in items) {
        bytes += formatBorrowedInto(sub, output, value, provider, state).getOrElse { return Result.failure(it) }
    }
    return Result.success(bytes)
}

private fun <V, State> formatOwnedInto(
    item: OwnedFormatItem,
    output: Appendable,
    value: V,
    provider: ComponentProvider<V, State>,
    state: State,
): Result<Int> =
    when (item) {
        is OwnedFormatItem.Literal -> write(output, item.bytes)
        is OwnedFormatItem.Component -> formatComponent(output, item.component, value, provider, state)
        is OwnedFormatItem.Compound -> formatOwnedListInto(item.items, output, value, provider, state)
        is OwnedFormatItem.Optional -> formatOwnedInto(item.item, output, value, provider, state)
        is OwnedFormatItem.First ->
            if (item.items.isEmpty()) {
                Result.success(0)
            } else {
                formatOwnedInto(item.items[0], output, value, provider, state)
            }
    }

internal fun <V, State> formatOwnedListInto(
    items: List<OwnedFormatItem>,
    output: Appendable,
    value: V,
    provider: ComponentProvider<V, State>,
    state: State,
): Result<Int> {
    var bytes = 0
    for (sub in items) {
        bytes += formatOwnedInto(sub, output, value, provider, state).getOrElse { return Result.failure(it) }
    }
    return Result.success(bytes)
}

private fun <V, State> formatRfc2822Into(
    output: Appendable,
    value: V,
    provider: ComponentProvider<V, State>,
    state: State,
): Result<Int> {
    if (!provider.suppliesDate || !provider.suppliesTime || !provider.suppliesOffset) {
        return Result.failure(Format.InsufficientTypeInformation())
    }

    var bytes = 0

    if (provider.calendarYear(value, state) < 1900) {
        return Result.failure(Format.InvalidComponent("year"))
    }
    if (provider.offsetSecond(value, state).toInt() != 0) {
        return Result.failure(Format.InvalidComponent("offset_second"))
    }

    bytes +=
        write(
            output,
            WEEKDAY_NAMES[provider.weekday(value, state).numberDaysFromMonday().toInt()].substring(0, 3),
        ).getOrElse { return Result.failure(it) }
    bytes += write(output, ", ").getOrElse { return Result.failure(it) }
    bytes += formatNumberPadZero(2, output, provider.day(value, state).toUInt()).getOrElse { return Result.failure(it) }
    bytes += write(output, " ").getOrElse { return Result.failure(it) }
    bytes +=
        write(
            output,
            MONTH_NAMES[provider.month(value, state).number().toInt() - 1].substring(0, 3),
        ).getOrElse { return Result.failure(it) }
    bytes += write(output, " ").getOrElse { return Result.failure(it) }
    bytes += formatNumberPadZero(4, output, provider.calendarYear(value, state).toUInt()).getOrElse { return Result.failure(it) }
    bytes += write(output, " ").getOrElse { return Result.failure(it) }
    bytes += formatNumberPadZero(2, output, provider.hour(value, state).toUInt()).getOrElse { return Result.failure(it) }
    bytes += write(output, ":").getOrElse { return Result.failure(it) }
    bytes += formatNumberPadZero(2, output, provider.minute(value, state).toUInt()).getOrElse { return Result.failure(it) }
    bytes += write(output, ":").getOrElse { return Result.failure(it) }
    bytes += formatNumberPadZero(2, output, provider.second(value, state).toUInt()).getOrElse { return Result.failure(it) }
    bytes += write(output, " ").getOrElse { return Result.failure(it) }
    bytes += writeIfElse(output, provider.offsetIsNegative(value, state), "-", "+").getOrElse { return Result.failure(it) }
    bytes += formatNumberPadZero(2, output, abs(provider.offsetHour(value, state).toInt()).toUInt()).getOrElse { return Result.failure(it) }
    bytes += formatNumberPadZero(2, output, abs(provider.offsetMinute(value, state).toInt()).toUInt()).getOrElse { return Result.failure(it) }

    return Result.success(bytes)
}

private fun <V, State> formatRfc3339Into(
    output: Appendable,
    value: V,
    provider: ComponentProvider<V, State>,
    state: State,
): Result<Int> {
    if (!provider.suppliesDate || !provider.suppliesTime || !provider.suppliesOffset) {
        return Result.failure(Format.InsufficientTypeInformation())
    }

    val offsetHour = provider.offsetHour(value, state)
    var bytes = 0

    if (provider.calendarYear(value, state) !in 0 until 10_000) {
        return Result.failure(Format.InvalidComponent("year"))
    }
    if (abs(offsetHour.toInt()) > 23) {
        return Result.failure(Format.InvalidComponent("offset_hour"))
    }
    if (provider.offsetSecond(value, state).toInt() != 0) {
        return Result.failure(Format.InvalidComponent("offset_second"))
    }

    bytes += formatNumberPadZero(4, output, provider.calendarYear(value, state).toUInt()).getOrElse { return Result.failure(it) }
    bytes += write(output, "-").getOrElse { return Result.failure(it) }
    bytes += formatNumberPadZero(2, output, provider.month(value, state).number().toUInt()).getOrElse { return Result.failure(it) }
    bytes += write(output, "-").getOrElse { return Result.failure(it) }
    bytes += formatNumberPadZero(2, output, provider.day(value, state).toUInt()).getOrElse { return Result.failure(it) }
    bytes += write(output, "T").getOrElse { return Result.failure(it) }
    bytes += formatNumberPadZero(2, output, provider.hour(value, state).toUInt()).getOrElse { return Result.failure(it) }
    bytes += write(output, ":").getOrElse { return Result.failure(it) }
    bytes += formatNumberPadZero(2, output, provider.minute(value, state).toUInt()).getOrElse { return Result.failure(it) }
    bytes += write(output, ":").getOrElse { return Result.failure(it) }
    bytes += formatNumberPadZero(2, output, provider.second(value, state).toUInt()).getOrElse { return Result.failure(it) }

    val nanos = provider.nanosecond(value, state)
    if (nanos != 0u) {
        bytes += write(output, ".").getOrElse { return Result.failure(it) }
        val nanoRes =
            when {
                nanos % 10u != 0u -> formatNumberPadZero(9, output, nanos)
                (nanos / 10u) % 10u != 0u -> formatNumberPadZero(8, output, nanos / 10u)
                (nanos / 100u) % 10u != 0u -> formatNumberPadZero(7, output, nanos / 100u)
                (nanos / 1_000u) % 10u != 0u -> formatNumberPadZero(6, output, nanos / 1_000u)
                (nanos / 10_000u) % 10u != 0u -> formatNumberPadZero(5, output, nanos / 10_000u)
                (nanos / 100_000u) % 10u != 0u -> formatNumberPadZero(4, output, nanos / 100_000u)
                (nanos / 1_000_000u) % 10u != 0u -> formatNumberPadZero(3, output, nanos / 1_000_000u)
                (nanos / 10_000_000u) % 10u != 0u -> formatNumberPadZero(2, output, nanos / 10_000_000u)
                else -> formatNumberPadZero(1, output, nanos / 100_000_000u)
            }
        bytes += nanoRes.getOrElse { return Result.failure(it) }
    }

    if (provider.offsetIsUtc(value, state)) {
        bytes += write(output, "Z").getOrElse { return Result.failure(it) }
        return Result.success(bytes)
    }

    bytes += writeIfElse(output, provider.offsetIsNegative(value, state), "-", "+").getOrElse { return Result.failure(it) }
    bytes += formatNumberPadZero(2, output, abs(offsetHour.toInt()).toUInt()).getOrElse { return Result.failure(it) }
    bytes += write(output, ":").getOrElse { return Result.failure(it) }
    bytes += formatNumberPadZero(2, output, abs(provider.offsetMinute(value, state).toInt()).toUInt()).getOrElse { return Result.failure(it) }

    return Result.success(bytes)
}

private fun <V, State> formatIso8601Into(
    iso: Iso8601,
    output: Appendable,
    value: V,
    provider: ComponentProvider<V, State>,
    state: State,
): Result<Int> {
    if ((iso.formatDate && !provider.suppliesDate) ||
        (iso.formatTime && !provider.suppliesTime) ||
        (iso.formatOffset && !provider.suppliesOffset) ||
        (!iso.formatDate && !iso.formatTime && !iso.formatOffset)
    ) {
        return Result.failure(Format.InsufficientTypeInformation())
    }

    var bytes = 0
    if (iso.formatDate) {
        bytes += formatIso8601Date(iso, output, value, provider, state).getOrElse { return Result.failure(it) }
    }
    if (iso.formatTime) {
        bytes += formatIso8601Time(iso, output, value, provider, state).getOrElse { return Result.failure(it) }
    }
    if (iso.formatOffset) {
        bytes += formatIso8601Offset(iso, output, value, provider, state).getOrElse { return Result.failure(it) }
    }

    return Result.success(bytes)
}
