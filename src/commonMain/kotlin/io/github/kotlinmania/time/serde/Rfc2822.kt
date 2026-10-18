// port-lint: source serde/rfc2822.rs
package io.github.kotlinmania.time.serde

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.formatdescription.wellknown.Rfc2822

/**
 * Use the well-known RFC2822 format when serializing and deserializing an [OffsetDateTime].
 */
object Rfc2822Serde : Deserialize<OffsetDateTime> {
    /** Serialize an [OffsetDateTime] using the well-known RFC2822 format. */
    fun <Ok> serialize(datetime: OffsetDateTime, serializer: Serializer<Ok>): SerdeResult<Ok> {
        val str =
            datetime.format(Rfc2822).getOrElse {
                return SerdeResult.failure(SerdeError.custom(it.message ?: it.toString()))
            }
        return serializer.serializeStr(str)
    }

    /** Deserialize an [OffsetDateTime] from its RFC2822 representation. */
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<OffsetDateTime> =
        deserializer.deserializeStr(Rfc2822Visitor)

    /**
     * Use the well-known RFC2822 format when serializing and deserializing an `OffsetDateTime?`.
     */
    object Option : Deserialize<OffsetDateTime?> {
        /** Serialize an `OffsetDateTime?` using the well-known RFC2822 format. */
        fun <Ok> serialize(option: OffsetDateTime?, serializer: Serializer<Ok>): SerdeResult<Ok> {
            if (option == null) return serializer.serializeNone()
            val str =
                option.format(Rfc2822).getOrElse {
                    return SerdeResult.failure(SerdeError.custom(it.message ?: it.toString()))
                }
            return serializer.serializeSome(StringSerialize(str))
        }

        /** Deserialize an `OffsetDateTime?` from its RFC2822 representation. */
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<OffsetDateTime?> =
            deserializer.deserializeOption(OptionRfc2822Visitor)
    }
}
