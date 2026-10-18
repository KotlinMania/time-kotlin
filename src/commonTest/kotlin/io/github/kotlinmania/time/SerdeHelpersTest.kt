// port-lint: tests tests/integration/serde_helpers.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.SeqAccess
import io.github.kotlinmania.serdecore.de.Visitor
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.serdetest.Token
import io.github.kotlinmania.serdetest.assertDeTokensError
import io.github.kotlinmania.serdetest.assertSerTokensError
import io.github.kotlinmania.serdetest.assertTokens
import io.github.kotlinmania.time.macros.datetime
import io.github.kotlinmania.time.serde.Rfc2822Serde
import io.github.kotlinmania.time.serde.Rfc3339Serde
import io.github.kotlinmania.time.serde.timestamp.TimestampSerde
import kotlin.test.Test

class SerdeHelpersTest {
    private data class Full(
        val rfc2822: OffsetDateTime,
        val rfc2822OptSome: OffsetDateTime?,
        val rfc2822OptNone: OffsetDateTime?,
        val rfc3339: OffsetDateTime,
        val rfc3339OptSome: OffsetDateTime?,
        val rfc3339OptNone: OffsetDateTime?,
        val timestamp: OffsetDateTime,
        val timestampOptSome: OffsetDateTime?,
        val timestampOptNone: OffsetDateTime?,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
            val state = serializer.serializeTupleStruct("Full", 9).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            Rfc2822Serde.serialize(rfc2822, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            Rfc2822Serde.Option.serialize(rfc2822OptSome, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            Rfc2822Serde.Option.serialize(rfc2822OptNone, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            Rfc3339Serde.serialize(rfc3339, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            Rfc3339Serde.Option.serialize(rfc3339OptSome, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            Rfc3339Serde.Option.serialize(rfc3339OptNone, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            TimestampSerde.serialize(timestamp, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            TimestampSerde.Option.serialize(timestampOptSome, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            TimestampSerde.Option.serialize(timestampOptNone, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            return state.end()
        }
    }

    private val fullDeserializer: Deserialize<Full> =
        object : Deserialize<Full> {
            override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Full> =
                deserializer.deserializeTupleStruct(
                    "Full",
                    9,
                    object : Visitor<Full> {
                        override fun expecting(): String = "tuple struct Full"

                        override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<Full> {
                            val f0 =
                                access.nextElement(Rfc2822Serde).getOrElse { return SerdeResult.failure(it) }
                                    ?: return SerdeResult.failure(SerdeError.invalidLength(0, this))
                            val f1 =
                                access.nextElement(Rfc2822Serde.Option).getOrElse { return SerdeResult.failure(it) }
                            val f2 =
                                access.nextElement(Rfc2822Serde.Option).getOrElse { return SerdeResult.failure(it) }
                            val f3 =
                                access.nextElement(Rfc3339Serde).getOrElse { return SerdeResult.failure(it) }
                                    ?: return SerdeResult.failure(SerdeError.invalidLength(3, this))
                            val f4 =
                                access.nextElement(Rfc3339Serde.Option).getOrElse { return SerdeResult.failure(it) }
                            val f5 =
                                access.nextElement(Rfc3339Serde.Option).getOrElse { return SerdeResult.failure(it) }
                            val f6 =
                                access.nextElement(TimestampSerde).getOrElse { return SerdeResult.failure(it) }
                                    ?: return SerdeResult.failure(SerdeError.invalidLength(6, this))
                            val f7 =
                                access.nextElement(TimestampSerde.Option).getOrElse { return SerdeResult.failure(it) }
                            val f8 =
                                access.nextElement(TimestampSerde.Option).getOrElse { return SerdeResult.failure(it) }
                            return SerdeResult.success(Full(f0, f1, f2, f3, f4, f5, f6, f7, f8))
                        }
                    },
                )
        }

    @Test
    fun success() {
        val dt = datetime("2021-01-02 03:04:05 UTC")
        assertTokens(
            Full(dt, dt, null, dt, dt, null, dt, dt, null),
            fullDeserializer,
            listOf(
                Token.TupleStruct("Full", 9),
                Token.Str("Sat, 02 Jan 2021 03:04:05 +0000"),
                Token.Some,
                Token.Str("Sat, 02 Jan 2021 03:04:05 +0000"),
                Token.None,
                Token.Str("2021-01-02T03:04:05Z"),
                Token.Some,
                Token.Str("2021-01-02T03:04:05Z"),
                Token.None,
                Token.I64(1_609_556_645),
                Token.Some,
                Token.I64(1_609_556_645),
                Token.None,
                Token.TupleStructEnd,
            ),
        )
    }

    @Test
    fun rfc2822Error() {
        val dtBad = datetime("2021-01-02 03:04:05 +0:00:01")
        assertSerTokensError(
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    Rfc2822Serde.serialize(dtBad, serializer)
            },
            emptyList(),
            "The offset_second component cannot be formatted into the requested format.",
        )
        assertSerTokensError(
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    Rfc2822Serde.Option.serialize(dtBad, serializer)
            },
            emptyList(),
            "The offset_second component cannot be formatted into the requested format.",
        )
        assertDeTokensError(
            Rfc2822Serde,
            listOf(Token.Bool(false)),
            "invalid type: Bool(value=false), expected an RFC2822-formatted `OffsetDateTime`",
        )
        assertDeTokensError(
            Rfc2822Serde.Option,
            listOf(Token.Bool(false)),
            "invalid type: Bool(value=false), expected an RFC2822-formatted `Option<OffsetDateTime>`",
        )
        assertDeTokensError(
            Rfc2822Serde.Option,
            listOf(Token.Some, Token.Bool(false)),
            "invalid type: Bool(value=false), expected an RFC2822-formatted `OffsetDateTime`",
        )
    }

    @Test
    fun rfc3339Error() {
        val dtBad = datetime("2021-01-02 03:04:05 +0:00:01")
        assertSerTokensError(
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    Rfc3339Serde.serialize(dtBad, serializer)
            },
            emptyList(),
            "The offset_second component cannot be formatted into the requested format.",
        )
        assertSerTokensError(
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
                    Rfc3339Serde.Option.serialize(dtBad, serializer)
            },
            emptyList(),
            "The offset_second component cannot be formatted into the requested format.",
        )
        assertDeTokensError(
            Rfc3339Serde,
            listOf(Token.Bool(false)),
            "invalid type: Bool(value=false), expected an RFC3339-formatted `OffsetDateTime`",
        )
        assertDeTokensError(
            Rfc3339Serde.Option,
            listOf(Token.Bool(false)),
            "invalid type: Bool(value=false), expected an RFC3339-formatted `Option<OffsetDateTime>`",
        )
        assertDeTokensError(
            Rfc3339Serde.Option,
            listOf(Token.Some, Token.Bool(false)),
            "invalid type: Bool(value=false), expected an RFC3339-formatted `OffsetDateTime`",
        )
    }

    @Test
    fun timestampError() {
        assertDeTokensError(
            TimestampSerde,
            listOf(Token.Bool(false)),
            "invalid type: Bool(value=false), expected i64",
        )
        assertDeTokensError(
            TimestampSerde.Option,
            listOf(Token.Some, Token.Bool(false)),
            "invalid type: Bool(value=false), expected i64",
        )
        assertDeTokensError(
            TimestampSerde,
            listOf(Token.I64(100_000_000_000_000L)),
            "invalid timestamp, expected an in-range value",
        )
        assertDeTokensError(
            TimestampSerde.Option,
            listOf(Token.Some, Token.I64(-100_000_000_000_000L)),
            "invalid timestamp, expected an in-range value",
        )
    }
}
