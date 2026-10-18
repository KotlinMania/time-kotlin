package io.github.kotlinmania.time.parsing.combinator

import io.github.kotlinmania.time.formatdescription.modifier.Padding
import io.github.kotlinmania.time.parsing.ParsedInteger
import io.github.kotlinmania.time.parsing.ParsedItem

/** The sign of a number. */
internal enum class Sign {
    Negative,
    Positive,
}

/** Parse a "+" or "-" sign. */
internal fun sign(input: ByteArray): ParsedItem<Sign>? {
    if (input.isEmpty()) return null
    return when (input[0]) {
        '-'.code.toByte() -> ParsedItem(input.copyOfRange(1, input.size), Sign.Negative)
        '+'.code.toByte() -> ParsedItem(input.copyOfRange(1, input.size), Sign.Positive)
        else -> null
    }
}

/** Consume zero or more instances of the provided parser. */
internal fun zeroOrMore(
    parser: (ByteArray) -> ParsedItem<Unit>?,
): (ByteArray) -> ParsedItem<Unit> = { initialInput ->
    var input = initialInput
    while (true) {
        val remaining = parser(input) ?: break
        input = remaining.intoInner()
    }
    ParsedItem(input, Unit)
}

/** Consume one or more instances of the provided parser. */
internal fun oneOrMore(
    parser: (ByteArray) -> ParsedItem<Unit>?,
): (ByteArray) -> ParsedItem<Unit>? = { initialInput ->
    val first = parser(initialInput)
    if (first == null) {
        null
    } else {
        var input = first.intoInner()
        while (true) {
            val remaining = parser(input) ?: break
            input = remaining.intoInner()
        }
        ParsedItem(input, Unit)
    }
}

/** Consume between `n` and `m` digits, returning the numerical value. */
internal fun <T : Any> nToMDigits(
    n: Int,
    m: Int,
    integer: ParsedInteger<T>,
    initialInput: ByteArray,
): ParsedItem<T>? {
    require(n > 0 && m >= n)
    var input = initialInput
    var value = integer.zero

    // Mandatory
    for (i in 0 until n) {
        val parsed = anyDigit(input) ?: return null
        input = parsed.remaining
        val digit = (parsed.value - '0'.code.toUByte()).toUByte()
        value = if (i != integer.maxNumDigits - 1) {
            integer.pushDigit(value, digit)
        } else {
            integer.checkedPushDigit(value, digit) ?: return null
        }
    }

    // Optional
    for (i in n until m) {
        val parsed = anyDigit(input) ?: break
        input = parsed.remaining
        val digit = (parsed.value - '0'.code.toUByte()).toUByte()
        value = if (i != integer.maxNumDigits - 1) {
            integer.pushDigit(value, digit)
        } else {
            integer.checkedPushDigit(value, digit) ?: return null
        }
    }

    return ParsedItem(input, value)
}

/** Consume between `n` and `m` ASCII digits, returning the raw digit string. */
internal fun nToMDigitString(
    n: Int,
    m: Int,
    initialInput: ByteArray,
): ParsedItem<String>? {
    require(n > 0 && m >= n)
    var input = initialInput
    val sb = StringBuilder(m)

    repeat(n) {
        val parsed = anyDigit(input) ?: return null
        input = parsed.remaining
        sb.append(parsed.value.toInt().toChar())
    }

    for (_ in n until m) {
        val parsed = anyDigit(input) ?: break
        input = parsed.remaining
        sb.append(parsed.value.toInt().toChar())
    }

    return ParsedItem(input, sb.toString())
}

/** Consume one or two digits, returning the numerical value. */
internal fun oneOrTwoDigits(input: ByteArray): ParsedItem<UByte>? {
    if (input.isEmpty()) return null
    val a = input[0].toUByte()
    if (a < '0'.code.toUByte() || a > '9'.code.toUByte()) return null
    val aVal = (a - '0'.code.toUByte()).toUByte()
    if (input.size >= 2) {
        val b = input[1].toUByte()
        if (b in '0'.code.toUByte()..'9'.code.toUByte()) {
            val bVal = (b - '0'.code.toUByte()).toUByte()
            return ParsedItem(
                input.copyOfRange(2, input.size),
                (aVal.toUInt() * 10u + bVal.toUInt()).toUByte(),
            )
        }
    }
    return ParsedItem(input.copyOfRange(1, input.size), aVal)
}

/** Parse an exact number of digits without padding. */
internal object ExactlyNDigits {
    fun parse1(input: ByteArray): ParsedItem<UByte>? {
        if (input.isEmpty()) return null
        val a = input[0].toUByte()
        if (a !in '0'.code.toUByte()..'9'.code.toUByte()) return null
        return ParsedItem(input.copyOfRange(1, input.size), (a - '0'.code.toUByte()).toUByte())
    }

    fun parse2(input: ByteArray): ParsedItem<UByte>? {
        if (input.size < 2) return null
        val a = input[0].toUByte()
        val b = input[1].toUByte()
        if (a !in '0'.code.toUByte()..'9'.code.toUByte() || b !in '0'.code.toUByte()..'9'.code.toUByte()) {
            return null
        }
        val value = ((a - '0'.code.toUByte()) * 10u + (b - '0'.code.toUByte())).toUByte()
        return ParsedItem(input.copyOfRange(2, input.size), value)
    }

