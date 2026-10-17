// port-lint: source format_description/parse/ast.rs
package io.github.kotlinmania.time.formatdescription.parse

import io.github.kotlinmania.time.error.InvalidFormatDescription

/** One part of a complete format description AST. */
internal sealed class AstItem {
    class Literal(val value: Spanned<ByteArray>) : AstItem()

    data object EscapedBracket : AstItem()

    class Component(
        val name: Spanned<ByteArray>,
        val modifiers: List<AstModifier>,
    ) : AstItem()

    class Optional(
        val openingBracket: Location,
        val nestedFormatDescription: NestedFormatDescription,
        val closingBracket: Location,
    ) : AstItem()

    class First(
        val openingBracket: Location,
        val nestedFormatDescriptions: List<NestedFormatDescription>,
        val closingBracket: Location,
    ) : AstItem()
}

/** A format description that is nested within another format description. */
internal class NestedFormatDescription(
    val items: List<AstItem>,
)

/** A modifier for a component. */
internal class AstModifier(
    val key: Spanned<ByteArray>,
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
        val next = nextRes.getOrElse {
            items.add(Result.failure(it))
            break
        }
        val itemResult: Result<AstItem> = when (next) {
            is Token.Literal -> Result.success(AstItem.Literal(next.value))
            is Token.Bracket -> {
                if (next.kind == BracketKind.Opening) {
                    if (version <= 1) {
                        val second = tokens.nextIfOpeningBracket()
                        if (second != null) {
                            Result.success(AstItem.EscapedBracket)
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
            InvalidFormatDescription.MissingComponentName(index = span.start.byte),
        )
    }

    val nameStr = name.value.decodeToString()
    if (nameStr == "optional") {
        val whitespace = tokens.nextIfWhitespace()
            ?: return Result.failure(
                InvalidFormatDescription.Expected(
                    what = "whitespace after `optional`",
                    index = name.span.end.byte,
                ),
            )
        val nested = parseNested(version, whitespace.span.end, tokens)
            .getOrElse { return Result.failure(it) }
        val closingBracket = tokens.nextIfClosingBracket()
            ?: return Result.failure(
                InvalidFormatDescription.UnclosedOpeningBracket(index = openingBracket.byte),
            )
        return Result.success(
            AstItem.Optional(
                openingBracket = openingBracket,
                nestedFormatDescription = nested,
                closingBracket = closingBracket,
            ),
        )
    }

    if (nameStr == "first") {
        val whitespace = tokens.nextIfWhitespace()
            ?: return Result.failure(
                InvalidFormatDescription.Expected(
                    what = "whitespace after `first`",
                    index = name.span.end.byte,
                ),
            )
        val nestedFormatDescriptions = mutableListOf<NestedFormatDescription>()
        while (true) {
            val desc = parseNested(version, whitespace.span.end, tokens).getOrNull() ?: break
            nestedFormatDescriptions.add(desc)
        }
        val closingBracket = tokens.nextIfClosingBracket()
            ?: return Result.failure(
                InvalidFormatDescription.UnclosedOpeningBracket(index = openingBracket.byte),
            )
        return Result.success(
            AstItem.First(
                openingBracket = openingBracket,
                nestedFormatDescriptions = nestedFormatDescriptions,
                closingBracket = closingBracket,
            ),
        )
    }

    val modifiers = mutableListOf<AstModifier>()
    while (true) {
        val whitespace = tokens.nextIfWhitespace() ?: break
        val nestedOpening = tokens.nextIfOpeningBracket()
        if (nestedOpening != null) {
            return Result.failure(
                InvalidFormatDescription.InvalidModifier(
                    value = "[",
                    index = nestedOpening.byte,
                ),
            )
        }
        val part = tokens.nextIfNotWhitespace() ?: break
        val valueBytes = part.value
        val span = part.span
        val colonIndex = valueBytes.indexOf(':'.code.toByte())
        if (colonIndex == -1) {
            return Result.failure(
                InvalidFormatDescription.InvalidModifier(
                    value = valueBytes.decodeToString(),
                    index = span.start.byte,
                ),
            )
        }
        val key = valueBytes.copyOfRange(0, colonIndex)
        val modVal = valueBytes.copyOfRange(colonIndex + 1, valueBytes.size)
        if (key.isEmpty()) {
            return Result.failure(
                InvalidFormatDescription.InvalidModifier(
                    value = "",
                    index = span.start.byte,
                ),
            )
        }
        if (modVal.isEmpty()) {
            return Result.failure(
                InvalidFormatDescription.InvalidModifier(
                    value = "",
                    index = span.shrinkToEnd().start.byte,
                ),
            )
        }
        modifiers.add(
            AstModifier(
                key = key.spanned(span.shrinkToBefore(colonIndex)),
                value = modVal.spanned(span.shrinkToAfter(colonIndex)),
            ),
        )
    }

    val closingBracket = tokens.nextIfClosingBracket()
        ?: return Result.failure(
            InvalidFormatDescription.UnclosedOpeningBracket(index = openingBracket.byte),
        )

    return Result.success(
        AstItem.Component(
            name = name,
            modifiers = modifiers,
        ),
    )
}

private fun parseNested(
    version: Int,
    lastLocation: Location,
    tokens: Lexed,
): Result<NestedFormatDescription> {
    val openingBracket = tokens.nextIfOpeningBracket()
        ?: return Result.failure(
            InvalidFormatDescription.Expected(
                what = "opening bracket",
                index = lastLocation.byte,
            ),
        )
    val rawItems = parseInner(version, nested = true, tokens)
    val items = mutableListOf<AstItem>()
    for (res in rawItems) {
        val item = res.getOrElse { return Result.failure(it) }
        items.add(item)
    }
    val closingBracket = tokens.nextIfClosingBracket()
        ?: return Result.failure(
            InvalidFormatDescription.UnclosedOpeningBracket(index = openingBracket.byte),
        )
    tokens.nextIfWhitespace()
    return Result.success(NestedFormatDescription(items))
}
