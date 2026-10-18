// port-lint: tests tests/integration/serde/json.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdejson.JsonDeserializer
import io.github.kotlinmania.serdejson.JsonParser
import io.github.kotlinmania.serdejson.toStr
import io.github.kotlinmania.serdetest.compact
import io.github.kotlinmania.serdetest.readable
import io.github.kotlinmania.time.macros.date
import io.github.kotlinmania.time.macros.datetime
import io.github.kotlinmania.time.macros.time
import kotlin.test.Test
import kotlin.test.assertEquals

class SerdeJsonTest {
    @Test
    fun jsonRoundtrip() {
        fun <T> deserializeJson(from: String, de: Deserialize<T>, compact: Boolean): T {
            val parser = JsonParser(from)
            val jsonDe = JsonDeserializer(parser)
            val configuredDe = if (compact) de.compact() else de.readable()
            return configuredDe.deserialize(jsonDe).getOrThrow()
        }

        assertEquals("1", toStr(Weekday.Monday.compact()).getOrThrow())
        assertEquals(Weekday.Monday, deserializeJson("1", Weekday, true))
        assertEquals("\"Monday\"", toStr(Weekday.Monday.readable()).getOrThrow())
        assertEquals(Weekday.Monday, deserializeJson("\"Monday\"", Weekday, false))
        assertEquals(Weekday.Monday, deserializeJson("1", Weekday, false))

        assertEquals("3", toStr(Month.March.compact()).getOrThrow())
        assertEquals(Month.March, deserializeJson("3", Month, true))
        assertEquals("\"March\"", toStr(Month.March.readable()).getOrThrow())
        assertEquals(Month.March, deserializeJson("\"March\"", Month, false))
        assertEquals(Month.March, deserializeJson("3", Month, false))

        val t = time(12, 40, 20)
        assertEquals("[12,40,20,0]", toStr(t.compact()).getOrThrow())
        assertEquals(t, deserializeJson("[12,40,20,0]", Time, true))
        assertEquals("\"12:40:20.0\"", toStr(t.readable()).getOrThrow())
        assertEquals(t, deserializeJson("\"12:40:20.0\"", Time, false))
        assertEquals(t, deserializeJson("[12,40,20,0]", Time, false))

        val pdt = datetime(2022, 5, 20, 12, 40, 20)
        assertEquals("[2022,140,12,40,20,0]", toStr(pdt.compact()).getOrThrow())
        assertEquals(pdt, deserializeJson("[2022,140,12,40,20,0]", PrimitiveDateTime, true))
        assertEquals("\"2022-05-20 12:40:20.0\"", toStr(pdt.readable()).getOrThrow())
        assertEquals(pdt, deserializeJson("\"2022-05-20 12:40:20.0\"", PrimitiveDateTime, false))
        assertEquals(pdt, deserializeJson("[2022,140,12,40,20,0]", PrimitiveDateTime, false))

        val odt = pdt.assumeUtc()
        assertEquals("[2022,140,12,40,20,0,0,0,0]", toStr(odt.compact()).getOrThrow())
        assertEquals(odt, deserializeJson("[2022,140,12,40,20,0,0,0,0]", OffsetDateTime, true))
        assertEquals("\"2022-05-20 12:40:20.0 +00:00:00\"", toStr(odt.readable()).getOrThrow())
        assertEquals(odt, deserializeJson("\"2022-05-20 12:40:20.0 +00:00:00\"", OffsetDateTime, false))
        assertEquals(odt, deserializeJson("[2022,140,12,40,20,0,0,0,0]", OffsetDateTime, false))

        val dur = Duration.new(50, 0)
        assertEquals("[50,0]", toStr(dur.compact()).getOrThrow())
        assertEquals(dur, deserializeJson("[50,0]", Duration, true))
        assertEquals("\"50.000000000\"", toStr(dur.readable()).getOrThrow())
        assertEquals(dur, deserializeJson("\"50.000000000\"", Duration, false))
        assertEquals(dur, deserializeJson("[50,0]", Duration, false))

        val d = date(2022, 4, 5)
        assertEquals("[2022,95]", toStr(d.compact()).getOrThrow())
        assertEquals(d, deserializeJson("[2022,95]", Date, true))
        assertEquals("\"2022-04-05\"", toStr(d.readable()).getOrThrow())
        assertEquals(d, deserializeJson("\"2022-04-05\"", Date, false))
        assertEquals(d, deserializeJson("[2022,95]", Date, false))
    }
}
