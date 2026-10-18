// port-lint: tests tests/integration/error.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.error.ComponentRange
import io.github.kotlinmania.time.error.ConversionRange
import io.github.kotlinmania.time.error.DifferentVariant
import io.github.kotlinmania.time.error.Format
import io.github.kotlinmania.time.error.IndeterminateOffset
import io.github.kotlinmania.time.error.InvalidFormatDescription
import io.github.kotlinmania.time.error.InvalidVariant
import io.github.kotlinmania.time.error.Parse
import io.github.kotlinmania.time.error.ParseFromDescription
import io.github.kotlinmania.time.error.StdError
import io.github.kotlinmania.time.error.TryFromParsed
import io.github.kotlinmania.time.formatdescription.parse.parse
import io.github.kotlinmania.time.macros.formatDescription
import io.github.kotlinmania.time.parsing.Parsed
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ErrorTest {
    private fun componentRange(): ComponentRange =
        Date.fromOrdinalDate(0, 367).exceptionOrNull() as ComponentRange

    private fun insufficientTypeInformation(): Format =
        Time.MIDNIGHT.format(formatDescription("[year]")).exceptionOrNull() as Format

    private fun unexpectedTrailingCharacters(): Parse =
        Time.parse("a", formatDescription("")).exceptionOrNull() as Parse

    private fun invalidFormatDescription(): InvalidFormatDescription =
        parse("[").exceptionOrNull() as InvalidFormatDescription

    private fun invalidLiteral(): ParseFromDescription =
        Parsed.parseLiteral("a".encodeToByteArray(), "b".encodeToByteArray()).exceptionOrNull() as ParseFromDescription

    @Test
    fun debug() {
        val p = Parse.from(ParseFromDescription.InvalidComponent("a"))
        assertEquals(p.toString(), p.toString())
        val ifd = invalidFormatDescription()
        assertEquals(ifd.toString(), ifd.toString())
        val dv = DifferentVariant()
        assertEquals(dv.toString(), dv.toString())
        val iv = InvalidVariant()
        assertEquals(iv.toString(), iv.toString())
    }

    @Test
    fun display() {
        assertEquals(ConversionRange.fmt(), StdError.from(ConversionRange).fmt())
        assertEquals(componentRange().fmt(), StdError.from(componentRange()).fmt())
        assertEquals(componentRange().fmt(), TryFromParsed.from(componentRange()).fmt())
        assertEquals(IndeterminateOffset.fmt(), StdError.from(IndeterminateOffset).fmt())
        assertEquals(
            TryFromParsed.InsufficientInformation.fmt(),
            StdError.from(TryFromParsed.InsufficientInformation).fmt(),
        )
        assertEquals(
            insufficientTypeInformation().fmt(),
            StdError.from(insufficientTypeInformation()).fmt(),
        )
        assertEquals(
            Format.InvalidComponent("a").fmt(),
            StdError.from(Format.InvalidComponent("a")).fmt(),
        )
        assertEquals(
            ParseFromDescription.InvalidComponent("a").fmt(),
            StdError.from(Parse.from(ParseFromDescription.InvalidComponent("a"))).fmt(),
        )
        assertEquals(invalidLiteral().fmt(), Parse.from(invalidLiteral()).fmt())
        assertEquals(
            componentRange().fmt(),
            StdError.from(Parse.from(TryFromParsed.from(componentRange()))).fmt(),
        )
        assertEquals(
            ParseFromDescription.InvalidComponent("a").fmt(),
            Parse.from(ParseFromDescription.InvalidComponent("a")).fmt(),
        )
        assertEquals(
            componentRange().fmt(),
            Parse.from(TryFromParsed.from(componentRange())).fmt(),
        )
        assertEquals(
            unexpectedTrailingCharacters().fmt(),
            StdError.from(unexpectedTrailingCharacters()).fmt(),
        )
        assertEquals(
            invalidFormatDescription().fmt(),
            StdError.from(invalidFormatDescription()).fmt(),
        )
        assertEquals(DifferentVariant().fmt(), StdError.from(DifferentVariant()).fmt())
        assertEquals(InvalidVariant().fmt(), StdError.from(InvalidVariant()).fmt())
    }

    @Test
    fun source() {
        assertTrue(StdError.from(ConversionRange).source() is ConversionRange)
        assertTrue(StdError.from(componentRange()).source() is ComponentRange)
        assertTrue(TryFromParsed.from(componentRange()).source() is ComponentRange)
        assertNull(TryFromParsed.InsufficientInformation.source())
        assertNull(insufficientTypeInformation().source())
        assertNull(Format.InvalidComponent("a").source())
        assertTrue(StdError.from(insufficientTypeInformation()).source() is Format)
        assertTrue(StdError.from(IndeterminateOffset).source() is IndeterminateOffset)
        assertTrue(Parse.from(TryFromParsed.InsufficientInformation).source() is TryFromParsed)
        assertTrue(StdError.from(TryFromParsed.InsufficientInformation).source() is TryFromParsed)
        assertTrue(Parse.from(ParseFromDescription.InvalidComponent("a")).source() is ParseFromDescription)
        assertTrue(StdError.from(ParseFromDescription.InvalidComponent("a")).source() is ParseFromDescription)
        assertTrue(unexpectedTrailingCharacters().source() is ParseFromDescription)
        assertTrue(StdError.from(unexpectedTrailingCharacters()).source() is ParseFromDescription)
        assertTrue(StdError.from(invalidFormatDescription()).source() is InvalidFormatDescription)
        assertTrue(StdError.from(DifferentVariant()).source() is DifferentVariant)
        assertTrue(StdError.from(InvalidVariant()).source() is InvalidVariant)
    }

    @Test
    fun componentName() {
        assertEquals("ordinal", componentRange().name())
    }

    @Test
    fun conversion() {
        assertTrue(ComponentRange.tryFrom(StdError.from(componentRange())).isSuccess)
        assertTrue(ConversionRange.tryFrom(StdError.from(ConversionRange)).isSuccess)
        assertTrue(Format.tryFrom(StdError.from(insufficientTypeInformation())).isSuccess)
        assertTrue(IndeterminateOffset.tryFrom(StdError.from(IndeterminateOffset)).isSuccess)
        assertTrue(InvalidFormatDescription.tryFrom(StdError.from(invalidFormatDescription())).isSuccess)
        assertTrue(ParseFromDescription.tryFrom(StdError.from(invalidLiteral())).isSuccess)
        assertTrue(ParseFromDescription.tryFrom(Parse.from(invalidLiteral())).isSuccess)
        assertTrue(ParseFromDescription.tryFrom(unexpectedTrailingCharacters()).isSuccess)
        assertTrue(Parse.tryFrom(StdError.from(unexpectedTrailingCharacters())).isSuccess)
        assertTrue(Parse.tryFrom(StdError.from(invalidLiteral())).isSuccess)
        assertTrue(Parse.tryFrom(StdError.from(TryFromParsed.InsufficientInformation)).isSuccess)
        assertTrue(DifferentVariant.tryFrom(StdError.from(DifferentVariant())).isSuccess)
        assertTrue(InvalidVariant.tryFrom(StdError.from(InvalidVariant())).isSuccess)
        assertTrue(ComponentRange.tryFrom(TryFromParsed.ComponentRange(componentRange())).isSuccess)
        assertTrue(TryFromParsed.tryFrom(StdError.from(TryFromParsed.InsufficientInformation)).isSuccess)
        assertTrue(TryFromParsed.tryFrom(Parse.from(TryFromParsed.InsufficientInformation)).isSuccess)

        assertTrue(ComponentRange.tryFrom(StdError.from(IndeterminateOffset)).isFailure)
        assertTrue(ConversionRange.tryFrom(StdError.from(IndeterminateOffset)).isFailure)
        assertTrue(Format.tryFrom(StdError.from(IndeterminateOffset)).isFailure)
        assertTrue(IndeterminateOffset.tryFrom(StdError.from(ConversionRange)).isFailure)
        assertTrue(InvalidFormatDescription.tryFrom(StdError.from(IndeterminateOffset)).isFailure)
        assertTrue(ParseFromDescription.tryFrom(StdError.from(IndeterminateOffset)).isFailure)
        assertTrue(Parse.tryFrom(StdError.from(IndeterminateOffset)).isFailure)
        assertTrue(DifferentVariant.tryFrom(StdError.from(IndeterminateOffset)).isFailure)
        assertTrue(InvalidVariant.tryFrom(StdError.from(IndeterminateOffset)).isFailure)
        assertTrue(ComponentRange.tryFrom(TryFromParsed.InsufficientInformation).isFailure)
        assertTrue(TryFromParsed.tryFrom(StdError.from(IndeterminateOffset)).isFailure)
        assertTrue(TryFromParsed.tryFrom(unexpectedTrailingCharacters()).isFailure)
    }
}
