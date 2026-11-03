// port-lint: tests format_description/well_known/iso8601/adt_hack.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.time.formatdescription.wellknown.Config
import io.github.kotlinmania.time.formatdescription.wellknown.DateKind
import io.github.kotlinmania.time.formatdescription.wellknown.FormattedComponents
import io.github.kotlinmania.time.formatdescription.wellknown.OffsetPrecision
import io.github.kotlinmania.time.formatdescription.wellknown.TimePrecision
import io.github.kotlinmania.time.formatdescription.wellknown.iso8601.EncodedConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AdtHackTest {
    private fun assertRoundtrip(config: Config) {
        val encoded = config.encode()
        val decoded = Config.decode(encoded)
        assertEquals(config.formattedComponents, decoded.formattedComponents)
        assertEquals(config.useSeparators, decoded.useSeparators)
        assertEquals(config.yearIsSixDigits, decoded.yearIsSixDigits)
        assertEquals(config.dateKind, decoded.dateKind)
        assertEquals(config.timePrecision, decoded.timePrecision)
        assertEquals(config.offsetPrecision, decoded.offsetPrecision)
    }

    @Test
    fun encodingRoundtrip() {
        assertRoundtrip(Config.DEFAULT)
        assertRoundtrip(Config.DEFAULT.setFormattedComponents(FormattedComponents.None))
        assertRoundtrip(Config.DEFAULT.setFormattedComponents(FormattedComponents.Date))
        assertRoundtrip(Config.DEFAULT.setFormattedComponents(FormattedComponents.Time))
        assertRoundtrip(Config.DEFAULT.setFormattedComponents(FormattedComponents.Offset))
        assertRoundtrip(Config.DEFAULT.setFormattedComponents(FormattedComponents.DateTime))
        assertRoundtrip(Config.DEFAULT.setFormattedComponents(FormattedComponents.DateTimeOffset))
        assertRoundtrip(Config.DEFAULT.setFormattedComponents(FormattedComponents.TimeOffset))
        assertRoundtrip(Config.DEFAULT.setUseSeparators(false))
        assertRoundtrip(Config.DEFAULT.setUseSeparators(true))
        assertRoundtrip(Config.DEFAULT.setYearIsSixDigits(false))
        assertRoundtrip(Config.DEFAULT.setYearIsSixDigits(true))
        assertRoundtrip(Config.DEFAULT.setDateKind(DateKind.Calendar))
        assertRoundtrip(Config.DEFAULT.setDateKind(DateKind.Week))
        assertRoundtrip(Config.DEFAULT.setDateKind(DateKind.Ordinal))
        assertRoundtrip(Config.DEFAULT.setTimePrecision(TimePrecision.Hour(decimalDigits = null)))
        assertRoundtrip(Config.DEFAULT.setTimePrecision(TimePrecision.Minute(decimalDigits = null)))
        assertRoundtrip(Config.DEFAULT.setTimePrecision(TimePrecision.Second(decimalDigits = null)))
        assertRoundtrip(Config.DEFAULT.setTimePrecision(TimePrecision.Hour(decimalDigits = 1u)))
        assertRoundtrip(Config.DEFAULT.setTimePrecision(TimePrecision.Minute(decimalDigits = 1u)))
        assertRoundtrip(Config.DEFAULT.setTimePrecision(TimePrecision.Second(decimalDigits = 1u)))
        assertRoundtrip(Config.DEFAULT.setOffsetPrecision(OffsetPrecision.Hour))
        assertRoundtrip(Config.DEFAULT.setOffsetPrecision(OffsetPrecision.Minute))
    }

    @Test
    fun decodeFail() {
        assertFailsWith<IllegalArgumentException> {
            Config.decode(EncodedConfig.fromHighLow(0x07_00_00_00_00_00_00_00uL, 0uL))
        }
        assertFailsWith<IllegalArgumentException> {
            Config.decode(EncodedConfig.fromHighLow(0x00_02_00_00_00_00_00_00uL, 0uL))
        }
        assertFailsWith<IllegalArgumentException> {
            Config.decode(EncodedConfig.fromHighLow(0x00_00_02_00_00_00_00_00uL, 0uL))
        }
        assertFailsWith<IllegalArgumentException> {
            Config.decode(EncodedConfig.fromHighLow(0x00_00_00_03_00_00_00_00uL, 0uL))
        }
        assertFailsWith<IllegalArgumentException> {
            Config.decode(EncodedConfig.fromHighLow(0x00_00_00_00_03_00_00_00uL, 0uL))
        }
        assertFailsWith<IllegalArgumentException> {
            Config.decode(EncodedConfig.fromHighLow(0x00_00_00_00_00_00_02_00uL, 0uL))
        }
        assertFailsWith<IllegalArgumentException> {
            Config.decode(EncodedConfig.fromHighLow(0x00_00_00_00_00_00_00_01uL, 0uL))
        }
    }
}
