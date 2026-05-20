// port-lint: ignore - upstream test source is src/tests.rs.
package io.github.kotlinmania.time

import io.github.kotlinmania.time.ext.numDigits
import kotlin.test.Test
import kotlin.test.assertEquals

class InternalTest {
    @Test
    fun digitCount() {
        assertEquals(1, 1.toUByte().numDigits())
        assertEquals(1, 9.toUByte().numDigits())
        assertEquals(2, 10.toUByte().numDigits())
        assertEquals(2, 99.toUByte().numDigits())
        assertEquals(3, 100.toUByte().numDigits())

        assertEquals(1, 1.toUShort().numDigits())
        assertEquals(1, 9.toUShort().numDigits())
        assertEquals(2, 10.toUShort().numDigits())
        assertEquals(2, 99.toUShort().numDigits())
        assertEquals(3, 100.toUShort().numDigits())
        assertEquals(3, 999.toUShort().numDigits())
        assertEquals(4, 1_000.toUShort().numDigits())
        assertEquals(4, 9_999.toUShort().numDigits())
        assertEquals(5, 10_000.toUShort().numDigits())

        assertEquals(1, 1u.numDigits())
        assertEquals(1, 9u.numDigits())
        assertEquals(2, 10u.numDigits())
        assertEquals(2, 99u.numDigits())
        assertEquals(3, 100u.numDigits())
        assertEquals(3, 999u.numDigits())
        assertEquals(4, 1_000u.numDigits())
        assertEquals(4, 9_999u.numDigits())
        assertEquals(5, 10_000u.numDigits())
        assertEquals(5, 99_999u.numDigits())
        assertEquals(6, 100_000u.numDigits())
        assertEquals(6, 999_999u.numDigits())
        assertEquals(7, 1_000_000u.numDigits())
        assertEquals(7, 9_999_999u.numDigits())
        assertEquals(8, 10_000_000u.numDigits())
        assertEquals(8, 99_999_999u.numDigits())
        assertEquals(9, 100_000_000u.numDigits())
        assertEquals(9, 999_999_999u.numDigits())
        assertEquals(10, 1_000_000_000u.numDigits())
    }
}
