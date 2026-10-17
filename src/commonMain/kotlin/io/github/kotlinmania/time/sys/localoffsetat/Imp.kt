// port-lint: source sys/local_offset_at/imp.rs
package io.github.kotlinmania.time.sys.localoffsetat

import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.UtcOffset

/** A fallback for any OS not covered. */
internal object Imp {
    fun localOffsetAt(datetime: OffsetDateTime): UtcOffset? =
        if (datetime.unixTimestamp() == Long.MIN_VALUE) null else null
}

internal fun impLocalOffsetAt(datetime: OffsetDateTime): UtcOffset? = Imp.localOffsetAt(datetime)
