// port-lint: tests tests.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.ext.numDigits
import io.github.kotlinmania.time.formatdescription.Period
import io.github.kotlinmania.time.parsing.ParsedItem
import io.github.kotlinmania.time.parsing.combinator.rfc.ExtendedKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InternalTest {
    @Test
    fun digitCount() {
        assertEquals(1.toUByte(), 1.toUByte().numDigits())
        assertEquals(1.toUByte(), 9.toUByte().numDigits())
        assertEquals(2.toUByte(), 10.toUByte().numDigits())
        assertEquals(2.toUByte(), 99.toUByte().numDigits())
        assertEquals(3.toUByte(), 100.toUByte().numDigits())

        assertEquals(1.toUByte(), 1.toUShort().numDigits())
        assertEquals(1.toUByte(), 9.toUShort().numDigits())
        assertEquals(2.toUByte(), 10.toUShort().numDigits())
        assertEquals(2.toUByte(), 99.toUShort().numDigits())
        assertEquals(3.toUByte(), 100.toUShort().numDigits())
        assertEquals(3.toUByte(), 999.toUShort().numDigits())
        assertEquals(4.toUByte(), 1_000.toUShort().numDigits())
        assertEquals(4.toUByte(), 9_999.toUShort().numDigits())
        assertEquals(5.toUByte(), 10_000.toUShort().numDigits())

        assertEquals(1.toUByte(), 1u.numDigits())
        assertEquals(1.toUByte(), 9u.numDigits())
        assertEquals(2.toUByte(), 10u.numDigits())
        assertEquals(2.toUByte(), 99u.numDigits())
        assertEquals(3.toUByte(), 100u.numDigits())
        assertEquals(3.toUByte(), 999u.numDigits())
        assertEquals(4.toUByte(), 1_000u.numDigits())
        assertEquals(4.toUByte(), 9_999u.numDigits())
        assertEquals(5.toUByte(), 10_000u.numDigits())
        assertEquals(5.toUByte(), 99_999u.numDigits())
        assertEquals(6.toUByte(), 100_000u.numDigits())
        assertEquals(6.toUByte(), 999_999u.numDigits())
        assertEquals(7.toUByte(), 1_000_000u.numDigits())
        assertEquals(7.toUByte(), 9_999_999u.numDigits())
        assertEquals(8.toUByte(), 10_000_000u.numDigits())
        assertEquals(8.toUByte(), 99_999_999u.numDigits())
        assertEquals(9.toUByte(), 100_000_000u.numDigits())
        assertEquals(9.toUByte(), 999_999_999u.numDigits())
        assertEquals(10.toUByte(), 1_000_000_000u.numDigits())
    }

    @Test
    fun debug() {
        val item = ParsedItem(ByteArray(0), 0)
        assertEquals(item.toString(), item.toString())
        assertEquals(Period.Am.toString(), Period.Am.toString())
        assertEquals(ExtendedKind.Basic.toString(), ExtendedKind.Basic.toString())
    }

    @Test
    fun clone() {
        assertEquals(Period.Am, Period.Am)
        assertEquals(ExtendedKind.Basic, ExtendedKind.Basic)
    }

    @Test
    fun parsingInternals() {
        assertNull(ParsedItem(ByteArray(0), Unit).flatMap<Unit> { null })
    }
}
