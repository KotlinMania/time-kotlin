// port-lint: tests tests/integration/serde/rfc3339.rs
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
import io.github.kotlinmania.serdetest.assertDeTokensError
import io.github.kotlinmania.serdetest.assertSerTokensError
import io.github.kotlinmania.serdetest.assertTokens
import io.github.kotlinmania.serdetest.compact
import io.github.kotlinmania.time.macros.datetime
import io.github.kotlinmania.time.serde.Rfc3339Serde
import kotlin.test.Test
import kotlin.test.assertEquals

class SerdeRfc3339Test {
    private data class WithDtTest(
        val dt: OffsetDateTime,
        val optionDt: OffsetDateTime?,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
            val state = serializer.serializeStruct("Test", 2).getOrElse { return SerdeResult.failure(it) }
            val dtSer =
                object : Serialize {
                    override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                        Rfc3339Serde.serialize(dt, serializer)
                }
            val optSer =
                object : Serialize {
                    override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                        Rfc3339Serde.Option.serialize(optionDt, serializer)
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
                                    "dt" ->
                                        dtVal =
                                            access.nextValue(Rfc3339Serde).getOrElse { return SerdeResult.failure(it) }
                                    "option_dt" ->
                                        optVal =
                                            access
                                                .nextValue(Rfc3339Serde.Option)
                                                .getOrElse { return SerdeResult.failure(it) }
                                }
                            }
                            val finalDt = dtVal ?: return SerdeResult.failure(SerdeError.missingField("dt"))
                            return SerdeResult.success(WithDtTest(finalDt, optVal))
                        }
                    },
                )
        }

    @Test
    fun rfc3339Serde() {
        val value1 =
            WithDtTest(
                datetime(2000, 1, 1, 0, 0, 0, 0, 0),
                datetime(2000, 1, 1, 0, 0, 0, 0, 0),
            )
        assertTokens(
            value1.compact(),
            deserializer.compactDe(),
            listOf(
                Token.Struct("Test", 2),
                Token.Str("dt"),
                Token.BorrowedStr("2000-01-01T00:00:00Z"),
                Token.Str("option_dt"),
                Token.Some,
                Token.BorrowedStr("2000-01-01T00:00:00Z"),
                Token.StructEnd,
            ),
        )
        val value2 =
            WithDtTest(
                datetime(2000, 1, 1, 0, 0, 0, 0, 0),
                null,
            )
        assertTokens(
            value2.compact(),
            deserializer.compactDe(),
            listOf(
                Token.Struct("Test", 2),
                Token.Str("dt"),
                Token.BorrowedStr("2000-01-01T00:00:00Z"),
                Token.Str("option_dt"),
                Token.None,
                Token.StructEnd,
            ),
        )
        assertDeTokensError(
            deserializer,
            listOf(
                Token.Struct("Test", 2),
                Token.Str("dt"),
                Token.BorrowedStr("bad"),
                Token.StructEnd,
            ),
            "the 'year' component could not be parsed",
        )
        val valueBad =
            WithDtTest(
                datetime(2000, 1, 1, 0, 0, 0, 0, 0, 0, 1),
                null,
            )
        assertSerTokensError(
            valueBad,
            listOf(
                Token.Struct("Test", 2),
                Token.Str("dt"),
            ),
            "The offset_second component cannot be formatted into the requested format.",
        )
    }

    @Test
    fun parseJson() {
        val parsed =
            fromStr("""{"dt": "2000-01-01T00:00:00Z", "option_dt": null}""", deserializer).getOrThrow()
        assertEquals(
            WithDtTest(
                datetime("2000-01-01 00:00:00 UTC"),
                null,
            ),
            parsed,
        )
    }

    @Test
    fun issue479() {
        val aJson = """{"dt": "2022-05-01T10:20:42.123Z"}"""
        val bJson = """{"dt": "2022-05-01T10:20:42.123Z", "option_dt": "2022-05-01T10:20:42.123Z"}"""

        val a = fromStr(aJson, deserializer).getOrThrow()
        val b = fromStr(bJson, deserializer).getOrThrow()

        assertEquals(
            WithDtTest(
                datetime("2022-05-01 10:20:42.123 UTC"),
                null,
            ),
            a,
        )
        assertEquals(
            WithDtTest(
                datetime("2022-05-01 10:20:42.123 UTC"),
                datetime("2022-05-01 10:20:42.123 UTC"),
            ),
            b,
        )
    }
}
