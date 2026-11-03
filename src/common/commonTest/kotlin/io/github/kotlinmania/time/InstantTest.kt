// port-lint: tests tests/integration/instant.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.ext.milliseconds
import io.github.kotlinmania.time.ext.seconds
import io.github.kotlinmania.time.ext.stdMilliseconds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.TimeSource

class InstantTest {
    @Test
    fun elapsed() {
        val instant = Instant.now()
        val elapsed = instant.elapsed()
        assertTrue(elapsed >= Duration.ZERO)
    }

    @Test
    fun checkedAdd() {
        val now = Instant.now()
        assertEquals(now, now.checkedAdd(0.seconds()))
        assertEquals(now + 5.seconds(), now.checkedAdd(5.seconds()))
        assertEquals(now + (-5).seconds(), now.checkedAdd((-5).seconds()))
    }

    @Test
    fun checkedSub() {
        val now = Instant.now()
        assertEquals(now, now.checkedSub(0.seconds()))
        assertEquals(now - 5.seconds(), now.checkedSub(5.seconds()))
        assertEquals(now - (-5).seconds(), now.checkedSub((-5).seconds()))
    }

    @Test
    fun intoInner() {
        val now = Instant.now()
        assertEquals(now.inner, now.intoInner())
    }

    @Test
    fun stdFrom() {
        val nowTime = Instant.now()
        val nowStd = Instant.toStdInstant(nowTime)
        assertTrue(nowTime.eq(nowStd))
    }

    @Test
    fun fromStd() {
        val nowStd = TimeSource.Monotonic.markNow()
        val nowTime = Instant.from(nowStd)
        assertTrue(nowTime.eq(nowStd))
    }

    @Test
    fun sub() {
        val start = Instant.now()
        assertTrue(Instant.now() - start >= Duration.ZERO)
        assertEquals(Duration.ZERO, start - start)
    }

    @Test
    fun subStd() {
        val start = TimeSource.Monotonic.markNow()
        assertTrue(Instant.now() - start >= Duration.ZERO)
    }

    @Test
    fun stdSub() {
        val start = Instant.now()
        assertTrue(TimeSource.Monotonic.markNow() - start >= Duration.ZERO)
    }

    @Test
    fun addDuration() {
        val start = Instant.now()
        assertTrue(start + 0.seconds() <= Instant.now())
        assertTrue(start - 1.milliseconds() <= Instant.now())
    }

    @Test
    fun stdAddDuration() {
        val start = TimeSource.Monotonic.markNow()
        assertTrue(start - 1.milliseconds() <= TimeSource.Monotonic.markNow())
    }

    @Test
    fun addStdDuration() {
        val start = Instant.now()
        assertTrue((start - 1.milliseconds()) + 1uL.stdMilliseconds() <= Instant.now())
    }

    @Test
    fun addAssignDuration() {
        var start = Instant.now() - 1.milliseconds()
        start += 1.milliseconds()
        assertTrue(start <= Instant.now())
    }

    @Test
    fun stdAddAssignDuration() {
        var start = TimeSource.Monotonic.markNow() - 1.milliseconds()
        start += 1.milliseconds()
        assertTrue(start <= TimeSource.Monotonic.markNow())
    }

    @Test
    fun addAssignStdDuration() {
        var start = Instant.now() - 1.milliseconds()
        start += 1uL.stdMilliseconds()
        assertTrue(start <= Instant.now())
    }

    @Test
    fun subDuration() {
        val instant = Instant.now()
        assertTrue(instant - 100.milliseconds() <= Instant.now())
        assertEquals(instant, instant - Duration.ZERO)
    }

    @Test
    fun stdSubDuration() {
        val instant = TimeSource.Monotonic.markNow()
        assertTrue(instant - 100.milliseconds() <= TimeSource.Monotonic.markNow())
    }

    @Test
    fun subStdDuration() {
        val instant = Instant.now()
        assertTrue(instant - 100uL.stdMilliseconds() <= Instant.now())
    }

    @Test
    fun subAssignDuration() {
        var instant = Instant.now()
        instant -= 100.milliseconds()
        assertTrue(instant <= Instant.now())
    }

    @Test
    fun stdSubAssignDuration() {
        var instant: kotlin.time.ComparableTimeMark = TimeSource.Monotonic.markNow()
        instant -= 100.milliseconds()
        assertTrue(instant <= TimeSource.Monotonic.markNow())
    }

    @Test
    fun subAssignStdDuration() {
        var instant = Instant.now()
        instant -= 100uL.stdMilliseconds()
        assertTrue(instant <= Instant.now())
    }

    @Test
    fun eqStd() {
        val nowTime = Instant.now()
        val nowStd = Instant.toStdInstant(nowTime)
        assertTrue(nowTime.eq(nowStd))
    }

    @Test
    fun stdEq() {
        val nowTime = Instant.now()
        val nowStd = Instant.toStdInstant(nowTime)
        assertTrue(nowTime.equals(nowStd))
    }

    @Test
    fun ord() {
        val nowTime = Instant.now()
        val nowStd = nowTime + 1.seconds()
        assertTrue(nowTime.compareTo(nowStd) < 0)

        val nowTime2 = Instant.now()
        val nowStd2 = nowTime2 - 1.seconds()
        assertTrue(nowTime2.compareTo(nowStd2) > 0)
    }

    @Test
    fun partialOrdStd() {
        val nowTime = Instant.now()
        val nowStd = Instant.toStdInstant(nowTime) + 1.seconds()
        assertTrue(nowTime < nowStd)

        val nowTime2 = Instant.now()
        val nowStd2 = Instant.toStdInstant(nowTime2) - 1.seconds()
        assertTrue(nowTime2 > nowStd2)
    }

    @Test
    fun stdPartialOrd() {
        val nowTime = Instant.now()
        val nowStd = Instant.toStdInstant(nowTime) + 1.seconds()
        assertTrue(nowStd > nowTime)

        val nowTime2 = Instant.now()
        val nowStd2 = Instant.toStdInstant(nowTime2) - 1.seconds()
        assertTrue(nowStd2 < nowTime2)
    }

    @Test
    fun subRegression() {
        val now = Instant.now()
        val future = now + Duration.seconds(5)
        val past = now - Duration.seconds(5)

        assertEquals(Duration.seconds(5), future - now)
        assertEquals(Duration.seconds(5), now - past)
        assertEquals(Duration.seconds(10), future - past)

        assertEquals(Duration.seconds(-5), now - future)
        assertEquals(Duration.seconds(-5), past - now)
        assertEquals(Duration.seconds(-10), past - future)
    }

    @Test
    fun asRef() {
        val now = Instant.now()
        assertEquals(now.asRef(), now.asRef())
    }

    @Test
    fun borrow() {
        val now = Instant.now()
        assertEquals(now.borrow(), now.borrow())
    }
}
