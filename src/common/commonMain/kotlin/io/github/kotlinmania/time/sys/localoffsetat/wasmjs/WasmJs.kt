// port-lint: source sys/local_offset_at/wasm_js.rs
package io.github.kotlinmania.time.sys.localoffsetat.wasmjs

import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.UtcOffset
import io.github.kotlinmania.time.convert.Hour
import io.github.kotlinmania.time.convert.Minute
import io.github.kotlinmania.time.interop.JsSysDate

/** Obtain the system's UTC offset. */
internal fun localOffsetAt(datetime: OffsetDateTime): UtcOffset? {
    val jsDate = JsSysDate.fromOffsetDateTime(datetime)
    val timezoneOffset = jsDate.getTimezoneOffset().toInt() * -Minute.perT<Int>(Hour)
    return UtcOffset.fromWholeSeconds(timezoneOffset).getOrNull()
}
