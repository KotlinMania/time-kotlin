package io.github.kotlinmania.time.sys.refreshtz

/**
 * Update time zone information from the system.
 *
 * For safety documentation, see [io.github.kotlinmania.time.refreshTz].
 */
internal fun refreshTzUnchecked() {
    unixRefreshTzUnchecked()
}

/**
 * Attempt to update time zone information from the system.
 *
 * Returns `null` if the call is not known to be sound.
 */
internal fun refreshTz(): Unit? = unixRefreshTz()
