// port-lint: source serde/timestamp/milliseconds_i64.rs
package io.github.kotlinmania.time.serde.timestamp

import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.I64Deserialize
import io.github.kotlinmania.serdecore.de.nullableDeserialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.error.intoDeError
import io.github.kotlinmania.time.formatting.computeTimestampMillis
import io.github.kotlinmania.time.serde.I64Serialize

private fun millisI64ToNanosString(millis: Long): String =
    if (millis == 0L) "0" else "${millis}000000"

/**
 * Treat an [OffsetDateTime] as a 64-bit Unix timestamp with milliseconds for the purposes of serde.
 */
object MillisecondsI64Serde : Deserialize<OffsetDateTime> {
    /** Serialize an [OffsetDateTime] as its 64-bit Unix timestamp with milliseconds. */
    fun <Ok> serialize(datetime: OffsetDateTime, serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeI64(computeTimestampMillis(datetime.unixTimestamp(), datetime.nanosecond()))

    /** Deserialize an [OffsetDateTime] from its 64-bit Unix timestamp with milliseconds. */
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<OffsetDateTime> =
        when (val res = I64Deserialize.deserialize(deserializer)) {
            is SerdeResult.Success ->
                OffsetDateTime.fromUnixTimestampNanos(millisI64ToNanosString(res.value)).fold(
                    onSuccess = { SerdeResult.success(it) },
                    onFailure = { SerdeResult.failure(it.intoDeError()) },
                )
            is SerdeResult.Failure -> res
        }

    /**
     * Treat an `OffsetDateTime?` as a 64-bit Unix timestamp with milliseconds for the purposes of serde.
     */
    object Option : Deserialize<OffsetDateTime?> {
        /** Serialize an `OffsetDateTime?` as its 64-bit Unix timestamp with milliseconds. */
        fun <Ok> serialize(option: OffsetDateTime?, serializer: Serializer<Ok>): SerdeResult<Ok> =
            if (option == null) {
                serializer.serializeNone()
            } else {
                serializer.serializeSome(
                    I64Serialize(computeTimestampMillis(option.unixTimestamp(), option.nanosecond())),
                )
            }

        /** Deserialize an `OffsetDateTime?` from its 64-bit Unix timestamp with milliseconds. */
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<OffsetDateTime?> =
            when (val res = nullableDeserialize(I64Deserialize).deserialize(deserializer)) {
                is SerdeResult.Success -> {
                    val v = res.value ?: return SerdeResult.success(null)
                    OffsetDateTime.fromUnixTimestampNanos(millisI64ToNanosString(v)).fold(
                        onSuccess = { SerdeResult.success(it) },
                        onFailure = { SerdeResult.failure(it.intoDeError()) },
                    )
                }
                is SerdeResult.Failure -> res
            }
    }
}
