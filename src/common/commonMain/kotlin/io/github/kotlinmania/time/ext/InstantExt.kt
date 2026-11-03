// port-lint: source ext/instant.rs
package io.github.kotlinmania.time.ext

import io.github.kotlinmania.time.Duration
import io.github.kotlinmania.time.Instant

/**
 * Extension functions for [Instant] that add methods for signed [Duration]s.
 */

fun Instant.addSigned(duration: Duration): Instant =
    checkedAddSigned(duration)
        ?: throw ArithmeticException("overflow when adding duration to instant")

fun Instant.subSigned(duration: Duration): Instant =
    checkedSubSigned(duration)
        ?: throw ArithmeticException("overflow when subtracting duration from instant")

fun Instant.checkedAddSigned(duration: Duration): Instant? =
    checkedAdd(duration)

fun Instant.checkedSubSigned(duration: Duration): Instant? =
    checkedSub(duration)

fun Instant.signedDurationSince(earlier: Instant): Duration =
    this - earlier
