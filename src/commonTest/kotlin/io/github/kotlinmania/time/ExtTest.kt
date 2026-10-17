// port-lint: tests tests/integration/ext.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.ext.days
import io.github.kotlinmania.time.ext.hours
import io.github.kotlinmania.time.ext.microseconds
import io.github.kotlinmania.time.ext.milliseconds
import io.github.kotlinmania.time.ext.minutes
import io.github.kotlinmania.time.ext.nanoseconds
import io.github.kotlinmania.time.ext.seconds
import io.github.kotlinmania.time.ext.stdDays
import io.github.kotlinmania.time.ext.stdHours
import io.github.kotlinmania.time.ext.stdMicroseconds
import io.github.kotlinmania.time.ext.stdMilliseconds
import io.github.kotlinmania.time.ext.stdMinutes
import io.github.kotlinmania.time.ext.stdNanoseconds
import io.github.kotlinmania.time.ext.stdSeconds
import io.github.kotlinmania.time.ext.stdWeeks
import io.github.kotlinmania.time.ext.weeks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.microseconds as ktMicroseconds
import kotlin.time.Duration.Companion.milliseconds as ktMilliseconds
import kotlin.time.Duration.Companion.nanoseconds as ktNanoseconds
import kotlin.time.Duration.Companion.seconds as ktSeconds

class ExtTest {
    @Test
    fun numericalDurationUnsigned() {
        assertEquals(Duration.nanoseconds(5), 5uL.nanoseconds())
        assertEquals(Duration.microseconds(5), 5uL.microseconds())
        assertEquals(Duration.milliseconds(5), 5uL.milliseconds())
        assertEquals(Duration.seconds(5), 5uL.seconds())
        assertEquals(Duration.minutes(5), 5uL.minutes())
        assertEquals(Duration.hours(5), 5uL.hours())
        assertEquals(Duration.days(5), 5uL.days())
        assertEquals(Duration.weeks(5), 5uL.weeks())
    }

    @Test
    fun numericalDurationSigned() {
        assertEquals(Duration.nanoseconds(-5), (-5).nanoseconds())
        assertEquals(Duration.microseconds(-5), (-5).microseconds())
        assertEquals(Duration.milliseconds(-5), (-5).milliseconds())
        assertEquals(Duration.seconds(-5), (-5).seconds())
        assertEquals(Duration.minutes(-5), (-5).minutes())
        assertEquals(Duration.hours(-5), (-5).hours())
        assertEquals(Duration.days(-5), (-5).days())
        assertEquals(Duration.weeks(-5), (-5).weeks())
    }

    @Test
    fun numericalDurationFloat() {
        assertEquals(Duration.nanoseconds(1), 1.9.nanoseconds())

        assertEquals(Duration.nanoseconds(1), 1.0.nanoseconds())
        assertEquals(Duration.microseconds(1), 1.0.microseconds())
        assertEquals(Duration.milliseconds(1), 1.0.milliseconds())
        assertEquals(Duration.seconds(1), 1.0.seconds())
        assertEquals(Duration.minutes(1), 1.0.minutes())
        assertEquals(Duration.hours(1), 1.0.hours())
        assertEquals(Duration.days(1), 1.0.days())
        assertEquals(Duration.weeks(1), 1.0.weeks())

        assertEquals(Duration.nanoseconds(1), 1.5.nanoseconds())
        assertEquals(Duration.nanoseconds(1_500), 1.5.microseconds())
        assertEquals(Duration.microseconds(1_500), 1.5.milliseconds())
        assertEquals(Duration.milliseconds(1_500), 1.5.seconds())
        assertEquals(Duration.seconds(90), 1.5.minutes())
        assertEquals(Duration.minutes(90), 1.5.hours())
        assertEquals(Duration.hours(36), 1.5.days())
        assertEquals(Duration.hours(252), 1.5.weeks())
    }

    @Test
    fun numericalDurationArithmetic() {
        assertEquals(2_500.milliseconds(), 2.seconds() + 500.milliseconds())
        assertEquals(1_500.milliseconds(), 2.seconds() - 500.milliseconds())
    }

    @Test
    fun numericalStdDurationUnsigned() {
        assertEquals(5L.ktNanoseconds, 5uL.stdNanoseconds())
        assertEquals(5L.ktMicroseconds, 5uL.stdMicroseconds())
        assertEquals(5L.ktMilliseconds, 5uL.stdMilliseconds())
        assertEquals(5L.ktSeconds, 5uL.stdSeconds())
        assertEquals((5L * 60).ktSeconds, 5uL.stdMinutes())
        assertEquals((5L * 3_600).ktSeconds, 5uL.stdHours())
        assertEquals((5L * 86_400).ktSeconds, 5uL.stdDays())
        assertEquals((5L * 604_800).ktSeconds, 5uL.stdWeeks())
    }

    @Test
    fun numericalStdDurationFloat() {
        assertEquals(1L.ktNanoseconds, 1.9.stdNanoseconds())

        assertEquals(1L.ktNanoseconds, 1.0.stdNanoseconds())
        assertEquals(1L.ktMicroseconds, 1.0.stdMicroseconds())
        assertEquals(1L.ktMilliseconds, 1.0.stdMilliseconds())
        assertEquals(1L.ktSeconds, 1.0.stdSeconds())
        assertEquals(60L.ktSeconds, 1.0.stdMinutes())
        assertEquals(3_600L.ktSeconds, 1.0.stdHours())
        assertEquals(86_400L.ktSeconds, 1.0.stdDays())
        assertEquals(604_800L.ktSeconds, 1.0.stdWeeks())

        assertEquals(1L.ktNanoseconds, 1.5.stdNanoseconds())
        assertEquals(1_500L.ktNanoseconds, 1.5.stdMicroseconds())
        assertEquals(1_500L.ktMicroseconds, 1.5.stdMilliseconds())
        assertEquals(1_500L.ktMilliseconds, 1.5.stdSeconds())
        assertEquals(90L.ktSeconds, 1.5.stdMinutes())
        assertEquals((90L * 60).ktSeconds, 1.5.stdHours())
        assertEquals((36L * 3_600).ktSeconds, 1.5.stdDays())
        assertEquals((252L * 3_600).ktSeconds, 1.5.stdWeeks())
    }

    @Test
    fun numericalStdDurationArithmetic() {
        assertEquals(2_500uL.stdMilliseconds(), 2uL.stdSeconds() + 500uL.stdMilliseconds())
        assertEquals(1_500uL.stdMilliseconds(), 2uL.stdSeconds() - 500uL.stdMilliseconds())
    }
}
