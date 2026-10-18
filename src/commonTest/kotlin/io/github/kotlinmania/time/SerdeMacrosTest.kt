// port-lint: tests tests/integration/serde/macros.rs
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
import io.github.kotlinmania.serdetest.Token
import io.github.kotlinmania.serdetest.assertDeTokens
import io.github.kotlinmania.serdetest.assertDeTokensError
import io.github.kotlinmania.serdetest.assertSerTokensError
import io.github.kotlinmania.serdetest.assertTokens
import io.github.kotlinmania.serdetest.compact
import io.github.kotlinmania.time.formatdescription.wellknown.Iso8601
import io.github.kotlinmania.time.macros.date
import io.github.kotlinmania.time.macros.datetime
import io.github.kotlinmania.time.macros.formatDescription
import io.github.kotlinmania.time.macros.offset
import io.github.kotlinmania.time.macros.time
import io.github.kotlinmania.time.serde.formatDescriptionDate
import io.github.kotlinmania.time.serde.formatDescriptionOffsetDateTime
import io.github.kotlinmania.time.serde.formatDescriptionPrimitiveDateTime
import io.github.kotlinmania.time.serde.formatDescriptionTime
import io.github.kotlinmania.time.serde.formatDescriptionUtcOffset
import kotlin.test.Test
import kotlin.test.assertNotNull

