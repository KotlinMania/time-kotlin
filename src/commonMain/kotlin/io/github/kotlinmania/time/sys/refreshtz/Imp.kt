// port-lint: source sys/refresh_tz/imp.rs
package io.github.kotlinmania.time.sys.refreshtz

/** A fallback for any OS not covered. */
internal object Imp {
    fun refreshTzUnchecked() {}

    fun refreshTz(): Unit? = Unit
}

internal fun impRefreshTzUnchecked() {
    Imp.refreshTzUnchecked()
}

internal fun impRefreshTz(): Unit? = Imp.refreshTz()
