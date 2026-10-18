// port-lint: source sys/refresh_tz/unix.rs
package io.github.kotlinmania.time.sys.refreshtz.unix

/**
 * Whether the operating system has a thread-safe environment. This allows bypassing the check for
 * if the process is multi-threaded.
 */
internal const val OS_HAS_THREAD_SAFE_ENVIRONMENT: Boolean = true

/**
 * Update time zone information from the system.
 *
 * For safety documentation, see [io.github.kotlinmania.time.refreshTz].
 */
internal fun refreshTzUnchecked() {}

/**
 * Attempt to update time zone information from the system. Returns `null` if the call is not known
 * to be sound.
 */
internal fun refreshTz(): Unit? =
    if (OS_HAS_THREAD_SAFE_ENVIRONMENT) {
        refreshTzUnchecked()
        Unit
    } else {
        null
    }
