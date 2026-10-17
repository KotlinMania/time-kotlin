// port-lint: source serde/iso8601.rs
package io.github.kotlinmania.time.serde

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.formatdescription.wellknown.Iso8601
import io.github.kotlinmania.time.formatdescription.wellknown.iso8601.Config
import io.github.kotlinmania.time.formatdescription.wellknown.iso8601.EncodedConfig

/** The configuration of ISO 8601 used for serde implementations. */
internal val SERDE_CONFIG: EncodedConfig =
    Config.DEFAULT.setYearIsSixDigits(true).encode()

/**
 * Use the well-known ISO 8601 format when serializing and deserializing an [OffsetDateTime].
 */
object Iso8601Serde : Deserialize<OffsetDateTime> {
    /** Serialize an [OffsetDateTime] using the well-known ISO 8601 format. */
    fun <Ok> serialize(datetime: OffsetDateTime, serializer: Serializer<Ok>): SerdeResult<Ok> {
        val str =
            datetime.format(Iso8601(SERDE_CONFIG)).getOrElse {
                return SerdeResult.failure(SerdeError.custom(it.message ?: it.toString()))
            }
        return serializer.serializeStr(str)
    }

    /** Deserialize an [OffsetDateTime] from its ISO 8601 representation. */
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<OffsetDateTime> =
        deserializer.deserializeStr(Iso8601SerdeVisitor)

    /**
     * Use the well-known ISO 8601 format when serializing and deserializing an `OffsetDateTime?`.
     */
    object Option : Deserialize<OffsetDateTime?> {
        /** Serialize an `OffsetDateTime?` using the well-known ISO 8601 format. */
        fun <Ok> serialize(option: OffsetDateTime?, serializer: Serializer<Ok>): SerdeResult<Ok> {
            if (option == null) return serializer.serializeNone()
            val str =
                option.format(Iso8601(SERDE_CONFIG)).getOrElse {
                    return SerdeResult.failure(SerdeError.custom(it.message ?: it.toString()))
                }
            return serializer.serializeSome(StringSerialize(str))
        }

        /** Deserialize an `OffsetDateTime?` from its ISO 8601 representation. */
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<OffsetDateTime?> =
            deserializer.deserializeOption(OptionIso8601SerdeVisitor)
    }
}
