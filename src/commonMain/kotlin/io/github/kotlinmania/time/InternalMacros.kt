// port-lint: source internal_macros.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.error.ComponentRange

/**
 * Internal helper functions corresponding to upstream `internal_macros.rs`.
 */
internal object InternalMacros {
    /** Division of integers, rounding the resulting value towards negative infinity. */
    fun divFloor(selfValue: Int, rhs: Int): Int {
        val d = selfValue / rhs
        val r = selfValue % rhs
        val correction = (selfValue xor rhs) shr (Int.SIZE_BITS - 1)
        return if (r != 0) d + correction else d
    }

    /** Division of 64-bit integers, rounding the resulting value towards negative infinity. */
    fun divFloor(selfValue: Long, rhs: Long): Long {
        val d = selfValue / rhs
        val r = selfValue % rhs
        val correction = (selfValue xor rhs) shr (Long.SIZE_BITS - 1)
        return if (r != 0L) d + correction else d
    }

    /** Similar to `overflowing_add`, returning the number of times that it overflowed (at most once). */
    fun carryMostOnce(value: Int, min: Int, max: Int): Pair<Int, Int> =
        if (likely(value >= min)) {
            if (likely(value < max)) {
                value to 0
            } else {
                (value - (max - min)) to 1
            }
        } else {
            (value + (max - min)) to -1
        }

    /** Similar to `overflowing_add`, returning the number of times that it overflowed (at most twice). */
    fun carryMostTwice(value: Int, min: Int, max: Int): Pair<Int, Int> =
        if (likely(value >= min)) {
            if (likely(value < max)) {
                value to 0
            } else if (value < 2 * max - min) {
                (value - (max - min)) to 1
            } else {
                (value - 2 * (max - min)) to 2
            }
        } else {
            if (value >= min - max) {
                (value + (max - min)) to -1
            } else {
                (value + 2 * (max - min)) to -2
            }
        }

    /** Similar to `overflowing_add`, returning the number of times that it overflowed (at most thrice). */
    fun carryMostThrice(value: Int, min: Int, max: Int): Pair<Int, Int> =
        if (likely(value >= min)) {
            if (likely(value < max)) {
                value to 0
            } else if (value < 2 * max - min) {
                (value - (max - min)) to 1
            } else if (value < 3 * max - 2 * min) {
                (value - 2 * (max - min)) to 2
            } else {
                (value - 3 * (max - min)) to 3
            }
        } else {
            if (value >= min - max) {
                (value + (max - min)) to -1
            } else if (value >= 2 * (min - max)) {
                (value + 2 * (max - min)) to -2
            } else {
                (value + 3 * (max - min)) to -3
            }
        }

    /** Ensure a value is within `[min, max]`, returning [ComponentRange] on failure. */
    fun ensureRanged(value: Int, min: Int, max: Int, name: String): Result<Int> =
        if (value in min..max) {
            Result.success(value)
        } else {
            coldPath()
            Result.failure(ComponentRange.unconditional(name))
        }

    /** Fail with an internal bug message. */
    fun bug(description: String): Nothing = error("internal error: $description")
}
