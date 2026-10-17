// port-lint: source instant.rs
package io.github.kotlinmania.time

import kotlin.time.TimeSource

/**
 * A measurement of a monotonically non-decreasing clock. Opaque and useful only with [Duration].
 */
class Instant internal constructor(
    private val nanos: Long,
) : Comparable<Instant> {
    companion object {
        private val baseMark = TimeSource.Monotonic.markNow()

        /** Returns an [Instant] corresponding to "now". */
        fun now(): Instant =
            Instant(baseMark.elapsedNow().inWholeNanoseconds)
    }

    /**
     * Returns the amount of time elapsed since this instant was created.
     */
    fun elapsed(): Duration = now() - this

    /**
     * Returns Some(t) where t is self + duration if it fits, null otherwise.
     */
    fun checkedAdd(duration: Duration): Instant? {
        val durNanos = duration.wholeNanoseconds()
        val result = nanos + durNanos
        // Overflow check
        if ((nanos xor result) and (durNanos xor result) < 0) return null
        return Instant(result)
    }

    /**
     * Returns Some(t) where t is self - duration if it fits, null otherwise.
     */
    fun checkedSub(duration: Duration): Instant? {
        val durNanos = duration.wholeNanoseconds()
        val result = nanos - durNanos
        // Overflow check
        if ((nanos xor result) and (-durNanos xor result) < 0) return null
        return Instant(result)
    }

    operator fun plus(duration: Duration): Instant =
        checkedAdd(duration) ?: throw ArithmeticException("overflow adding duration to instant")

    operator fun minus(duration: Duration): Instant =
        checkedSub(duration) ?: throw ArithmeticException("overflow subtracting duration from instant")

    operator fun minus(other: Instant): Duration =
        Duration.nanoseconds(nanos - other.nanos)

    override fun compareTo(other: Instant): Int = nanos.compareTo(other.nanos)

    override fun equals(other: Any?): Boolean = other is Instant && nanos == other.nanos

    override fun hashCode(): Int = nanos.hashCode()

    override fun toString(): String = "Instant($nanos ns)"
}
