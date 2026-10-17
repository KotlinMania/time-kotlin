// port-lint: tests tests/integration/duration.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.ext.days
import io.github.kotlinmania.time.ext.hours
import io.github.kotlinmania.time.ext.microseconds
import io.github.kotlinmania.time.ext.milliseconds
import io.github.kotlinmania.time.ext.minutes
import io.github.kotlinmania.time.ext.nanoseconds
import io.github.kotlinmania.time.ext.seconds
import io.github.kotlinmania.time.ext.weeks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DurationTest {

    @Test
    fun unitValues() {
        assertEquals(Duration.ZERO, 0.seconds())
        assertEquals(Duration.NANOSECOND, 1.nanoseconds())
        assertEquals(Duration.MICROSECOND, 1.microseconds())
        assertEquals(Duration.MILLISECOND, 1.milliseconds())
        assertEquals(Duration.SECOND, 1.seconds())
        assertEquals(Duration.MINUTE, 60.seconds())
        assertEquals(Duration.HOUR, 3_600.seconds())
        assertEquals(Duration.DAY, 86_400.seconds())
        assertEquals(Duration.WEEK, 604_800.seconds())
    }

    @Test
    fun default() {
        assertEquals(Duration.default(), Duration.ZERO)
    }

    @Test
    fun isZero() {
        assertFalse((-1).seconds().isZero())
        assertTrue(0.seconds().isZero())
        assertFalse(1.seconds().isZero())
    }

    @Test
    fun isNegative() {
        assertTrue((-1).seconds().isNegative())
        assertFalse(0.seconds().isNegative())
        assertFalse(1.seconds().isNegative())
    }

    @Test
    fun isPositive() {
        assertFalse((-1).seconds().isPositive())
        assertFalse(0.seconds().isPositive())
        assertTrue(1.seconds().isPositive())
    }

    @Test
    fun abs() {
        assertEquals(1.seconds(), 1.seconds().abs())
        assertEquals(0.seconds(), 0.seconds().abs())
        assertEquals(1.seconds(), (-1).seconds().abs())
        assertEquals(Duration.MAX, Duration.new(Long.MIN_VALUE, 0).abs())
    }

    @Test
    fun new() {
        assertEquals(1.seconds(), Duration.new(1, 0))
        assertEquals((-1).seconds(), Duration.new(-1, 0))
        assertEquals(3.seconds(), Duration.new(1, 2_000_000_000))
        assertEquals(0.seconds(), Duration.new(0, 0))
        assertEquals(1.seconds(), Duration.new(0, 1_000_000_000))
        assertEquals(0.seconds(), Duration.new(-1, 1_000_000_000))
        assertEquals((-1).seconds(), Duration.new(-2, 1_000_000_000))
        assertEquals(999_999_999.nanoseconds(), Duration.new(1, -1))
        assertEquals((-999_999_999).nanoseconds(), Duration.new(-1, 1))
        assertEquals(1_000_000_001.nanoseconds(), Duration.new(1, 1))
        assertEquals((-1_000_000_001).nanoseconds(), Duration.new(-1, -1))
        assertEquals(1.nanoseconds(), Duration.new(0, 1))
        assertEquals((-1).nanoseconds(), Duration.new(0, -1))
        assertEquals(400.milliseconds(), Duration.new(-1, 1_400_000_000))
        assertEquals((-600).milliseconds(), Duration.new(-2, 1_400_000_000))
        assertEquals((-1_600).milliseconds(), Duration.new(-3, 1_400_000_000))
        assertEquals((-400).milliseconds(), Duration.new(1, -1_400_000_000))
        assertEquals(600.milliseconds(), Duration.new(2, -1_400_000_000))
        assertEquals(1_600.milliseconds(), Duration.new(3, -1_400_000_000))
    }

    @Test
    fun newPanic() {
        assertFailsWith<ArithmeticException> {
            Duration.new(Long.MAX_VALUE, 1_000_000_000)
        }
        assertFailsWith<ArithmeticException> {
            Duration.new(Long.MIN_VALUE, -1_000_000_000)
        }
    }

    @Test
    fun weeks() {
        assertEquals(1.weeks(), Duration.weeks(1))
        assertEquals((-1).weeks(), Duration.weeks(-1))
        assertEquals(604_800.seconds(), Duration.weeks(1))
    }

    @Test
    fun days() {
        assertEquals(1.days(), Duration.days(1))
        assertEquals((-1).days(), Duration.days(-1))
        assertEquals(86_400.seconds(), Duration.days(1))
    }

    @Test
    fun hours() {
        assertEquals(1.hours(), Duration.hours(1))
        assertEquals((-1).hours(), Duration.hours(-1))
        assertEquals(3_600.seconds(), Duration.hours(1))
    }

    @Test
    fun minutes() {
        assertEquals(1.minutes(), Duration.minutes(1))
        assertEquals((-1).minutes(), Duration.minutes(-1))
        assertEquals(60.seconds(), Duration.minutes(1))
    }

    @Test
    fun seconds() {
        assertEquals(1.seconds(), Duration.seconds(1))
        assertEquals((-1).seconds(), Duration.seconds(-1))
        assertEquals(1.seconds().wholeSeconds(), 1L)
    }

    @Test
    fun secondsF64() {
        assertEquals(500.milliseconds(), Duration.secondsF64(0.5))
        assertEquals((-500).milliseconds(), Duration.secondsF64(-0.5))
        assertEquals(123_250.milliseconds(), Duration.secondsF64(123.250))
        assertEquals(Duration.ZERO, Duration.secondsF64(0.000_000_000_012))

        assertFailsWith<IllegalArgumentException> {
            Duration.secondsF64(Double.NaN)
        }
        assertFailsWith<ArithmeticException> {
            Duration.secondsF64(Double.POSITIVE_INFINITY)
        }
        assertFailsWith<ArithmeticException> {
            Duration.secondsF64(Double.NEGATIVE_INFINITY)
        }
    }

    @Test
    fun saturatingSecondsF64() {
        assertEquals(500.milliseconds(), Duration.saturatingSecondsF64(0.5))
        assertEquals((-500).milliseconds(), Duration.saturatingSecondsF64(-0.5))
        assertEquals(123_250.milliseconds(), Duration.saturatingSecondsF64(123.250))
        assertEquals(Duration.ZERO, Duration.saturatingSecondsF64(0.000_000_000_012))
        assertEquals(Duration.MAX, Duration.saturatingSecondsF64(Double.MAX_VALUE))
        assertEquals(Duration.MIN, Duration.saturatingSecondsF64(-Double.MAX_VALUE))
        assertEquals(Duration.ZERO, Duration.saturatingSecondsF64(Double.NaN))
    }

    @Test
    fun checkedSecondsF64() {
        assertEquals(0.5.seconds(), Duration.checkedSecondsF64(0.5))
        assertEquals((-0.5).seconds(), Duration.checkedSecondsF64(-0.5))
        assertEquals(123.250.seconds(), Duration.checkedSecondsF64(123.250))
        assertNull(Duration.checkedSecondsF64(Double.NaN))
        assertNull(Duration.checkedSecondsF64(Double.POSITIVE_INFINITY))
        assertNull(Duration.checkedSecondsF64(Double.NEGATIVE_INFINITY))
    }

    @Test
    fun milliseconds() {
        assertEquals(1_000.microseconds(), Duration.milliseconds(1))
        assertEquals((-1_000).microseconds(), Duration.milliseconds(-1))
    }

    @Test
    fun microseconds() {
        assertEquals(1_000.nanoseconds(), Duration.microseconds(1))
        assertEquals((-1_000).nanoseconds(), Duration.microseconds(-1))
    }

    @Test
    fun nanoseconds() {
        assertEquals(1.microseconds() / 1_000, Duration.nanoseconds(1))
        assertEquals((-1).microseconds() / 1_000, Duration.nanoseconds(-1))
    }

    @Test
    fun wholeWeeks() {
        assertEquals(1L, 1.weeks().wholeWeeks())
        assertEquals(-1L, (-1).weeks().wholeWeeks())
        assertEquals(0L, 6.days().wholeWeeks())
        assertEquals(0L, (-6).days().wholeWeeks())
    }

    @Test
    fun wholeDays() {
        assertEquals(1L, 1.days().wholeDays())
        assertEquals(-1L, (-1).days().wholeDays())
        assertEquals(0L, 23.hours().wholeDays())
        assertEquals(0L, (-23).hours().wholeDays())
    }

    @Test
    fun wholeHours() {
        assertEquals(1L, 1.hours().wholeHours())
        assertEquals(-1L, (-1).hours().wholeHours())
        assertEquals(0L, 59.minutes().wholeHours())
        assertEquals(0L, (-59).minutes().wholeHours())
    }

    @Test
    fun wholeMinutes() {
        assertEquals(1L, 1.minutes().wholeMinutes())
        assertEquals(-1L, (-1).minutes().wholeMinutes())
        assertEquals(0L, 59.seconds().wholeMinutes())
        assertEquals(0L, (-59).seconds().wholeMinutes())
    }

    @Test
    fun wholeSeconds() {
        assertEquals(1L, 1.seconds().wholeSeconds())
        assertEquals(-1L, (-1).seconds().wholeSeconds())
        assertEquals(60L, 1.minutes().wholeSeconds())
        assertEquals(-60L, (-1).minutes().wholeSeconds())
    }

    @Test
    fun subsecMilliseconds() {
        assertEquals(400.toShort(), 1.4.seconds().subsecMilliseconds())
        assertEquals((-400).toShort(), (-1.4).seconds().subsecMilliseconds())
    }

    @Test
    fun subsecMicroseconds() {
        assertEquals(400, 1.0004.seconds().subsecMicroseconds())
        assertEquals(-400, (-1.0004).seconds().subsecMicroseconds())
    }

    @Test
    fun subsecNanoseconds() {
        assertEquals(400, 1.000_000_400.seconds().subsecNanoseconds())
        assertEquals(-400, (-1.000_000_400).seconds().subsecNanoseconds())
    }

    @Test
    fun checkedAdd() {
        assertEquals(10.seconds(), 5.seconds().checkedAdd(5.seconds()))
        assertNull(Duration.MAX.checkedAdd(1.nanoseconds()))
        assertEquals(0.seconds(), (-5).seconds().checkedAdd(5.seconds()))
    }

    @Test
    fun checkedSub() {
        assertEquals(Duration.ZERO, 5.seconds().checkedSub(5.seconds()))
        assertNull(Duration.MIN.checkedSub(1.nanoseconds()))
        assertEquals((-5).seconds(), 5.seconds().checkedSub(10.seconds()))
    }

    @Test
    fun checkedMul() {
        assertEquals(10.seconds(), 5.seconds().checkedMul(2))
        assertEquals((-10).seconds(), 5.seconds().checkedMul(-2))
        assertEquals(0.seconds(), 5.seconds().checkedMul(0))
        assertNull(Duration.MAX.checkedMul(2))
        assertNull(Duration.MIN.checkedMul(2))
    }

    @Test
    fun checkedDiv() {
        assertEquals(5.seconds(), 10.seconds().checkedDiv(2))
        assertEquals((-5).seconds(), 10.seconds().checkedDiv(-2))
        assertNull(1.seconds().checkedDiv(0))
    }

    @Test
    fun checkedNeg() {
        assertEquals((-5).seconds(), 5.seconds().checkedNeg())
        assertNull(Duration.MIN.checkedNeg())
    }

    @Test
    fun saturatingAdd() {
        assertEquals(10.seconds(), 5.seconds().saturatingAdd(5.seconds()))
        assertEquals(Duration.MAX, Duration.MAX.saturatingAdd(1.nanoseconds()))
        assertEquals(Duration.MIN, Duration.MIN.saturatingAdd((-1).nanoseconds()))
        assertEquals(Duration.ZERO, (-5).seconds().saturatingAdd(5.seconds()))
    }

    @Test
    fun saturatingSub() {
        assertEquals(Duration.ZERO, 5.seconds().saturatingSub(5.seconds()))
        assertEquals(Duration.MIN, Duration.MIN.saturatingSub(1.nanoseconds()))
        assertEquals(Duration.MAX, Duration.MAX.saturatingSub((-1).nanoseconds()))
        assertEquals((-5).seconds(), 5.seconds().saturatingSub(10.seconds()))
    }

    @Test
    fun saturatingMul() {
        assertEquals(10.seconds(), 5.seconds().saturatingMul(2))
        assertEquals((-10).seconds(), 5.seconds().saturatingMul(-2))
        assertEquals(Duration.ZERO, 5.seconds().saturatingMul(0))
        assertEquals(Duration.MAX, Duration.MAX.saturatingMul(2))
        assertEquals(Duration.MIN, Duration.MIN.saturatingMul(2))
        assertEquals(Duration.MIN, Duration.MAX.saturatingMul(-2))
        assertEquals(Duration.MAX, Duration.MIN.saturatingMul(-2))
    }

    @Test
    fun operators() {
        assertEquals(10.seconds(), 5.seconds() + 5.seconds())
        assertEquals(0.seconds(), 5.seconds() - 5.seconds())
        assertEquals(10.seconds(), 5.seconds() * 2)
        assertEquals(5.seconds(), 10.seconds() / 2)
        assertEquals((-5).seconds(), -(5.seconds()))
        assertEquals(2.0, 10.seconds() / 5.seconds())
    }

    @Test
    fun ordering() {
        assertTrue(5.seconds() < 10.seconds())
        assertTrue(10.seconds() > 5.seconds())
        assertTrue(5.seconds() <= 5.seconds())
        assertEquals(0, 5.seconds().compareTo(5.seconds()))
    }

    @Test
    fun display() {
        assertEquals("0s", Duration.ZERO.fmt())
        assertEquals("1s", 1.seconds().fmt())
        assertEquals("-1s", (-1).seconds().fmt())
        assertEquals("1m1s", 61.seconds().fmt())
        assertEquals("1h1m1s", 3661.seconds().fmt())
        assertEquals("1d", 1.days().fmt())
        assertEquals("1d1ms", (1.days() + 1.milliseconds()).fmt())
        assertEquals("1ms", 1.milliseconds().fmt())
        assertEquals("1µs", 1.microseconds().fmt())
        assertEquals("1ns", 1.nanoseconds().fmt())
    }
}
