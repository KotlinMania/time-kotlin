// port-lint: tests tests/integration/serde/timestamps.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.value.I128Deserializer
import io.github.kotlinmania.serdecore.ser.FormatterSerializer
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.serdetest.Token
import io.github.kotlinmania.serdetest.assertDeTokens
import io.github.kotlinmania.serdetest.assertDeTokensError
import io.github.kotlinmania.serdetest.assertSerTokens
import io.github.kotlinmania.serdetest.compact
import io.github.kotlinmania.time.macros.datetime
import io.github.kotlinmania.time.serde.timestamp.MicrosecondsSerde
import io.github.kotlinmania.time.serde.timestamp.MillisecondsSerde
import io.github.kotlinmania.time.serde.timestamp.NanosecondsSerde
import io.github.kotlinmania.time.serde.timestamp.TimestampSerde
import kotlin.test.Test
import kotlin.test.assertEquals

class SerdeTimestampsTest {
    @Test
    fun timestampsSerde() {
        val dt = datetime(2000, 1, 1, 0, 0, 0, 0, 0)
        val ser =
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    TimestampSerde.serialize(dt, serializer)
            }
        assertSerTokens(ser.compact(), listOf(Token.I64(946684800L)))
        assertDeTokens(dt, TimestampSerde.compact(), listOf(Token.I64(946684800L)))
        assertDeTokensError(TimestampSerde, listOf(Token.Str("bad")), "invalid type: Str(value=bad), expected i64")

        val dtMillis = datetime(2000, 1, 1, 0, 0, 0, 999_000_000, 0)
        assertDeTokensError(MillisecondsSerde, listOf(Token.Str("bad")), "invalid type: Str(value=bad), expected i128")
        val sbMillis = StringBuilder()
        MillisecondsSerde.serialize(dtMillis, FormatterSerializer(sbMillis)).getOrThrow()
        assertEquals("946684800999", sbMillis.toString())
        assertEquals(dtMillis, MillisecondsSerde.deserialize(I128Deserializer.new("946684800999")).getOrThrow())

        val dtMicros = datetime(2000, 1, 1, 0, 0, 0, 999_999_000, 0)
        assertDeTokensError(MicrosecondsSerde, listOf(Token.Str("bad")), "invalid type: Str(value=bad), expected i128")
        val sbMicros = StringBuilder()
        MicrosecondsSerde.serialize(dtMicros, FormatterSerializer(sbMicros)).getOrThrow()
        assertEquals("946684800999999", sbMicros.toString())
        assertEquals(dtMicros, MicrosecondsSerde.deserialize(I128Deserializer.new("946684800999999")).getOrThrow())

        val dtNanos = datetime(2000, 1, 1, 0, 0, 0, 999_999_999, 0)
        assertDeTokensError(NanosecondsSerde, listOf(Token.Str("bad")), "invalid type: Str(value=bad), expected i128")
        val sbNanos = StringBuilder()
        NanosecondsSerde.serialize(dtNanos, FormatterSerializer(sbNanos)).getOrThrow()
        assertEquals("946684800999999999", sbNanos.toString())
        assertEquals(dtNanos, NanosecondsSerde.deserialize(I128Deserializer.new("946684800999999999")).getOrThrow())
    }
}
