// port-lint: source format_description/well_known/rfc3339.rs
package io.github.kotlinmania.time.formatdescription.wellknown

import io.github.kotlinmania.time.formatting.Formattable
import io.github.kotlinmania.time.parsing.Parsable

/**
 * The format described in [RFC 3339](https://tools.ietf.org/html/rfc3339#section-5.6).
 *
 * Format example: 1985-04-12T23:20:50.52Z
 */
data object Rfc3339 : Formattable, Parsable