class SerdeMacrosTest {
    private data class TestCustomFormat(
        val offsetDt: OffsetDateTime,
        val primitiveDt: PrimitiveDateTime?,
        val date: Date,
        val time: Time?,
        val offset: UtcOffset,
        val timeAlt: Time,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
            val state = serializer.serializeStruct("TestCustomFormat", 6).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    "offset_dt",
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            offsetDtFormat.serialize(offsetDt, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    "primitive_dt",
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            primitiveDtFormat.option.serialize(primitiveDt, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    "date",
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            dateFormat.serialize(date, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    "time",
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            timeFormat.option.serialize(time, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    "offset",
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            offsetFormat.serialize(offset, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    "time_alt",
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            timeFormatAlt.serialize(timeAlt, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            return state.end()
        }
    }

    private data class TestVersioning(
        val time1: Time,
        val time2: Time,
        val time3: Time,
        val time4: Time,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
            val state = serializer.serializeStruct("TestVersioning", 4).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    "time_1",
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            versionTest1.serialize(time1, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    "time_2",
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            versionTest2.serialize(time2, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    "time_3",
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            versionTest3.serialize(time3, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    "time_4",
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            versionDefault.serialize(time4, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            return state.end()
        }
    }

    private data class TestNested(
        val time1: Time,
        val time2: Time,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
            val state = serializer.serializeStruct("TestNested", 2).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    "time_1",
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            nestedV1.serialize(time1, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            state
                .serializeField(
                    "time_2",
                    object : Serialize {
                        override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                            nestedV2.serialize(time2, serializer)
                    },
                ).getOrElse { return SerdeResult.failure(it) }
            return state.end()
        }
    }

    @Test
    fun customSerialize() {
        assertNotNull(myFormat)

        val value =
            TestCustomFormat(
                offsetDt = datetime("2000-01-01 00:00 -4:00"),
                primitiveDt = datetime(2000, 1, 1, 0, 0),
                date = date("2000-01-01"),
                time = null,
                offset = offset(-4),
                timeAlt = time("12:34"),
            )
        assertTokens(
            value.compact(),
            customFormatDeserializer.compactDe(),
            listOf(
                Token.Struct("TestCustomFormat", 6),
                Token.Str("offset_dt"),
                Token.BorrowedStr("custom format: 2000-01-01 00:00:00 -04:00"),
                Token.Str("primitive_dt"),
                Token.Some,
                Token.BorrowedStr("custom format: 2000-01-01 00:00:00"),
                Token.Str("date"),
                Token.BorrowedStr("custom format: 2000-01-01"),
                Token.Str("time"),
                Token.None,
                Token.Str("offset"),
                Token.BorrowedStr("custom format: -04:00"),
                Token.Str("time_alt"),
                Token.BorrowedStr("12:34"),
                Token.StructEnd,
            ),
        )
    }

    @Test
    fun customSerializeError() {
        assertDeTokensError(
            customFormatDeserializer,
            listOf(
                Token.Struct("TestCustomFormat", 5),
                Token.Str("offset_dt"),
                Token.BorrowedStr("custom format: 2000-01-01 0:00:00 -04:00"),
            ),
            "the 'hour' component could not be parsed",
        )
        assertDeTokensError(
            customFormatDeserializer,
            listOf(
                Token.Struct("TestCustomFormat", 5),
                Token.Str("offset_dt"),
                Token.BorrowedStr("custom format: 2000-01-01 00:00:00 -04:00"),
                Token.Str("primitive_dt"),
                Token.Some,
                Token.BorrowedStr("custom format: 2000-01-01 0:00:00 -04:00"),
            ),
            "the 'hour' component could not be parsed",
        )
        assertDeTokensError(
            customFormatDeserializer,
            listOf(
                Token.Struct("TestCustomFormat", 5),
                Token.Str("offset_dt"),
                Token.Bool(false),
            ),
            "invalid type: Bool(value=false), expected a(n) `OffsetDateTime` in the format \"custom format: [year]-[month]-[day] [hour]:[minute]:[second] [offset_hour sign:mandatory]:[offset_minute]\"",
        )
        assertDeTokensError(
            customFormatDeserializer,
            listOf(
                Token.Struct("TestCustomFormat", 5),
                Token.Str("offset_dt"),
                Token.BorrowedStr("custom format: 2000-01-01 00:00:00 -04:00"),
                Token.Str("primitive_dt"),
                Token.Bool(false),
            ),
            "invalid type: Bool(value=false), expected an `Option<PrimitiveDateTime>` in the format \"custom format: [year]-[month]-[day] [hour]:[minute]:[second]\"",
        )
    }

    @Test
    fun customSerializeBadTypeError() {
        val badFormat = formatDescriptionPrimitiveDateTime("[offset_hour]:[offset_minute]")
        val dt = datetime(2000, 1, 1, 0, 0)
        val fieldSer =
            object : Serialize {
                override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                    badFormat.serialize(dt, serializer)
            }
        val badStruct =
            object : Serialize {
                override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
                    val state =
                        serializer
                            .serializeStruct("TestCustomFormatPrimitiveDateTimeBad", 1)
                            .getOrElse { return SerdeResult.failure(it) }
                    state
                        .serializeField("dt", fieldSer)
                        .getOrElse { return SerdeResult.failure(it) }
                    return state.end()
                }
            }
        assertSerTokensError(
            badStruct,
            listOf(
                Token.Struct("TestCustomFormatPrimitiveDateTimeBad", 1),
                Token.Str("dt"),
            ),
            "The type being formatted does not contain sufficient information to format a component.",
        )
    }

    @Test
    fun versioning() {
        val value =
            TestVersioning(
                time1 = Time.MIDNIGHT,
                time2 = Time.MIDNIGHT,
                time3 = Time.MIDNIGHT,
                time4 = Time.MIDNIGHT,
            )
        assertTokens(
            value,
            versioningDeserializer,
            listOf(
                Token.Struct("TestVersioning", 4),
                Token.Str("time_1"),
                Token.Str("[ 00:00"),
                Token.Str("time_2"),
                Token.Str("""\\ 00:00"""),
                Token.Str("time_3"),
                Token.Str("""\ 00:00"""),
                Token.Str("time_4"),
                Token.Str("00:00"),
                Token.StructEnd,
            ),
        )
    }

    @Test
    fun nested() {
        val value =
            TestNested(
                time1 = time("12:34:56"),
                time2 = time("12:34:56"),
            )
        assertTokens(
            value,
            nestedDeserializer,
            listOf(
                Token.Struct("TestNested", 2),
                Token.Str("time_1"),
                Token.Str("12:34:56"),
                Token.Str("time_2"),
                Token.Str("12:34:56"),
                Token.StructEnd,
            ),
        )

        val expected =
            TestNested(
                time1 = time("12:34"),
                time2 = time("12:34"),
            )
        assertDeTokens(
            expected,
            nestedDeserializer,
            listOf(
                Token.Struct("TestNested", 2),
                Token.Str("time_1"),
                Token.Str("12:34"),
                Token.Str("time_2"),
                Token.Str("12:34"),
                Token.StructEnd,
            ),
        )
    }

    companion object {
        private val myFormat = formatDescriptionOffsetDateTime(Iso8601.DEFAULT)
        private val offsetDtFormat =
            formatDescriptionOffsetDateTime(
                "custom format: [year]-[month]-[day] [hour]:[minute]:[second] [offset_hour sign:mandatory]:[offset_minute]",
            )
        private val primitiveDtFormat =
            formatDescriptionPrimitiveDateTime(
                "custom format: [year]-[month]-[day] [hour]:[minute]:[second]",
            )
        private val timeFormat =
            formatDescriptionTime("custom format: [minute]:[second]")
        private val dateFormat =
            formatDescriptionDate("custom format: [year]-[month]-[day]")
        private val offsetFormat =
            formatDescriptionUtcOffset("custom format: [offset_hour sign:mandatory]:[offset_minute]")
        private val timeFormatAlt =
            formatDescriptionTime(formatDescription("[hour]:[minute]"))

        private val versionTest1 = formatDescriptionTime("[[ [hour]:[minute]", version = 1)
        private val versionTest2 = formatDescriptionTime("""\\ [hour]:[minute]""", version = 1)
        private val versionTest3 = formatDescriptionTime("""\\ [hour]:[minute]""", version = 2)
        private val versionDefault = formatDescriptionTime("[hour]:[minute]")

        private val nestedV1 = formatDescriptionTime("[hour]:[minute][optional [:[second]]]", version = 1)
        private val nestedV2 = formatDescriptionTime("[hour]:[minute][optional [:[second]]]", version = 2)

        private val customFormatDeserializer: Deserialize<TestCustomFormat> =
            object : Deserialize<TestCustomFormat> {
                override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<TestCustomFormat> =
                    deserializer.deserializeStruct(
                        "TestCustomFormat",
                        listOf("offset_dt", "primitive_dt", "date", "time", "offset", "time_alt"),
                        object : Visitor<TestCustomFormat> {
                            override fun expecting(): String = "struct TestCustomFormat"

                            override fun <A : MapAccess> visitMap(access: A): SerdeResult<TestCustomFormat> {
                                var offsetDtVal: OffsetDateTime? = null
                                var primitiveDtVal: PrimitiveDateTime? = null
                                var dateVal: Date? = null
                                var timeVal: Time? = null
                                var offsetVal: UtcOffset? = null
                                var timeAltVal: Time? = null
                                while (true) {
                                    val key =
                                        access.nextKey(StringDeserialize).getOrElse { return SerdeResult.failure(it) }
                                            ?: break
                                    when (key) {
                                        "offset_dt" -> {
                                            offsetDtVal =
                                                access
                                                    .nextValue(offsetDtFormat)
                                                    .getOrElse { return SerdeResult.failure(it) }
                                        }

                                        "primitive_dt" -> {
                                            primitiveDtVal =
                                                access
                                                    .nextValue(primitiveDtFormat.option)
                                                    .getOrElse { return SerdeResult.failure(it) }
                                        }

                                        "date" -> {
                                            dateVal =
                                                access
                                                    .nextValue(dateFormat)
                                                    .getOrElse { return SerdeResult.failure(it) }
                                        }

                                        "time" -> {
                                            timeVal =
                                                access
                                                    .nextValue(timeFormat.option)
                                                    .getOrElse { return SerdeResult.failure(it) }
                                        }

                                        "offset" -> {
                                            offsetVal =
                                                access
                                                    .nextValue(offsetFormat)
                                                    .getOrElse { return SerdeResult.failure(it) }
                                        }

                                        "time_alt" -> {
                                            timeAltVal =
                                                access
                                                    .nextValue(timeFormatAlt)
                                                    .getOrElse { return SerdeResult.failure(it) }
                                        }
                                    }
                                }
                                return SerdeResult.success(
                                    TestCustomFormat(
                                        offsetDt = offsetDtVal ?: return SerdeResult.failure(SerdeError.missingField("offset_dt")),
                                        primitiveDt = primitiveDtVal,
                                        date = dateVal ?: return SerdeResult.failure(SerdeError.missingField("date")),
                                        time = timeVal,
                                        offset = offsetVal ?: return SerdeResult.failure(SerdeError.missingField("offset")),
                                        timeAlt = timeAltVal ?: return SerdeResult.failure(SerdeError.missingField("time_alt")),
                                    ),
                                )
                            }
                        },
                    )
            }

        private val versioningDeserializer: Deserialize<TestVersioning> =
            object : Deserialize<TestVersioning> {
                override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<TestVersioning> =
                    deserializer.deserializeStruct(
                        "TestVersioning",
                        listOf("time_1", "time_2", "time_3", "time_4"),
                        object : Visitor<TestVersioning> {
                            override fun expecting(): String = "struct TestVersioning"

                            override fun <A : MapAccess> visitMap(access: A): SerdeResult<TestVersioning> {
                                var t1: Time? = null
                                var t2: Time? = null
                                var t3: Time? = null
                                var t4: Time? = null
                                while (true) {
                                    val key =
                                        access.nextKey(StringDeserialize).getOrElse { return SerdeResult.failure(it) }
                                            ?: break
                                    when (key) {
                                        "time_1" -> t1 = access.nextValue(versionTest1).getOrElse { return SerdeResult.failure(it) }
                                        "time_2" -> t2 = access.nextValue(versionTest2).getOrElse { return SerdeResult.failure(it) }
                                        "time_3" -> t3 = access.nextValue(versionTest3).getOrElse { return SerdeResult.failure(it) }
                                        "time_4" -> t4 = access.nextValue(versionDefault).getOrElse { return SerdeResult.failure(it) }
                                    }
                                }
                                return SerdeResult.success(
                                    TestVersioning(
                                        time1 = t1 ?: return SerdeResult.failure(SerdeError.missingField("time_1")),
                                        time2 = t2 ?: return SerdeResult.failure(SerdeError.missingField("time_2")),
                                        time3 = t3 ?: return SerdeResult.failure(SerdeError.missingField("time_3")),
                                        time4 = t4 ?: return SerdeResult.failure(SerdeError.missingField("time_4")),
                                    ),
                                )
                            }
                        },
                    )
            }

        private val nestedDeserializer: Deserialize<TestNested> =
            object : Deserialize<TestNested> {
                override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<TestNested> =
                    deserializer.deserializeStruct(
                        "TestNested",
                        listOf("time_1", "time_2"),
                        object : Visitor<TestNested> {
                            override fun expecting(): String = "struct TestNested"

                            override fun <A : MapAccess> visitMap(access: A): SerdeResult<TestNested> {
                                var t1: Time? = null
                                var t2: Time? = null
                                while (true) {
                                    val key =
                                        access.nextKey(StringDeserialize).getOrElse { return SerdeResult.failure(it) }
                                            ?: break
                                    when (key) {
                                        "time_1" -> t1 = access.nextValue(nestedV1).getOrElse { return SerdeResult.failure(it) }
                                        "time_2" -> t2 = access.nextValue(nestedV2).getOrElse { return SerdeResult.failure(it) }
                                    }
                                }
                                return SerdeResult.success(
                                    TestNested(
                                        time1 = t1 ?: return SerdeResult.failure(SerdeError.missingField("time_1")),
                                        time2 = t2 ?: return SerdeResult.failure(SerdeError.missingField("time_2")),
                                    ),
                                )
                            }
                        },
                    )
            }
    }
}
