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
    fun fmt(): String =
        when (this) {
            is ConversionRange -> err.fmt()
            is ComponentRange -> err.fmt()
            is IndeterminateOffset -> err.fmt()
            is Format -> err.fmt()
            is ParseFromDescription -> err.fmt()
            is UnexpectedTrailingCharacters -> never
            is TryFromParsed -> err.fmt()
            is InvalidFormatDescription -> err.fmt()
            is DifferentVariant -> err.fmt()
            is InvalidVariant -> err.fmt()
        }

    fun source(): Throwable? =
        when (this) {
            is ConversionRange -> err
            is ComponentRange -> err
            is IndeterminateOffset -> err
            is Format -> err
            is ParseFromDescription -> err
            is UnexpectedTrailingCharacters -> never
            is TryFromParsed -> err
            is InvalidFormatDescription -> err
            is DifferentVariant -> err
            is InvalidVariant -> err
        }

    class ConversionRange(
        val err: io.github.kotlinmania.time.error.ConversionRange,
    ) : Error

    class ComponentRange(
        val err: io.github.kotlinmania.time.error.ComponentRange,
    ) : Error

    class IndeterminateOffset(
        val err: io.github.kotlinmania.time.error.IndeterminateOffset,
    ) : Error

    class Format(
        val err: io.github.kotlinmania.time.error.Format,
    ) : Error

    class ParseFromDescription(
        val err: io.github.kotlinmania.time.error.ParseFromDescription,
    ) : Error

    class UnexpectedTrailingCharacters private constructor(
        val never: Nothing,
    ) : Error

    class TryFromParsed(
        val err: io.github.kotlinmania.time.error.TryFromParsed,
    ) : Error

    class InvalidFormatDescription(
        val err: io.github.kotlinmania.time.error.InvalidFormatDescription,
    ) : Error

    class DifferentVariant(
        val err: io.github.kotlinmania.time.error.DifferentVariant,
    ) : Error

    class InvalidVariant(
        val err: io.github.kotlinmania.time.error.InvalidVariant,
    ) : Error
}
