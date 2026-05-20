// port-lint: source error/mod.rs
package io.github.kotlinmania.time

/**
 * Various error types returned by methods in the time crate.
 */

/**
 * A unified error type for anything returned by a method in the time crate.
 *
 * This can be used when the exact error returned is not relevant.
 * `Result<T>` will work in these situations.
 */
sealed interface Error {
    fun fmt(): String {
        return when (this) {
            is ConversionRange -> {
                val err = this.err
                err.fmt()
            }
            is ComponentRange -> {
                val err = this.err
                err.fmt()
            }
            is IndeterminateOffset -> {
                val err = this.err
                err.fmt()
            }
            is Format -> {
                val err = this.err
                err.fmt()
            }
            is ParseFromDescription -> {
                val err = this.err
                err.fmt()
            }
            is UnexpectedTrailingCharacters -> {
                val impossible = this.impossible
                impossible
            }
            is TryFromParsed -> {
                val err = this.err
                err.fmt()
            }
            is InvalidFormatDescription -> {
                val err = this.err
                err.fmt()
            }
            is DifferentVariant -> {
                val err = this.err
                err.fmt()
            }
            is InvalidVariant -> {
                val err = this.err
                err.fmt()
            }
        }
    }

    fun source(): Throwable? {
        return when (this) {
            is ConversionRange -> {
                val err = this.err
                err
            }
            is ComponentRange -> {
                val err = this.err
                err
            }
            is IndeterminateOffset -> {
                val err = this.err
                err
            }
            is Format -> {
                val err = this.err
                err
            }
            is ParseFromDescription -> {
                val err = this.err
                err
            }
            is UnexpectedTrailingCharacters -> {
                val impossible = this.impossible
                impossible
            }
            is TryFromParsed -> {
                val err = this.err
                err
            }
            is InvalidFormatDescription -> {
                val err = this.err
                err
            }
            is DifferentVariant -> {
                val err = this.err
                err
            }
            is InvalidVariant -> {
                val err = this.err
                err
            }
        }
    }

    class ConversionRange(val err: io.github.kotlinmania.time.error.ConversionRange) :
        Error

    class ComponentRange(val err: io.github.kotlinmania.time.error.ComponentRange) :
        Error

    class IndeterminateOffset(val err: io.github.kotlinmania.time.error.IndeterminateOffset) :
        Error

    class Format(val err: io.github.kotlinmania.time.error.Format) :
        Error

    class ParseFromDescription(val err: io.github.kotlinmania.time.error.ParseFromDescription) :
        Error

    class UnexpectedTrailingCharacters private constructor(val impossible: Nothing) :
        Error

    class TryFromParsed(val err: io.github.kotlinmania.time.error.TryFromParsed) :
        Error

    class InvalidFormatDescription(
        val err: io.github.kotlinmania.time.error.InvalidFormatDescription,
    ) : Error

    class DifferentVariant(val err: io.github.kotlinmania.time.error.DifferentVariant) :
        Error

    class InvalidVariant(val err: io.github.kotlinmania.time.error.InvalidVariant) :
        Error
}
