// port-lint: tests tests/integration/serde/rfc2822.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.MapAccess
import io.github.kotlinmania.serdecore.de.StringDeserialize
import io.github.kotlinmania.serdecore.de.Visitor
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.serdejson.fromStr
import io.github.kotlinmania.serdetest.Token
import io.github.kotlinmania.serdetest.assertTokens
import io.github.kotlinmania.serdetest.compact
import io.github.kotlinmania.time.macros.datetime
import io.github.kotlinmania.time.serde.Rfc2822Serde
import kotlin.test.Test
import kotlin.test.assertEquals

class SerdeRfc2822Test {
    private data class WithDtTest(
        val dt: OffsetDateTime,
        val optionDt: OffsetDateTime?,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
            val state = serializer.serializeStruct("Test", 2).getOrElse { return SerdeResult.failure(it) }
            val dtSer =
                object : Serialize {
                    override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                        Rfc2822Serde.serialize(dt, serializer)
                }
            val optSer =
                object : Serialize {
                    override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                        Rfc2822Serde.Option.serialize(optionDt, serializer)
                }
            state.serializeField("dt", dtSer).getOrElse { return SerdeResult.failure(it) }
            state.serializeField("option_dt", optSer).getOrElse { return SerdeResult.failure(it) }
            return state.end()
        }
    }

    private val deserializer: Deserialize<WithDtTest> =
        object : Deserialize<WithDtTest> {
            override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<WithDtTest> =
                deserializer.deserializeStruct(
                    "Test",
                    listOf("dt", "option_dt"),
                    object : Visitor<WithDtTest> {
                        override fun expecting(): String = "struct Test"

                        override fun <A : MapAccess> visitMap(access: A): SerdeResult<WithDtTest> {
                            var dtVal: OffsetDateTime? = null
                            var optVal: OffsetDateTime? = null
                            while (true) {
                                val key =
                                    access.nextKey(StringDeserialize).getOrElse { return SerdeResult.failure(it) }
                                        ?: break
                                when (key) {
                                    "dt" -> {
                                        dtVal =
                                            access.nextValue(Rfc2822Serde).getOrElse { return SerdeResult.failure(it) }
                                    }

                                    "option_dt" -> {
                                        optVal =
                                            access
                                                .nextValue(Rfc2822Serde.Option)
                                                .getOrElse { return SerdeResult.failure(it) }
                                    }
                                }
                            }
                            val finalDt = dtVal ?: return SerdeResult.failure(SerdeError.missingField("dt"))
                            return SerdeResult.success(WithDtTest(finalDt, optVal))
                        }
                    },
                )
        }

    @Test
    fun rfc2822Serde() {
        val value =
            WithDtTest(
                datetime(2000, 1, 1, 0, 0, 0, 0, 0),
                datetime(2000, 1, 1, 0, 0, 0, 0, 0),
            )
        assertTokens(
            value.compact(),
            deserializer.compactDe(),
            listOf(
                Token.Struct("Test", 2),
                Token.Str("dt"),
                Token.BorrowedStr("Sat, 01 Jan 2000 00:00:00 +0000"),
                Token.Str("option_dt"),
                Token.Some,
                Token.BorrowedStr("Sat, 01 Jan 2000 00:00:00 +0000"),
                Token.StructEnd,
            ),
        )
        val parsed =
            fromStr("""{"dt": "Sat, 01 Jan 2000 00:00:00 +0000", "option_dt": null}""", deserializer).getOrThrow()
        assertEquals(datetime(2000, 1, 1, 0, 0, 0, 0, 0), parsed.dt)
        assertEquals(null, parsed.optionDt)
    }
}
