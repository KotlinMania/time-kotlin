// port-lint: ignore - upstream test source is tests/integration/utc_offset.rs.
package io.github.kotlinmania.time

import kotlin.test.Test
import kotlin.test.assertEquals

class UtcOffsetTest {
    @Test
    fun utcIsZero() {
        assertEquals(offset(0), UtcOffset.UTC)
    }

    @Test
    fun fromHms() {
        val cases =
            listOf(
                HmsCase(0, 0, 0, UtcOffset.UTC),
                HmsCase(0, 0, 1, offset(0, 0, 1)),
                HmsCase(0, 0, -1, offset(0, 0, -1)),
                HmsCase(1, 0, 0, offset(1)),
                HmsCase(-1, 0, 0, offset(-1)),
                HmsCase(23, 59, 0, offset(23, 59)),
                HmsCase(-23, -59, 0, offset(-23, -59)),
                HmsCase(23, 59, 59, offset(23, 59, 59)),
                HmsCase(-23, -59, -59, offset(-23, -59, -59)),
                HmsCase(1, 2, 3, offset(1, 2, 3)),
                HmsCase(1, -2, -3, offset(1, 2, 3)),
                HmsCase(0, 2, -3, offset(0, 2, 3)),
            )

        for ((hours, minutes, seconds, expected) in cases) {
            assertEquals(expected, UtcOffset.fromHms(hours, minutes, seconds).getOrThrow())
        }
    }

    @Test
    fun fromWholeSeconds() {
        val cases =
            listOf(
                0 to UtcOffset.UTC,
                1 to offset(0, 0, 1),
                -1 to offset(0, 0, -1),
                3_600 to offset(1),
                -3_600 to offset(-1),
                86_340 to offset(23, 59),
                -86_340 to offset(-23, -59),
                86_399 to offset(23, 59, 59),
                -86_399 to offset(-23, -59, -59),
            )

        for ((seconds, expected) in cases) {
            assertEquals(expected, UtcOffset.fromWholeSeconds(seconds).getOrThrow())
        }
    }

    @Test
    fun asHms() {
        val cases =
            listOf(
                UtcOffset.UTC to Triple(0, 0, 0),
                offset(0, 0, 1) to Triple(0, 0, 1),
                offset(0, 0, -1) to Triple(0, 0, -1),
                offset(1) to Triple(1, 0, 0),
                offset(-1) to Triple(-1, 0, 0),
                offset(23, 59) to Triple(23, 59, 0),
                offset(-23, -59) to Triple(-23, -59, 0),
                offset(23, 59, 59) to Triple(23, 59, 59),
                offset(-23, -59, -59) to Triple(-23, -59, -59),
            )

        for ((offset, expected) in cases) {
            assertEquals(expected, offset.asHms())
        }
    }

    @Test
    fun wholeHours() {
        assertEquals(1, offset(1, 2, 3).wholeHours())
        assertEquals(-1, offset(-1, -2, -3).wholeHours())
    }

    @Test
    fun wholeMinutes() {
        assertEquals(62, offset(1, 2, 3).wholeMinutes())
        assertEquals(-62, offset(-1, -2, -3).wholeMinutes())
    }

    @Test
    fun minutesPastHour() {
        assertEquals(2, offset(1, 2, 3).minutesPastHour())
        assertEquals(-2, offset(-1, -2, -3).minutesPastHour())
    }

    @Test
    fun wholeSeconds() {
        val cases =
            listOf(
                UtcOffset.UTC to 0,
                offset(0, 0, 1) to 1,
                offset(0, 0, -1) to -1,
                offset(1) to 3_600,
                offset(-1) to -3_600,
                offset(23, 59) to 86_340,
                offset(-23, -59) to -86_340,
                offset(23, 59, 59) to 86_399,
                offset(-23, -59, -59) to -86_399,
            )

        for ((offset, expected) in cases) {
            assertEquals(expected, offset.wholeSeconds())
        }
    }

