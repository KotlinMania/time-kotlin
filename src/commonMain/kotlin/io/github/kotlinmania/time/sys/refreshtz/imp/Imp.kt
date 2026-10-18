// port-lint: source sys/refresh_tz/imp.rs
package io.github.kotlinmania.time.sys.refreshtz.imp

/** A fallback for any OS not covered. */
internal fun refreshTzUnchecked() {}

/** A fallback for any OS not covered. */
internal fun refreshTz(): Unit? = Unit
