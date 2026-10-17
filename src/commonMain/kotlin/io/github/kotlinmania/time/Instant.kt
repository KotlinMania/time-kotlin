// port-lint: source instant.rs
package io.github.kotlinmania.time

import kotlin.time.ComparableTimeMark as StdInstant
import kotlin.time.Duration as StdDuration
import kotlin.time.Duration.Companion.nanoseconds as stdNanoseconds
import kotlin.time.TimeSource

/**
 * A measurement of a monotonically non-decreasing clock. Opaque and useful only with [Duration].
 */
class Instant(
    val inner: StdInstant,
) : Comparable<Instant> {
    internal constructor(nanos: Long) : this(baseMark + nanos.stdNanoseconds)

    companion object {
        internal val baseMark: StdInstant = TimeSource.Monotonic.markNow()

        /** Returns an [Instant] corresponding to "now". */
        fun now(): Instant = Instant(TimeSource.Monotonic.markNow())

        fun from(instant: StdInstant): Instant = Instant(instant)

        fun toStdInstant(instant: Instant): StdInstant = instant.inner
    }

    /**
     * Returns the amount of time elapsed since this instant was created.
     */
    fun elapsed(): Duration = now() - this

    /**
     * Returns `t` where `t` is the time `self + duration` if `t` can be represented as
     * [Instant], `null` otherwise.
     */
    fun checkedAdd(duration: Duration): Instant? =
        when {
            duration.isZero() -> this
            duration.isPositive() -> Instant(inner + duration.unsignedAbs())
            else -> Instant(inner - duration.unsignedAbs())
        }

    /**
     * Returns `t` where `t` is the time `self - duration` if `t` can be represented as
     * [Instant], `null` otherwise.
     */
    fun checkedSub(duration: Duration): Instant? =
        when {
            duration.isZero() -> this
            duration.isPositive() -> Instant(inner - duration.unsignedAbs())
            else -> Instant(inner + duration.unsignedAbs())
        }

    /** Obtain the inner [StdInstant]. */
    fun intoInner(): StdInstant = inner

    fun asRef(): StdInstant = inner

    fun borrow(): StdInstant = inner

    operator fun plus(duration: Duration): Instant =
        checkedAdd(duration) ?: throw ArithmeticException("overflow adding duration to instant")

    operator fun plus(duration: StdDuration): Instant =
        Instant(inner + duration)

    fun add(duration: Duration): Instant = this + duration

    fun add(duration: StdDuration): Instant = this + duration

    operator fun minus(duration: Duration): Instant =
        checkedSub(duration) ?: throw ArithmeticException("overflow subtracting duration from instant")

    operator fun minus(duration: StdDuration): Instant =
        Instant(inner - duration)

    operator fun minus(other: Instant): Duration =
        when {
            inner == other.inner -> Duration.ZERO
            inner > other.inner ->
                Duration.tryFrom(inner - other.inner).getOrElse {
                    error("overflow converting `std::time::Duration` to `time::Duration`")
                }
            else ->
                -Duration.tryFrom(other.inner - inner).getOrElse {
                    error("overflow converting `std::time::Duration` to `time::Duration`")
                }
        }

    operator fun minus(other: StdInstant): Duration = this - Instant(other)

    fun sub(other: Instant): Duration = this - other

    fun sub(other: StdInstant): Duration = this - other

    fun sub(duration: Duration): Instant = this - duration

    fun sub(duration: StdDuration): Instant = this - duration

    fun eq(rhs: StdInstant): Boolean = inner == rhs

    fun partialCmp(rhs: StdInstant): Int = inner.compareTo(rhs)

    override fun compareTo(other: Instant): Int = inner.compareTo(other.inner)

    operator fun compareTo(other: StdInstant): Int = inner.compareTo(other)

    override fun equals(other: Any?): Boolean =
        when (other) {
            is Instant -> inner == other.inner
            is StdInstant -> inner == other
            else -> false
        }

    override fun hashCode(): Int = inner.hashCode()

    override fun toString(): String = "Instant($inner)"
}

operator fun StdInstant.minus(other: Instant): Duration = Instant(this) - other

operator fun StdInstant.plus(duration: Duration): StdInstant = (Instant(this) + duration).inner

operator fun StdInstant.minus(duration: Duration): StdInstant = (Instant(this) - duration).inner

operator fun StdInstant.compareTo(other: Instant): Int = this.compareTo(other.inner)