    @Test
    fun secondsPastMinute() {
        assertEquals(3, offset(1, 2, 3).secondsPastMinute())
        assertEquals(-3, offset(-1, -2, -3).secondsPastMinute())
    }

    @Test
    fun isUtc() {
        val cases =
            listOf(
                UtcOffset.UTC to true,
                offset(0, 0, 1) to false,
                offset(0, 0, -1) to false,
                offset(1) to false,
                offset(-1) to false,
                offset(23, 59) to false,
                offset(-23, -59) to false,
                offset(23, 59, 59) to false,
                offset(-23, -59, -59) to false,
            )

        for ((offset, expected) in cases) {
            assertEquals(expected, offset.isUtc())
        }
    }

    @Test
    fun isPositive() {
        val cases =
            listOf(
                UtcOffset.UTC to false,
                offset(0, 0, 1) to true,
                offset(0, 0, -1) to false,
                offset(1) to true,
                offset(-1) to false,
                offset(23, 59) to true,
                offset(-23, -59) to false,
                offset(23, 59, 59) to true,
                offset(-23, -59, -59) to false,
            )

        for ((offset, expected) in cases) {
            assertEquals(expected, offset.isPositive())
        }
    }

    @Test
    fun isNegative() {
        val cases =
            listOf(
                UtcOffset.UTC to false,
                offset(0, 0, 1) to false,
                offset(0, 0, -1) to true,
                offset(1) to false,
                offset(-1) to true,
                offset(23, 59) to false,
                offset(-23, -59) to true,
                offset(23, 59, 59) to false,
                offset(-23, -59, -59) to true,
            )

        for ((offset, expected) in cases) {
            assertEquals(expected, offset.isNegative())
        }
    }

    @Test
    fun ordering() {
        val cases =
            listOf(
                CompareCase(UtcOffset.UTC, UtcOffset.UTC, 0),
                CompareCase(offset(1), offset(1), 0),
                CompareCase(offset(-1), offset(-1), 0),
                CompareCase(offset(1), UtcOffset.UTC, 1),
                CompareCase(UtcOffset.UTC, offset(-1), 1),
                CompareCase(offset(-1), offset(1), -1),
                CompareCase(offset(23, 59), offset(23, 58), 1),
                CompareCase(offset(-23, -59), offset(-23, -58), -1),
                CompareCase(offset(23, 59, 59), offset(23, 59, 58), 1),
                CompareCase(offset(-23, -59, -59), offset(-23, -59, -58), -1),
            )

        for ((a, b, expectedSign) in cases) {
            assertEquals(expectedSign, a.compareTo(b).sign())
        }
    }

    @Test
    fun neg() {
        val cases =
            listOf(
                UtcOffset.UTC to UtcOffset.UTC,
                offset(0, 0, 1) to offset(0, 0, -1),
                offset(0, 0, -1) to offset(0, 0, 1),
                offset(1) to offset(-1),
                offset(-1) to offset(1),
                offset(23, 59) to offset(-23, -59),
                offset(-23, -59) to offset(23, 59),
                offset(23, 59, 59) to offset(-23, -59, -59),
                offset(-23, -59, -59) to offset(23, 59, 59),
            )

        for ((offset, expected) in cases) {
            assertEquals(expected, -offset)
        }
    }

    private data class HmsCase(
        val hours: Int,
        val minutes: Int,
        val seconds: Int,
        val expected: UtcOffset,
    )

    private data class CompareCase(
        val a: UtcOffset,
        val b: UtcOffset,
        val expectedSign: Int,
    )

    private fun offset(hours: Int, minutes: Int = 0, seconds: Int = 0): UtcOffset =
        UtcOffset.fromHms(hours, minutes, seconds).getOrThrow()

    private fun Int.sign(): Int =
        when {
            this < 0 -> -1
            this > 0 -> 1
            else -> 0
        }
}
