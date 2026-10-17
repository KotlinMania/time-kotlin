// port-lint: source parsing/combinator/rfc/rfc2234.rs
package io.github.kotlinmania.time.parsing.combinator.rfc

import io.github.kotlinmania.time.parsing.ParsedItem

/** Consume exactly one space or tab. */
internal fun wsp(input: ByteArray): ParsedItem<Unit>? {
    if (input.isEmpty()) return null
    val b = input[0]
    return if (b == ' '.code.toByte() || b == '\t'.code.toByte()) {
        ParsedItem(input.copyOfRange(1, input.size), Unit)
    } else {
        null
    }
}
