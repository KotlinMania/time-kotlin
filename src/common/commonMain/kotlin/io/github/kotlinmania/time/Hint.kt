// port-lint: source hint.rs
package io.github.kotlinmania.time

/**
 * Hints to the compiler that affect how code should be emitted or optimized.
 */

/** Indicate that a given branch is not likely to be taken, relatively speaking. */
internal fun coldPath() = Unit

/** Indicate that a given condition is likely to be true. */
internal fun likely(value: Boolean): Boolean {
    if (!value) {
        coldPath()
    }
    return value
}

/** Indicate that a given condition is likely to be false. */
internal fun unlikely(value: Boolean): Boolean {
    if (value) {
        coldPath()
    }
    return value
}
