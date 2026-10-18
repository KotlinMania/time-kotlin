// port-lint: tests tests/integration/serde/iso8601.rs
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
import io.github.kotlinmania.serdejson.toStr
import io.github.kotlinmania.serdetest.Token
import io.github.kotlinmania.serdetest.assertDeTokens
import io.github.kotlinmania.serdetest.assertDeTokensError
import io.github.kotlinmania.serdetest.assertSerTokensError
import io.github.kotlinmania.serdetest.assertTokens
import io.github.kotlinmania.serdetest.compact
import io.github.kotlinmania.time.macros.datetime
import io.github.kotlinmania.time.serde.Iso8601Serde
import kotlin.test.Test
import kotlin.test.assertEquals

class SerdeIso8601Test {
    private data class WithDtTest(
        val dt: OffsetDateTime,
        val optionDt: OffsetDateTime?,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
            val state = serializer.serializeStruct("Test", 2).getOrElse { return SerdeResult.failure(it) }
            val dtSer =
                object : Serialize {
                    override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                        Iso8601Serde.serialize(dt, serializer)
                }
            val optSer =
                object : Serialize {
                    override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                        Iso8601Serde.Option.serialize(optionDt, serializer)
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
                                            access.nextValue(Iso8601Serde).getOrElse { return SerdeResult.failure(it) }
                                    "option_dt" ->
                                        optVal =
                                            access
                                                .nextValue(Iso8601Serde.Option)
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
    fun iso8601Serde() {
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
                Token.BorrowedStr("+002000-01-01T00:00:00.000000000Z"),
                Token.Str("option_dt"),
                Token.Some,
                Token.BorrowedStr("+002000-01-01T00:00:00.000000000Z"),
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
                Token.BorrowedStr("+002000-01-01T00:00:00.000000000Z"),
                Token.Str("option_dt"),
                Token.None,
                Token.StructEnd,
            ),
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
        val leapExpected =
            WithDtTest(
                datetime(2016, 12, 31, 23, 59, 59, 999_999_999, 0),
                datetime(2016, 12, 31, 23, 59, 59, 999_999_999, 0),
            )
        assertDeTokens(
            leapExpected,
            deserializer,
            listOf(
                Token.Struct("Test", 2),
                Token.Str("dt"),
                Token.BorrowedStr("2016-12-31T23:59:60.000Z"),
                Token.Str("option_dt"),
                Token.Some,
                Token.BorrowedStr("2016-12-31T23:59:60.000Z"),
                Token.StructEnd,
            ),
        )
    }

    @Test
    fun issue724Truncation() {
        val value = datetime("2025-01-10 23:01:16.000081999 UTC")
        val demoSer =
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    Iso8601Serde.serialize(value, serializer)
            }
        val serialized = toStr(demoSer).getOrThrow()
        assertEquals("\"+002025-01-10T23:01:16.000081999Z\"", serialized)

        val deserialized = fromStr(serialized, Iso8601Serde).getOrThrow()
        assertEquals(value, deserialized)
    }
}
