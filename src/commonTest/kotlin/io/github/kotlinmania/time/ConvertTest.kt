// port-lint: tests tests/integration/convert.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.convert.Nanosecond
import io.github.kotlinmania.time.convert.Second
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertTest {
    @Test
    fun issue749() {
        assertEquals(1_000_000_000u, Nanosecond.perT<UInt>(Second))
    }
}
