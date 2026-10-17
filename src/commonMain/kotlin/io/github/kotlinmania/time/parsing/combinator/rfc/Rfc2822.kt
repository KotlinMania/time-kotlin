// port-lint: source parsing/combinator/rfc/rfc2822.rs
package io.github.kotlinmania.time.parsing.combinator.rfc

import io.github.kotlinmania.time.parsing.ParsedItem
import io.github.kotlinmania.time.parsing.combinator.asciiChar
import io.github.kotlinmania.time.parsing.combinator.oneOrMore
import io.github.kotlinmania.time.parsing.combinator.zeroOrMore

private const val DEPTH_LIMIT: Int = 32

/** Consume the `fws` rule. */
internal fun fws(initialInput: ByteArray): ParsedItem<Unit>? {
    var input = initialInput
    if (input.size >= 2 && input[0] == '\r'.code.toByte() && input[1] == '\n'.code.toByte()) {
        return oneOrMore(::wsp)(input.copyOfRange(2, input.size))
    }
    input = oneOrMore(::wsp)(input)?.intoInner() ?: return null
    while (input.size >= 2 && input[0] == '\r'.code.toByte() && input[1] == '\n'.code.toByte()) {
        input = oneOrMore(::wsp)(input.copyOfRange(2, input.size))?.intoInner() ?: return null
    }
    return ParsedItem(input, Unit)
}

/** Consume the `cfws` rule. */
internal fun cfws(input: ByteArray): ParsedItem<Unit>? =
    oneOrMore { inp -> fws(inp) ?: comment(inp, 1) }(input)

/** Consume the `comment` rule. */
private fun comment(initialInput: ByteArray, depth: Int): ParsedItem<Unit>? {
    if (depth == DEPTH_LIMIT) {
        return null
    }
    var input = asciiChar('('.code.toByte(), initialInput)?.intoInner() ?: return null
    input = zeroOrMore(::fws)(input).intoInner()
    while (true) {
        val rest = ccontent(input, depth + 1) ?: break
        input = rest.intoInner()
        input = zeroOrMore(::fws)(input).intoInner()
    }
    input = asciiChar(')'.code.toByte(), input)?.intoInner() ?: return null
    return ParsedItem(input, Unit)
}

/** Consume the `ccontent` rule. */
private fun ccontent(input: ByteArray, depth: Int): ParsedItem<Unit>? =
    ctext(input) ?: quotedPair(input) ?: comment(input, depth)

/** Consume the `ctext` rule. */
private fun ctext(input: ByteArray): ParsedItem<Unit>? {
    noWsCtl(input)?.let { return it }
    if (input.isEmpty()) return null
    val b = input[0].toUByte().toInt()
    return if (b in 33..39 || b in 42..91 || b in 93..126) {
        ParsedItem(input.copyOfRange(1, input.size), Unit)
    } else {
        null
    }
}

/** Consume the `quotedPair` rule. */
private fun quotedPair(initialInput: ByteArray): ParsedItem<Unit>? {
    var input = asciiChar('\\'.code.toByte(), initialInput)?.intoInner() ?: return null
    input = text(input).intoInner()
    return ParsedItem(input, Unit)
}

/** Consume the `noWsCtl` rule. */
private fun noWsCtl(input: ByteArray): ParsedItem<Unit>? {
    if (input.isEmpty()) return null
    val b = input[0].toUByte().toInt()
    return if (b in 1..8 || b in 11..12 || b in 14..31 || b == 127) {
        ParsedItem(input.copyOfRange(1, input.size), Unit)
    } else {
        null
    }
}

/** Consume the `text` rule. */
private fun text(initialInput: ByteArray): ParsedItem<Unit> {
    if (initialInput.isNotEmpty()) {
        val b = initialInput[0].toUByte().toInt()
        if (b in 1..9 || b in 11..12 || b in 14..127) {
            return ParsedItem(initialInput.copyOfRange(1, initialInput.size), Unit)
        }
    }

    fun obsChar(inp: ByteArray): ByteArray? {
        if (inp.isEmpty()) return null
        val b = inp[0].toUByte().toInt()
        if (b == ')'.code) return null
        return if (b in 0..9 || b in 11..12 || b in 14..127) {
            inp.copyOfRange(1, inp.size)
        } else {
            null
        }
    }

    var input = zeroOrMore { asciiChar('\n'.code.toByte(), it) }(initialInput).intoInner()
    input = zeroOrMore { asciiChar('\r'.code.toByte(), it) }(input).intoInner()
    while (true) {
        val rest = obsChar(input) ?: break
        input = rest
        input = zeroOrMore { asciiChar('\n'.code.toByte(), it) }(input).intoInner()
        input = zeroOrMore { asciiChar('\r'.code.toByte(), it) }(input).intoInner()
    }
    return ParsedItem(input, Unit)
}

/** Consume an old zone literal, returning the offset in hours. */
internal fun zoneLiteral(input: ByteArray): ParsedItem<Byte>? {
    if (input.size < 3) {
        if (input.isEmpty()) return null
        val first = input[0].toUByte().toInt()
        val rest1 = input.copyOfRange(1, input.size)
        if (rest1.isNotEmpty()) {
            val second = rest1[0].toUByte().toInt()
            if ((first == 'u'.code || first == 'U'.code) && (second == 't'.code || second == 'T'.code)) {
                return ParsedItem(rest1.copyOfRange(1, rest1.size), 0)
            }
        }
        val isAsciiAlpha = (first in 'a'.code..'z'.code) || (first in 'A'.code..'Z'.code)
        return if (first != 'j'.code && first != 'J'.code && isAsciiAlpha) {
            ParsedItem(rest1, 0)
        } else {
            null
        }
    }

    fun toAsciiLower(b: Byte): Int {
        val c = b.toUByte().toInt()
        return if (c in 'A'.code..'Z'.code) c + 32 else c
    }

    val c1 = toAsciiLower(input[0]).toChar()
    val c2 = toAsciiLower(input[1]).toChar()
    val c3 = toAsciiLower(input[2]).toChar()
    val rest = input.copyOfRange(3, input.size)
    val zoneStr = "$c1$c2$c3"

    val zone: Byte = when (zoneStr) {
        "est" -> -5
        "edt" -> -4
        "cst" -> -6
        "cdt" -> -5
        "mst" -> -7
        "mdt" -> -6
        "pst" -> -8
        "pdt" -> -7
        "gmt" -> 0
        else -> return null
    }
    return ParsedItem(rest, zone)
}