    fun parse3(input: ByteArray): ParsedItem<UShort>? {
        if (input.size < 3) return null
        val a = input[0].toUByte()
        val b = input[1].toUByte()
        val c = input[2].toUByte()
        if (
            a !in '0'.code.toUByte()..'9'.code.toUByte() ||
            b !in '0'.code.toUByte()..'9'.code.toUByte() ||
            c !in '0'.code.toUByte()..'9'.code.toUByte()
        ) {
            return null
        }
        val value = (
            (a - '0'.code.toUByte()) * 100u +
                (b - '0'.code.toUByte()) * 10u +
                (c - '0'.code.toUByte())
            ).toUShort()
        return ParsedItem(input.copyOfRange(3, input.size), value)
    }

    fun parse4(input: ByteArray): ParsedItem<UShort>? {
        if (input.size < 4) return null
        var value = 0u
        for (i in 0 until 4) {
            val d = input[i].toUByte()
            if (d !in '0'.code.toUByte()..'9'.code.toUByte()) return null
            value = value * 10u + (d - '0'.code.toUByte())
        }
        return ParsedItem(input.copyOfRange(4, input.size), value.toUShort())
    }

    fun parse5(input: ByteArray): ParsedItem<UInt>? = parseUIntDigits(input, 5)

    fun parse6(input: ByteArray): ParsedItem<UInt>? = parseUIntDigits(input, 6)

    fun parse7(input: ByteArray): ParsedItem<UInt>? = parseUIntDigits(input, 7)

    fun parse8(input: ByteArray): ParsedItem<UInt>? = parseUIntDigits(input, 8)

    fun parse9(input: ByteArray): ParsedItem<UInt>? = parseUIntDigits(input, 9)

    private fun parseUIntDigits(input: ByteArray, count: Int): ParsedItem<UInt>? {
        if (input.size < count) return null
        var value = 0u
        for (i in 0 until count) {
            val d = input[i].toUByte()
            if (d !in '0'.code.toUByte()..'9'.code.toUByte()) return null
            value = value * 10u + (d - '0'.code.toUByte())
        }
        return ParsedItem(input.copyOfRange(count, input.size), value)
    }
}


/** Consume exactly `n` digits, returning the numerical value. */
internal fun <T : Any> exactlyNDigitsPadded(
    n: Int,
    padding: Padding,
    integer: ParsedInteger<T>,
): (ByteArray) -> ParsedItem<T>? = nToMDigitsPadded(n, n, padding, integer)

/** Consume between `n` and `m` digits, returning the numerical value. */
internal fun <T : Any> nToMDigitsPadded(
    n: Int,
    m: Int,
    padding: Padding,
    integer: ParsedInteger<T>,
): (ByteArray) -> ParsedItem<T>? {
    require(n > 0 && m >= n)
    return parser@{ initialInput ->
        var input = initialInput
        when (padding) {
            Padding.None -> nToMDigits(1, m, integer, input)
            Padding.Space -> {
                var value = integer.zero
                var padWidth = 0
                for (_ in 0 until (n - 1)) {
                    val parsed = asciiChar(' '.code.toByte(), input)
                    if (parsed != null) {
                        padWidth += 1
                        input = parsed.remaining
                    } else {
                        break
                    }
                }

                // Mandatory
                for (i in 0 until (n - padWidth)) {
                    val parsed = anyDigit(input) ?: return@parser null
                    input = parsed.remaining
                    val digit = (parsed.value - '0'.code.toUByte()).toUByte()
                    value = if (i != integer.maxNumDigits - 1) {
                        integer.pushDigit(value, digit)
                    } else {
                        integer.checkedPushDigit(value, digit) ?: return@parser null
                    }
                }

                // Optional
                for (i in n until m) {
                    val parsed = anyDigit(input) ?: break
                    input = parsed.remaining
                    val digit = (parsed.value - '0'.code.toUByte()).toUByte()
                    value = if (i - padWidth != integer.maxNumDigits - 1) {
                        integer.pushDigit(value, digit)
                    } else {
                        integer.checkedPushDigit(value, digit) ?: return@parser null
                    }
                }

                ParsedItem(input, value)
            }
            Padding.Zero -> nToMDigits(n, m, integer, input)
        }
    }
}

/** Consume exactly one digit. */
internal fun anyDigit(input: ByteArray): ParsedItem<UByte>? {
    if (input.isEmpty()) return null
    val c = input[0].toUByte()
    return if (c in '0'.code.toUByte()..'9'.code.toUByte()) {
        ParsedItem(input.copyOfRange(1, input.size), c)
    } else {
        null
    }
}

/** Consume exactly one of the provided ASCII characters. */
internal fun asciiChar(char: Byte, input: ByteArray): ParsedItem<Unit>? {
    if (input.isEmpty()) return null
    return if (input[0] == char) {
        ParsedItem(input.copyOfRange(1, input.size), Unit)
    } else {
        null
    }
}

/** Consume exactly one of the provided ASCII characters, case-insensitive. */
internal fun asciiCharIgnoreCase(char: Byte, input: ByteArray): ParsedItem<Unit>? {
    if (input.isEmpty()) return null
    val c = input[0].toInt()
    val target = char.toInt()
    val cLower = if (c in 'A'.code..'Z'.code) c + 32 else c
    val targetLower = if (target in 'A'.code..'Z'.code) target + 32 else target
    return if (cLower == targetLower) {
        ParsedItem(input.copyOfRange(1, input.size), Unit)
    } else {
        null
    }
}

/** Optionally consume an input with a given parser. */
internal fun <T : Any> opt(
    parser: (ByteArray) -> ParsedItem<T>?,
): (ByteArray) -> ParsedItem<T?> = { input ->
    val parsed = parser(input)
    if (parsed != null) {
        ParsedItem(parsed.remaining, parsed.value)
    } else {
        ParsedItem(input, null)
    }
}
