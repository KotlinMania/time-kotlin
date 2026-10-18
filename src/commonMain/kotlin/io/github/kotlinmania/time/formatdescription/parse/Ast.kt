// port-lint: source format_description/parse/ast.rs
package io.github.kotlinmania.time.formatdescription.parse

import io.github.kotlinmania.time.error.InvalidFormatDescription

/** One part of a complete format description AST. */
internal sealed class AstItem {
    class Literal(
        val value: Spanned<ByteArray>,
    ) : AstItem()

    class EscapedBracket(
        val first: Unused<Location>,
        val second: Unused<Location>,
    ) : AstItem()

    class Component(
        val openingBracket: Unused<Location>,
        val leadingWhitespace: Unused<Spanned<ByteArray>?>,
        val name: Spanned<ByteArray>,
        val modifiers: List<AstModifier>,
        val trailingWhitespace: Unused<Spanned<ByteArray>?>,
        val closingBracket: Unused<Location>,
    ) : AstItem()

    class Optional(
        val openingBracket: Location,
        val leadingWhitespace: Unused<Spanned<ByteArray>?>,
        val optionalKw: Unused<Spanned<ByteArray>>,
        val whitespace: Unused<Spanned<ByteArray>>,
        val nestedFormatDescription: NestedFormatDescription,
        val closingBracket: Location,
    ) : AstItem()

    class First(
        val openingBracket: Location,
        val leadingWhitespace: Unused<Spanned<ByteArray>?>,
        val firstKw: Unused<Spanned<ByteArray>>,
        val whitespace: Unused<Spanned<ByteArray>>,
        val nestedFormatDescriptions: List<NestedFormatDescription>,
        val closingBracket: Location,
    ) : AstItem()
}

/** A format description that is nested within another format description. */
internal class NestedFormatDescription(
    val openingBracket: Unused<Location>,
    val items: List<AstItem>,
    val closingBracket: Unused<Location>,
    val trailingWhitespace: Unused<Spanned<ByteArray>?>,
)

/** A modifier for a component. */
internal class AstModifier(
    val leadingWhitespace: Unused<Spanned<ByteArray>>,
    val key: Spanned<ByteArray>,
    val colon: Unused<Location>,
    val value: Spanned<ByteArray>,
)

/** Parse the provided tokens into an AST. */
internal fun parseAst(
    version: Int,
    tokens: Lexed,
): List<Result<AstItem>> = parseInner(version, nested = false, tokens)

private fun parseInner(
    version: Int,
    nested: Boolean,
    tokens: Lexed,
): List<Result<AstItem>> {
    require(version in 1..2)
    val items = mutableListOf<Result<AstItem>>()
    while (true) {
        if (nested && tokens.peekClosingBracket() != null) {
            break
        }
        val nextRes = tokens.next() ?: break
        val next =
            nextRes.getOrElse {
                items.add(Result.failure(it))
                break
            }
        val itemResult: Result<AstItem> =
            when (next) {
                is Token.Literal -> {
                    Result.success(AstItem.Literal(next.value))
                }

                is Token.Bracket -> {
                    if (next.kind == BracketKind.Opening) {
                        if (version <= 1) {
                            val secondLocation = tokens.nextIfOpeningBracket()
                            if (secondLocation != null) {
                                Result.success(
                                    AstItem.EscapedBracket(
                                        first = unused(next.location),
                                        second = unused(secondLocation),
                                    ),
                                )
                            } else {
                                parseComponent(version, next.location, tokens)
                            }
                        } else {
                            parseComponent(version, next.location, tokens)
                        }
                    } else {
                        error("closing bracket should have been consumed")
                    }
                }

                is Token.ComponentPart -> {
                    if (nested) {
                        Result.success(AstItem.Literal(next.value))
                    } else {
                        error("component part should have been consumed by parseComponent")
                    }
                }
            }
        items.add(itemResult)
        if (itemResult.isFailure) break
    }
    return items
}

