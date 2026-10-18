// port-lint: tests tests/integration/serde/error_conditions.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.serdetest.Token
import io.github.kotlinmania.serdetest.assertSerTokensError
import io.github.kotlinmania.time.error.Format
import io.github.kotlinmania.time.formatting.BorrowedFormatItemSliceFormattable
import io.github.kotlinmania.time.macros.datetime
import io.github.kotlinmania.time.macros.formatDescription
import kotlin.test.Test

class SerdeErrorConditionsTest {
    private data class TestBadIo(
        val dt: OffsetDateTime,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
            val state = serializer.serializeStruct("TestBadIo", 1).getOrElse { return SerdeResult.failure(it) }
            val fieldSer =
                object : Serialize {
                    override fun <R> serialize(serializer: Serializer<R>): SerdeResult<R> =
                        serializeBadIo(dt)
                }
            state.serializeField("dt", fieldSer).getOrElse { return SerdeResult.failure(it) }
            return state.end()
        }
    }

    @Test
    fun customSerializeIoError() {
        val value = TestBadIo(dt = datetime("2000-01-01 00:00 -4:00"))
        assertSerTokensError(
            value,
            listOf(
                Token.Struct("TestBadIo", 1),
                Token.Str("dt"),
            ),
            "failed to write whole buffer",
        )
    }

    companion object {
        private val zeroBuffer =
            object : Appendable {
                override fun append(value: Char): Appendable =
                    throw IllegalStateException("failed to write whole buffer")

                override fun append(value: CharSequence?): Appendable =
                    throw IllegalStateException("failed to write whole buffer")

                override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable =
                    throw IllegalStateException("failed to write whole buffer")
            }

        private fun <Ok> serializeBadIo(datetime: OffsetDateTime): SerdeResult<Ok> {
            val err =
                BorrowedFormatItemSliceFormattable(
                    formatDescription("nonempty format description"),
                ).formatInto(zeroBuffer, datetime).exceptionOrNull() as Format
            return SerdeResult.failure(err.intoInvalidSerdeValue())
        }
    }
}
