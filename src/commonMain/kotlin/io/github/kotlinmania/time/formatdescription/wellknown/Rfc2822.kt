// port-lint: source format_description/well_known/rfc2822.rs
package io.github.kotlinmania.time.formatdescription.wellknown

import io.github.kotlinmania.time.formatting.Formattable
import io.github.kotlinmania.time.parsing.Parsable

/**
 * The format described in [RFC 2822](https://tools.ietf.org/html/rfc2822#section-3.3).
 *
 * Example: Fri, 21 Nov 1997 09:55:06 -0600
 */
data object Rfc2822 : Formattable, Parsable
