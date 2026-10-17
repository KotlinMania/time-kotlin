// port-lint: source serde/timestamp/milliseconds.rs
package io.github.kotlinmania.time.serde.timestamp

import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.I128Deserialize
import io.github.kotlinmania.serdecore.de.nullableDeserialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.error.intoDeError
import io.github.kotlinmania.time.formatting.computeTimestampMillis

private fun millisString(datetime: OffsetDateTime): String =
    computeTimestampMillis(datetime.unixTimestamp(), datetime.nanosecond()).toString()

private fun millisToNanosString(millis: String): String =
    if (millis == "0" || millis == "-0" || millis == "+0") "0" else "${millis}000000"

/**
 * Treat an [OffsetDateTime] as a Unix timestamp with milliseconds for the purposes of serde.
 */
object MillisecondsSerde : Deserialize<OffsetDateTime> {
    /** Serialize an [OffsetDateTime] as its Unix timestamp with milliseconds. */
    fun <Ok> serialize(datetime: OffsetDateTime, serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeI128(millisString(datetime))

    /** Deserialize an [OffsetDateTime] from its Unix timestamp with milliseconds. */
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<OffsetDateTime> =
        when (val res = I128Deserialize.deserialize(deserializer)) {
            is SerdeResult.Success ->
                OffsetDateTime.fromUnixTimestampNanos(millisToNanosString(res.value)).fold(
                    onSuccess = { SerdeResult.success(it) },
                    onFailure = { SerdeResult.failure(it.intoDeError()) },
                )
            is SerdeResult.Failure -> res
        }

    /**
     * Treat an `OffsetDateTime?` as a Unix timestamp with milliseconds for the purposes of serde.
     */
    object Option : Deserialize<OffsetDateTime?> {
        /** Serialize an `OffsetDateTime?` as its Unix timestamp with milliseconds. */
        fun <Ok> serialize(option: OffsetDateTime?, serializer: Serializer<Ok>): SerdeResult<Ok> =
            if (option == null) {
                serializer.serializeNone()
            } else {
                serializer.serializeSome(I128Serialize(millisString(option)))
            }

        /** Deserialize an `OffsetDateTime?` from its Unix timestamp with milliseconds. */
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<OffsetDateTime?> =
            when (val res = nullableDeserialize(I128Deserialize).deserialize(deserializer)) {
                is SerdeResult.Success -> {
                    val v = res.value ?: return SerdeResult.success(null)
                    OffsetDateTime.fromUnixTimestampNanos(millisToNanosString(v)).fold(
                        onSuccess = { SerdeResult.success(it) },
                        onFailure = { SerdeResult.failure(it.intoDeError()) },
                    )
                }
                is SerdeResult.Failure -> res
            }
    }
}
