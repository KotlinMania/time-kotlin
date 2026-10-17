// port-lint: source format_description/parse/lexer.rs
package io.github.kotlinmania.time.formatdescription.parse

import io.github.kotlinmania.time.error.InvalidFormatDescription

/** What type of bracket is present. */
internal enum class BracketKind {
    Opening,
    Closing,
}

/** Indicates whether the component part is whitespace or not. */
internal enum class ComponentKind {
    Whitespace,
    NotWhitespace,
}

/** A token emitted by the lexer. */
internal sealed class Token {
    class Literal(val value: Spanned<ByteArray>) : Token()

    class Bracket(
        val kind: BracketKind,
        val location: Location,
    ) : Token()

    class ComponentPart(
        val kind: ComponentKind,
        val value: Spanned<ByteArray>,
    ) : Token()
}

/** A peekable iterator over the lexed tokens. */
internal class Lexed(
    private val tokens: List<Result<Token>>,
) {
    private var index: Int = 0

    fun hasNext(): Boolean = index < tokens.size

    fun next(): Result<Token>? = if (index < tokens.size) tokens[index++] else null

    fun peek(): Result<Token>? = if (index < tokens.size) tokens[index] else null

    fun nextIfWhitespace(): Spanned<ByteArray>? {
        val peeked = peek()?.getOrNull()
        if (peeked is Token.ComponentPart && peeked.kind == ComponentKind.Whitespace) {
            index++
            return peeked.value
        }
        return null
    }

    fun nextIfNotWhitespace(): Spanned<ByteArray>? {
        val peeked = peek()?.getOrNull()
        if (peeked is Token.ComponentPart && peeked.kind == ComponentKind.NotWhitespace) {
            index++
            return peeked.value
        }
        return null
    }

    fun nextIfOpeningBracket(): Location? {
        val peeked = peek()?.getOrNull()
        if (peeked is Token.Bracket && peeked.kind == BracketKind.Opening) {
            index++
            return peeked.location
        }
        return null
    }

    fun peekClosingBracket(): Location? {
        val peeked = peek()?.getOrNull()
        if (peeked is Token.Bracket && peeked.kind == BracketKind.Closing) {
            return peeked.location
        }
        return null
    }

    fun nextIfClosingBracket(): Location? {
        val peeked = peek()?.getOrNull()
        if (peeked is Token.Bracket && peeked.kind == BracketKind.Closing) {
            index++
            return peeked.location
        }
        return null
    }
}

private fun isAsciiWhitespace(b: Byte): Boolean =
    b == ' '.code.toByte() ||
        b == '\t'.code.toByte() ||
        b == '\n'.code.toByte() ||
        b == '\r'.code.toByte() ||
        b == 0x0C.toByte()

/** Parse the byte slice into a [Lexed] token stream. */
internal fun lex(
    version: Int,
    input: ByteArray,
): Lexed {
    require(version in 1..2)
    val result = mutableListOf<Result<Token>>()
    var depth = 0
    var pos = 0
    var secondBracketLocation: Location? = null

    while (true) {
        if (version <= 1 && secondBracketLocation != null) {
            val loc = secondBracketLocation
            secondBracketLocation = null
            result.add(Result.success(Token.Bracket(BracketKind.Opening, loc)))
            continue
        }

        if (pos >= input.size) break

        val byte = input[pos]
        val startLoc = Location(pos)
        pos++

        // Possible backslash escape sequence in version 2
        if (byte == '\\'.code.toByte() && version >= 2) {
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
            val nextByte = input[pos]
            val charLoc = Location(pos)
            pos++
            if (
                nextByte == '\\'.code.toByte() ||
                nextByte == '['.code.toByte() ||
                nextByte == ']'.code.toByte()
            ) {
                val charSlice = byteArrayOf(nextByte).spanned(startLoc.to(charLoc))
                if (depth == 0) {
                    result.add(Result.success(Token.Literal(charSlice)))
                } else {
                    result.add(
                        Result.success(
                            Token.ComponentPart(ComponentKind.NotWhitespace, charSlice),
                        ),
                    )
                }
            } else {
                result.add(
                    Result.failure(
                        InvalidFormatDescription.Expected(
                            what = "valid escape sequence",
                            index = charLoc.byte,
                        ),
                    ),
                )
                break
            }
            continue
        }

        // Potentially escaped opening bracket in version 1
        if (byte == '['.code.toByte() && version <= 1) {
            if (pos < input.size && input[pos] == '['.code.toByte()) {
                secondBracketLocation = Location(pos)
                pos++
            } else {
                depth += 1
            }
            result.add(Result.success(Token.Bracket(BracketKind.Opening, startLoc)))
            continue
        }

        // Opening bracket in version 2
        if (byte == '['.code.toByte()) {
            depth += 1
            result.add(Result.success(Token.Bracket(BracketKind.Opening, startLoc)))
            continue
        }

        // Closing bracket
        if (byte == ']'.code.toByte() && depth > 0) {
            depth -= 1
            result.add(Result.success(Token.Bracket(BracketKind.Closing, startLoc)))
            continue
        }

        // Literal when depth == 0
        if (depth == 0) {
            val sliceStart = startLoc.byte
            var endLoc = startLoc
            while (pos < input.size) {
                val next = input[pos]
                if ((version >= 2 && next == '\\'.code.toByte()) || next == '['.code.toByte()) {
                    break
                }
                endLoc = Location(pos)
                pos++
            }
            val slice = input.copyOfRange(sliceStart, pos)
            result.add(Result.success(Token.Literal(slice.spanned(startLoc.to(endLoc)))))
            continue
        }

        // ComponentPart when depth > 0
        val sliceStart = startLoc.byte
        var endLoc = startLoc
        val isWs = isAsciiWhitespace(byte)
        while (pos < input.size) {
            val next = input[pos]
            if (
                next == '\\'.code.toByte() ||
                next == '['.code.toByte() ||
                next == ']'.code.toByte() ||
                isAsciiWhitespace(next) != isWs
            ) {
                break
            }
            endLoc = Location(pos)
            pos++
        }
        val slice = input.copyOfRange(sliceStart, pos)
        result.add(
            Result.success(
                Token.ComponentPart(
                    kind = if (isWs) ComponentKind.Whitespace else ComponentKind.NotWhitespace,
                    value = slice.spanned(startLoc.to(endLoc)),
                ),
            ),
        )
    }

    return Lexed(result)
}
