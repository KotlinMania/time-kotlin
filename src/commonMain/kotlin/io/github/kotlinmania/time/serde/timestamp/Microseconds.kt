// port-lint: source serde/timestamp/microseconds.rs
package io.github.kotlinmania.time.serde.timestamp

import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.I128Deserialize
import io.github.kotlinmania.serdecore.de.nullableDeserialize
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.error.intoDeError
import io.github.kotlinmania.time.formatting.computeTimestampMicrosSignAndAbs

internal data class I128Serialize(
    val value: String,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeI128(value)
}

private fun microsString(datetime: OffsetDateTime): String {
    val (isNeg, absStr) = computeTimestampMicrosSignAndAbs(datetime.unixTimestamp(), datetime.nanosecond())
    return if (isNeg) "-$absStr" else absStr
}

private fun microsToNanosString(micros: String): String =
    if (micros == "0" || micros == "-0" || micros == "+0") "0" else "${micros}000"

/**
 * Treat an [OffsetDateTime] as a Unix timestamp with microseconds for the purposes of serde.
 */
object MicrosecondsSerde : Deserialize<OffsetDateTime> {
    /** Serialize an [OffsetDateTime] as its Unix timestamp with microseconds. */
    fun <Ok> serialize(datetime: OffsetDateTime, serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeI128(microsString(datetime))

    /** Deserialize an [OffsetDateTime] from its Unix timestamp with microseconds. */
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<OffsetDateTime> =
        when (val res = I128Deserialize.deserialize(deserializer)) {
            is SerdeResult.Success -> {
                OffsetDateTime.fromUnixTimestampNanos(microsToNanosString(res.value)).fold(
                    onSuccess = { SerdeResult.success(it) },
                    onFailure = { SerdeResult.failure(it.intoDeError()) },
                )
            }

            is SerdeResult.Failure -> {
                res
            }
        }

    /**
     * Treat an `OffsetDateTime?` as a Unix timestamp with microseconds for the purposes of serde.
     */
    object Option : Deserialize<OffsetDateTime?> {
        /** Serialize an `OffsetDateTime?` as its Unix timestamp with microseconds. */
        fun <Ok> serialize(option: OffsetDateTime?, serializer: Serializer<Ok>): SerdeResult<Ok> =
            if (option == null) {
                serializer.serializeNone()
            } else {
                serializer.serializeSome(I128Serialize(microsString(option)))
            }

        /** Deserialize an `OffsetDateTime?` from its Unix timestamp with microseconds. */
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<OffsetDateTime?> =
            when (val res = nullableDeserialize(I128Deserialize).deserialize(deserializer)) {
                is SerdeResult.Success -> {
                    val v = res.value ?: return SerdeResult.success(null)
                    OffsetDateTime.fromUnixTimestampNanos(microsToNanosString(v)).fold(
                        onSuccess = { SerdeResult.success(it) },
                        onFailure = { SerdeResult.failure(it.intoDeError()) },
                    )
                }

                is SerdeResult.Failure -> {
                    res
                }
            }
    }
}
