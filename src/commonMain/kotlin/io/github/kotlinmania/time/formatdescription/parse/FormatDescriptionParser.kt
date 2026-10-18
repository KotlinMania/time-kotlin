package io.github.kotlinmania.time.formatdescription.parse

import io.github.kotlinmania.time.error.InvalidFormatDescription
import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatdescription.OwnedFormatItem

/**
 * Parse a sequence of items from the format description (version 1).
 */
fun parse(s: String): Result<List<BorrowedFormatItem>> = parseBorrowed(1, s)

/**
 * Parse a sequence of items from the format description with the given version (`1` or `2`).
 */
fun parseBorrowed(
    version: Int = 1,
    s: String,
): Result<List<BorrowedFormatItem>> {
    require(version in 1..2) { "Version must be 1 or 2" }
    val lexed = lex(version, s.encodeToByteArray())
    val astItems = parseAst(version, lexed)
    val formatItems = parseFormatItems(astItems)
    val result = mutableListOf<BorrowedFormatItem>()
    for (res in formatItems) {
        val item = res.getOrElse { return Result.failure(it) }
        val borrowed = item.toBorrowedFormatItem().getOrElse { return Result.failure(it) }
        result.add(borrowed)
    }
    return Result.success(result)
}

/**
 * Parse a sequence of items from the format description into an [OwnedFormatItem] with the given version (`1` or `2`).
 */
fun parseOwned(
    version: Int = 1,
    s: String,
): Result<OwnedFormatItem> {
    require(version in 1..2) { "Version must be 1 or 2" }
    val lexed = lex(version, s.encodeToByteArray())
    val astItems = parseAst(version, lexed)
    val formatItems = parseFormatItems(astItems)
    val items = mutableListOf<ParsedFormatItem>()
    for (res in formatItems) {
        val item = res.getOrElse { return Result.failure(it) }
        items.add(item)
    }
    return Result.success(items.toOwnedFormatItem())
}

internal fun parseStaticBorrowed(
    version: Int = 1,
    s: String,
): Result<List<BorrowedFormatItem>> {
    require(version in 1..2) { "Version must be 1 or 2" }
    val lexed = lex(version, s.encodeToByteArray())
    val astItems = parseAst(version, lexed)
    val formatItems = parseFormatItems(astItems)
    val result = mutableListOf<BorrowedFormatItem>()
    for (res in formatItems) {
        val item = res.getOrElse { return Result.failure(it) }
        result.add(item.toStaticBorrowedFormatItem())
    }
    return Result.success(result)
}

/** Attach [Location] information to each byte in the iterator. */
internal fun attachLocation(iter: Iterator<Byte>): Sequence<Pair<Byte, Location>> =
    sequence {
        var bytePos = 0
        for (byte in iter) {
            val location = Location(byte = bytePos)
            bytePos += 1
            yield(byte to location)
        }
    }

/** A location within a string. */
internal data class Location(
    val byte: Int,
) {
    fun to(end: Location): Span = Span(start = this, end = end)

    fun toSelf(): Span = Span(start = this, end = this)

    fun offset(amount: Int): Location = Location(byte = byte + amount)

    fun error(message: String): ErrorInner =
        ErrorInner(
            message = message,
            span = Span(start = this, end = this),
        )
}

/** A start and end point within a string. */
internal data class Span(
    val start: Location,
    val end: Location,
) {
    fun shrinkToStart(): Span = Span(start = start, end = start)

    fun shrinkToEnd(): Span = Span(start = end, end = end)

    fun shrinkToBefore(pos: Int): Span =
        Span(start = start, end = Location(byte = start.byte + pos - 1))

    fun shrinkToAfter(pos: Int): Span =
        Span(start = Location(byte = start.byte + pos + 1), end = end)

    fun error(message: String): ErrorInner =
        ErrorInner(
            message = message,
            span = this,
        )
}

/** A value with an associated [Span]. */
internal data class Spanned<out T>(
    val value: T,
    val span: Span,
)

internal fun <T> T.spanned(span: Span): Spanned<T> = Spanned(this, span)

/** The internal error type. */
internal data class ErrorInner(
    val message: String,
    val span: Span,
)

/** A complete error description. */
internal data class ParseError(
    val inner: Unused<ErrorInner>,
    val publicError: InvalidFormatDescription,
) {
    fun toInvalidFormatDescription(): InvalidFormatDescription = publicError
}

/** A value that may be used in the future, but currently is not. */
internal data class Unused<out T>(
    val value: T,
)

/** Indicate that a value is currently unused. */
internal fun <T> unused(value: T): Unused<T> = Unused(value)

