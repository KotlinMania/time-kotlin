// port-lint: tests tests/integration/time.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.ext.hours
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimeTest {
    @Test
    fun fromHms() {
        val time = Time.fromHms(1, 2, 3).getOrThrow()
        assertEquals(1, time.hour())
        assertEquals(2, time.minute())
        assertEquals(3, time.second())
        assertEquals(0, time.nanosecond())

        assertTrue(Time.fromHms(24, 0, 0).isFailure)
        assertTrue(Time.fromHms(0, 60, 0).isFailure)
        assertTrue(Time.fromHms(0, 0, 60).isFailure)
    }

    @Test
    fun fromHmsMilli() {
        val time = Time.fromHmsMilli(1, 2, 3, 4).getOrThrow()
        assertEquals(1, time.hour())
        assertEquals(2, time.minute())
        assertEquals(3, time.second())
        assertEquals(4, time.millisecond())
        assertEquals(4_000_000, time.nanosecond())

        assertTrue(Time.fromHmsMilli(24, 0, 0, 0).isFailure)
        assertTrue(Time.fromHmsMilli(0, 60, 0, 0).isFailure)
        assertTrue(Time.fromHmsMilli(0, 0, 60, 0).isFailure)
        assertTrue(Time.fromHmsMilli(0, 0, 0, 1_000).isFailure)
    }

    @Test
    fun fromHmsMicro() {
        val time = Time.fromHmsMicro(1, 2, 3, 4).getOrThrow()
        assertEquals(1, time.hour())
        assertEquals(2, time.minute())
        assertEquals(3, time.second())
        assertEquals(4, time.microsecond())
        assertEquals(4_000, time.nanosecond())

        assertTrue(Time.fromHmsMicro(24, 0, 0, 0).isFailure)
        assertTrue(Time.fromHmsMicro(0, 60, 0, 0).isFailure)
        assertTrue(Time.fromHmsMicro(0, 0, 60, 0).isFailure)
        assertTrue(Time.fromHmsMicro(0, 0, 0, 1_000_000).isFailure)
    }

    @Test
    fun fromHmsNano() {
        val time = Time.fromHmsNano(1, 2, 3, 4).getOrThrow()
        assertEquals(1, time.hour())
        assertEquals(2, time.minute())
        assertEquals(3, time.second())
        assertEquals(4, time.nanosecond())

        assertTrue(Time.fromHmsNano(24, 0, 0, 0).isFailure)
        assertTrue(Time.fromHmsNano(0, 60, 0, 0).isFailure)
        assertTrue(Time.fromHmsNano(0, 0, 60, 0).isFailure)
        assertTrue(Time.fromHmsNano(0, 0, 0, 1_000_000_000).isFailure)
    }

    @Test
    fun asHms() {
        val time = Time.fromHms(1, 2, 3).getOrThrow()
        assertEquals(Triple(1, 2, 3), time.asHms())
    }

    @Test
    fun asHmsMilli() {
        val time = Time.fromHmsMilli(1, 2, 3, 4).getOrThrow()
        assertEquals(listOf(1, 2, 3, 4), time.asHmsMilli())
    }

    @Test
    fun asHmsMicro() {
        val time = Time.fromHmsMicro(1, 2, 3, 4_005).getOrThrow()
        assertEquals(listOf(1, 2, 3, 4_005), time.asHmsMicro())
    }

    @Test
    fun asHmsNano() {
        val time = Time.fromHmsNano(1, 2, 3, 4_005_006).getOrThrow()
        assertEquals(listOf(1, 2, 3, 4_005_006), time.asHmsNano())
    }

    @Test
    fun hour() {
        for (h in 0 until 24) {
            assertEquals(h, Time.fromHms(h, 0, 0).getOrThrow().hour())
            assertEquals(h, Time.fromHms(h, 59, 59).getOrThrow().hour())
        }
    }

    @Test
    fun minute() {
        for (m in 0 until 60) {
            assertEquals(m, Time.fromHms(0, m, 0).getOrThrow().minute())
            assertEquals(m, Time.fromHms(23, m, 59).getOrThrow().minute())
        }
    }

    @Test
    fun second() {
        for (s in 0 until 60) {
            assertEquals(s, Time.fromHms(0, 0, s).getOrThrow().second())
            assertEquals(s, Time.fromHms(23, 59, s).getOrThrow().second())
        }
    }

    @Test
    fun durationUntil() {
        val t18 = Time.fromHms(18, 0, 0).getOrThrow()
        assertEquals(6.hours(), t18.durationUntil(Time.MIDNIGHT))
        val t23 = Time.fromHms(23, 0, 0).getOrThrow()
        val t1 = Time.fromHms(1, 0, 0).getOrThrow()
        assertEquals(2.hours(), t23.durationUntil(t1))
    }

    @Test
    fun durationSince() {
        val t18 = Time.fromHms(18, 0, 0).getOrThrow()
        assertEquals(6.hours(), Time.MIDNIGHT.durationSince(t18))
        val t23 = Time.fromHms(23, 0, 0).getOrThrow()
        val t1 = Time.fromHms(1, 0, 0).getOrThrow()
        assertEquals(2.hours(), t1.durationSince(t23))
    }

    @Test
    fun replacing() {
        val t = Time.fromHmsNano(1, 2, 3, 4_005_006).getOrThrow()
        assertEquals(7, t.replaceHour(7).getOrThrow().hour())
        assertEquals(7, t.replaceMinute(7).getOrThrow().minute())
        assertEquals(7, t.replaceSecond(7).getOrThrow().second())
        assertEquals(7, t.replaceMillisecond(7).getOrThrow().millisecond())
        assertEquals(7_008, t.replaceMicrosecond(7_008).getOrThrow().microsecond())
        assertEquals(7_008_009, t.replaceNanosecond(7_008_009).getOrThrow().nanosecond())
    }

    @Test
    fun truncating() {
        val t = Time.fromHmsNano(1, 2, 3, 4_005_006).getOrThrow()
        assertEquals(Time.fromHms(1, 0, 0).getOrThrow(), t.truncateToHour())
        assertEquals(Time.fromHms(1, 2, 0).getOrThrow(), t.truncateToMinute())
        assertEquals(Time.fromHms(1, 2, 3).getOrThrow(), t.truncateToSecond())
        assertEquals(Time.fromHmsNano(1, 2, 3, 4_000_000).getOrThrow(), t.truncateToMillisecond())
        assertEquals(Time.fromHmsNano(1, 2, 3, 4_005_000).getOrThrow(), t.truncateToMicrosecond())
    }

    @Test
    fun ordering() {
        val t1 = Time.fromHms(1, 0, 0).getOrThrow()
        val t2 = Time.fromHms(2, 0, 0).getOrThrow()
        assertTrue(t1 < t2)
        assertFalse(t1 > t2)
        assertEquals(0, t1.compareTo(t1))
    }

    @Test
    fun display() {
        assertEquals("00:00:00", Time.MIDNIGHT.fmt())
        assertEquals("01:02:03", Time.fromHms(1, 2, 3).getOrThrow().fmt())
        assertEquals("01:02:03.004", Time.fromHmsMilli(1, 2, 3, 4).getOrThrow().fmt())
    }
}
