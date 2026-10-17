package io.github.kotlinmania.time

import io.github.kotlinmania.time.ext.seconds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InstantTest {
    @Test
    fun nowAndElapsed() {
        val i1 = Instant.now()
        val elapsed = i1.elapsed()
        assertTrue(elapsed.isPositive() || elapsed.isZero())

        val i2 = Instant.now()
        assertTrue(i2 >= i1)
    }

    @Test
    fun arithmetic() {
        val i = Instant.now()
        val later = i + 5.seconds()
        val diff = later - i
        assertEquals(5.seconds(), diff)

        val before = later - 5.seconds()
        assertEquals(i, before)
    }
}