private fun parseComponent(
    version: Int,
    openingBracket: Location,
    tokens: Lexed,
): Result<AstItem> {
    val leadingWhitespace = tokens.nextIfWhitespace()
    val name = tokens.nextIfNotWhitespace()
    if (name == null) {
        val span = leadingWhitespace?.span ?: openingBracket.toSelf()
        return Result.failure(
            ParseError(
                inner = unused(span.error("expected component name")),
                publicError = InvalidFormatDescription.MissingComponentName(index = span.start.byte),
            ).toInvalidFormatDescription(),
        )
    }

    val nameStr = name.value.decodeToString()
    if (nameStr == "optional") {
        val whitespace =
            tokens.nextIfWhitespace()
                ?: return Result.failure(
                    ParseError(
                        inner = unused(name.span.error("expected whitespace after `optional`")),
                        publicError =
                            InvalidFormatDescription.Expected(
                                what = "whitespace after `optional`",
                                index = name.span.end.byte,
                            ),
                    ).toInvalidFormatDescription(),
                )
        val nested =
            parseNested(version, whitespace.span.end, tokens)
                .getOrElse { return Result.failure(it) }
        val closingBracket =
            tokens.nextIfClosingBracket()
                ?: return Result.failure(
                    ParseError(
                        inner = unused(openingBracket.error("unclosed bracket")),
                        publicError = InvalidFormatDescription.UnclosedOpeningBracket(index = openingBracket.byte),
                    ).toInvalidFormatDescription(),
                )
        return Result.success(
            AstItem.Optional(
                openingBracket = openingBracket,
                leadingWhitespace = unused(leadingWhitespace),
                optionalKw = unused(name),
                whitespace = unused(whitespace),
                nestedFormatDescription = nested,
                closingBracket = closingBracket,
            ),
        )
    }

    if (nameStr == "first") {
        val whitespace =
            tokens.nextIfWhitespace()
                ?: return Result.failure(
                    ParseError(
                        inner = unused(name.span.error("expected whitespace after `first`")),
                        publicError =
                            InvalidFormatDescription.Expected(
                                what = "whitespace after `first`",
                                index = name.span.end.byte,
                            ),
                    ).toInvalidFormatDescription(),
                )
        val nestedFormatDescriptions = mutableListOf<NestedFormatDescription>()
        while (true) {
            val desc = parseNested(version, whitespace.span.end, tokens).getOrNull() ?: break
            nestedFormatDescriptions.add(desc)
        }
        val closingBracket =
            tokens.nextIfClosingBracket()
                ?: return Result.failure(
                    ParseError(
                        inner = unused(openingBracket.error("unclosed bracket")),
                        publicError = InvalidFormatDescription.UnclosedOpeningBracket(index = openingBracket.byte),
                    ).toInvalidFormatDescription(),
                )
        return Result.success(
            AstItem.First(
                openingBracket = openingBracket,
                leadingWhitespace = unused(leadingWhitespace),
                firstKw = unused(name),
                whitespace = unused(whitespace),
                nestedFormatDescriptions = nestedFormatDescriptions,
                closingBracket = closingBracket,
            ),
        )
    }

    val modifiers = mutableListOf<AstModifier>()
    var trailingWhitespace: Spanned<ByteArray>? = null
    while (true) {
        val whitespace = tokens.nextIfWhitespace() ?: break
        val nestedOpening = tokens.nextIfOpeningBracket()
        if (nestedOpening != null) {
            return Result.failure(
                ParseError(
                    inner = unused(nestedOpening.toSelf().error("modifier must be of the form `key:value`")),
                    publicError =
                        InvalidFormatDescription.InvalidModifier(
                            value = "[",
                            index = nestedOpening.byte,
                        ),
                ).toInvalidFormatDescription(),
            )
        }
        val part = tokens.nextIfNotWhitespace()
        if (part == null) {
            trailingWhitespace = whitespace
            break
        }
        val valueBytes = part.value
        val span = part.span
        val colonIndex = valueBytes.indexOf(':'.code.toByte())
        if (colonIndex == -1) {
            return Result.failure(
                ParseError(
                    inner = unused(span.error("modifier must be of the form `key:value`")),
                    publicError =
                        InvalidFormatDescription.InvalidModifier(
                            value = valueBytes.decodeToString(),
                            index = span.start.byte,
                        ),
                ).toInvalidFormatDescription(),
            )
        }
        val key = valueBytes.copyOfRange(0, colonIndex)
        val modVal = valueBytes.copyOfRange(colonIndex + 1, valueBytes.size)
        if (key.isEmpty()) {
            return Result.failure(
                ParseError(
                    inner = unused(span.shrinkToStart().error("expected modifier key")),
                    publicError =
                        InvalidFormatDescription.InvalidModifier(
                            value = "",
                            index = span.start.byte,
                        ),
                ).toInvalidFormatDescription(),
            )
        }
        if (modVal.isEmpty()) {
            return Result.failure(
                ParseError(
                    inner = unused(span.shrinkToEnd().error("expected modifier value")),
                    publicError =
                        InvalidFormatDescription.InvalidModifier(
                            value = "",
                            index = span.shrinkToEnd().start.byte,
                        ),
                ).toInvalidFormatDescription(),
            )
        }
        modifiers.add(
            AstModifier(
                leadingWhitespace = unused(whitespace),
                key = key.spanned(span.shrinkToBefore(colonIndex)),
                colon = unused(span.start.offset(colonIndex)),
                value = modVal.spanned(span.shrinkToAfter(colonIndex)),
            ),
        )
    }

    val closingBracket =
        tokens.nextIfClosingBracket()
            ?: return Result.failure(
                ParseError(
                    inner = unused(openingBracket.error("unclosed bracket")),
                    publicError = InvalidFormatDescription.UnclosedOpeningBracket(index = openingBracket.byte),
                ).toInvalidFormatDescription(),
            )

    return Result.success(
        AstItem.Component(
            openingBracket = unused(openingBracket),
            leadingWhitespace = unused(leadingWhitespace),
            name = name,
            modifiers = modifiers,
            trailingWhitespace = unused(trailingWhitespace),
            closingBracket = unused(closingBracket),
        ),
    )
}

private fun parseNested(
    version: Int,
    lastLocation: Location,
    tokens: Lexed,
): Result<NestedFormatDescription> {
    val openingBracket =
        tokens.nextIfOpeningBracket()
            ?: return Result.failure(
                ParseError(
                    inner = unused(lastLocation.error("expected opening bracket")),
                    publicError =
                        InvalidFormatDescription.Expected(
                            what = "opening bracket",
                            index = lastLocation.byte,
                        ),
                ).toInvalidFormatDescription(),
            )
    val rawItems = parseInner(version, nested = true, tokens)
    val items = mutableListOf<AstItem>()
    for (res in rawItems) {
        val item = res.getOrElse { return Result.failure(it) }
        items.add(item)
    }
    val closingBracket =
        tokens.nextIfClosingBracket()
            ?: return Result.failure(
                ParseError(
                    inner = unused(openingBracket.error("unclosed bracket")),
                    publicError = InvalidFormatDescription.UnclosedOpeningBracket(index = openingBracket.byte),
                ).toInvalidFormatDescription(),
            )
    val trailingWhitespace = tokens.nextIfWhitespace()
    return Result.success(
        NestedFormatDescription(
            openingBracket = unused(openingBracket),
            items = items,
            closingBracket = unused(closingBracket),
            trailingWhitespace = unused(trailingWhitespace),
        ),
    )
}
