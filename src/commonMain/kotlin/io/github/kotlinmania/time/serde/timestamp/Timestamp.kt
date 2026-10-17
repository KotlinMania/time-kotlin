package io.github.kotlinmania.time.serde.timestamp

import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.I64Deserialize
import io.github.kotlinmania.serdecore.de.nullableDeserialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.error.intoDeError
import io.github.kotlinmania.time.serde.I64Serialize

/**
 * Treat an [OffsetDateTime] as a Unix timestamp for the purposes of serde.
 */
object TimestampSerde : Deserialize<OffsetDateTime> {
    /** Serialize an [OffsetDateTime] as its Unix timestamp. */
    fun <Ok> serialize(datetime: OffsetDateTime, serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeI64(datetime.unixTimestamp())

    /** Deserialize an [OffsetDateTime] from its Unix timestamp. */
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<OffsetDateTime> =
        when (val res = I64Deserialize.deserialize(deserializer)) {
            is SerdeResult.Success ->
                OffsetDateTime.fromUnixTimestamp(res.value).fold(
                    onSuccess = { SerdeResult.success(it) },
                    onFailure = { SerdeResult.failure(it.intoDeError()) },
                )
            is SerdeResult.Failure -> res
        }

    /**
     * Treat an `OffsetDateTime?` as a Unix timestamp for the purposes of serde.
     */
    object Option : Deserialize<OffsetDateTime?> {
        /** Serialize an `OffsetDateTime?` as its Unix timestamp. */
        fun <Ok> serialize(option: OffsetDateTime?, serializer: Serializer<Ok>): SerdeResult<Ok> =
            if (option == null) {
                serializer.serializeNone()
            } else {
                serializer.serializeSome(I64Serialize(option.unixTimestamp()))
            }

        /** Deserialize an `OffsetDateTime?` from its Unix timestamp. */
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<OffsetDateTime?> =
            when (val res = nullableDeserialize(I64Deserialize).deserialize(deserializer)) {
                is SerdeResult.Success -> {
                    val v = res.value ?: return SerdeResult.success(null)
                    OffsetDateTime.fromUnixTimestamp(v).fold(
                        onSuccess = { SerdeResult.success(it) },
                        onFailure = { SerdeResult.failure(it.intoDeError()) },
                    )
                }
                is SerdeResult.Failure -> res
            }
    }
}
