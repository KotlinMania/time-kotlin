package io.github.kotlinmania.time.sys.localoffsetat

import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.UtcOffset

/**
 * Attempt to obtain the system's UTC offset. If the offset cannot be determined, `null` is
 * returned.
 */
internal fun localOffsetAt(datetime: OffsetDateTime): UtcOffset? =
    unixLocalOffsetAt(datetime)
