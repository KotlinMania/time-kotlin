package io.github.kotlinmania.time.serde

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.Visitor
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.time.Date
import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.PrimitiveDateTime
import io.github.kotlinmania.time.Time
import io.github.kotlinmania.time.UtcDateTime
import io.github.kotlinmania.time.UtcOffset
import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatdescription.Component
import io.github.kotlinmania.time.formatdescription.OwnedFormatItem
import io.github.kotlinmania.time.formatdescription.modifier.Day
import io.github.kotlinmania.time.formatdescription.modifier.Hour
import io.github.kotlinmania.time.formatdescription.modifier.Minute
import io.github.kotlinmania.time.formatdescription.modifier.Month
import io.github.kotlinmania.time.formatdescription.modifier.OffsetHour
import io.github.kotlinmania.time.formatdescription.modifier.OffsetMinute
import io.github.kotlinmania.time.formatdescription.modifier.OffsetSecond
import io.github.kotlinmania.time.formatdescription.modifier.Second
import io.github.kotlinmania.time.formatdescription.modifier.Subsecond
import io.github.kotlinmania.time.formatdescription.modifier.Year
import io.github.kotlinmania.time.formatdescription.parse.parseOwned
import io.github.kotlinmania.time.formatting.Formattable
import io.github.kotlinmania.time.parsing.Parsable

/** The format used when serializing and deserializing a human-readable [Date]. */
internal val DATE_FORMAT: List<BorrowedFormatItem> =
    listOf(
        BorrowedFormatItem.Component(Component.Year(Year())),
        BorrowedFormatItem.Literal("-".encodeToByteArray()),
        BorrowedFormatItem.Component(Component.Month(Month())),
        BorrowedFormatItem.Literal("-".encodeToByteArray()),
        BorrowedFormatItem.Component(Component.Day(Day())),
    )

/** The format used when serializing and deserializing a human-readable [Time]. */
internal val TIME_FORMAT: List<BorrowedFormatItem> =
    listOf(
        BorrowedFormatItem.Component(Component.Hour(Hour())),
        BorrowedFormatItem.Literal(":".encodeToByteArray()),
        BorrowedFormatItem.Component(Component.Minute(Minute())),
        BorrowedFormatItem.Literal(":".encodeToByteArray()),
        BorrowedFormatItem.Component(Component.Second(Second())),
        BorrowedFormatItem.Literal(".".encodeToByteArray()),
        BorrowedFormatItem.Component(Component.Subsecond(Subsecond())),
    )

/** The format used when serializing and deserializing a human-readable [UtcOffset]. */
internal val UTC_OFFSET_FORMAT: List<BorrowedFormatItem> =
    listOf(
        BorrowedFormatItem.Component(Component.OffsetHour(OffsetHour(signIsMandatory = true))),
        BorrowedFormatItem.Optional(
            BorrowedFormatItem.Compound(
                listOf(
                    BorrowedFormatItem.Literal(":".encodeToByteArray()),
                    BorrowedFormatItem.Component(Component.OffsetMinute(OffsetMinute())),
                    BorrowedFormatItem.Optional(
                        BorrowedFormatItem.Compound(
                            listOf(
                                BorrowedFormatItem.Literal(":".encodeToByteArray()),
                                BorrowedFormatItem.Component(Component.OffsetSecond(OffsetSecond())),
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )

/** The format used when serializing and deserializing a human-readable [PrimitiveDateTime]. */
internal val PRIMITIVE_DATE_TIME_FORMAT: List<BorrowedFormatItem> =
    listOf(
        BorrowedFormatItem.Compound(DATE_FORMAT),
        BorrowedFormatItem.Literal(" ".encodeToByteArray()),
        BorrowedFormatItem.Compound(TIME_FORMAT),
    )

/** The format used when serializing and deserializing a human-readable [UtcDateTime]. */
internal val UTC_DATE_TIME_FORMAT: List<BorrowedFormatItem> = PRIMITIVE_DATE_TIME_FORMAT

/** The format used when serializing and deserializing a human-readable [OffsetDateTime]. */
internal val OFFSET_DATE_TIME_FORMAT: List<BorrowedFormatItem> =
    listOf(
        BorrowedFormatItem.Compound(DATE_FORMAT),
        BorrowedFormatItem.Literal(" ".encodeToByteArray()),
        BorrowedFormatItem.Compound(TIME_FORMAT),
        BorrowedFormatItem.Literal(" ".encodeToByteArray()),
        BorrowedFormatItem.Compound(UTC_OFFSET_FORMAT),
    )

internal data class I8Serialize(
    val value: Byte,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeI8(value)
}

internal data class I32Serialize(
    val value: Int,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeI32(value)
}

internal data class I64Serialize(
    val value: Long,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeI64(value)
}

internal data class U8Serialize(
    val value: UByte,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeU8(value)
}

internal data class U16Serialize(
    val value: UShort,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeU16(value)
}

internal data class U32Serialize(
    val value: UInt,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeU32(value)
}

internal data class StringSerialize(
    val value: String,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeStr(value)
}

/**
 * Custom format serializer/deserializer corresponding to `time::serde::format_description!`.
 */
class FormatDescriptionSerde<T>(
    private val typeName: String,
    private val formatDescriptionString: String?,
    private val formatter: (T) -> Result<String>,
    private val parser: (String) -> Result<T>,
) : Deserialize<T> {
    private val valueVisitor =
        object : Visitor<T> {
            override fun expecting(): String =
                if (formatDescriptionString != null) {
                    "a(n) `$typeName` in the format \"$formatDescriptionString\""
                } else {
                    "a(n) `$typeName`"
                }

            override fun visitStr(v: String): SerdeResult<T> =
                parser(v).fold(
                    onSuccess = { SerdeResult.success(it) },
                    onFailure = { SerdeResult.failure(SerdeError.custom(it.message ?: it.toString())) },
                )
        }

    fun <Ok> serialize(value: T, serializer: Serializer<Ok>): SerdeResult<Ok> {
        val str =
            formatter(value).getOrElse {
                return SerdeResult.failure(SerdeError.custom(it.message ?: it.toString()))
            }
        return serializer.serializeStr(str)
    }

    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<T> =
        deserializer.deserializeStr(valueVisitor)

    val option: OptionSerde = OptionSerde()

    inner class OptionSerde : Deserialize<T?> {
        private val optionVisitor =
            object : Visitor<T?> {
                override fun expecting(): String =
                    if (formatDescriptionString != null) {
                        "an `Option<$typeName>` in the format \"$formatDescriptionString\""
                    } else {
                        "an `Option<$typeName>`"
                    }

                override fun <D : Deserializer> visitSome(deserializer: D): SerdeResult<T?> =
                    deserializer.deserializeStr(valueVisitor).map { it }

                override fun visitNone(): SerdeResult<T?> = SerdeResult.success(null)

                override fun visitUnit(): SerdeResult<T?> = SerdeResult.success(null)
            }

        fun <Ok> serialize(option: T?, serializer: Serializer<Ok>): SerdeResult<Ok> {
            if (option == null) return serializer.serializeNone()
            val str =
                formatter(option).getOrElse {
                    return SerdeResult.failure(SerdeError.custom(it.message ?: it.toString()))
                }
            return serializer.serializeSome(StringSerialize(str))
        }

        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<T?> =
            deserializer.deserializeOption(optionVisitor)
    }
}

fun formatDescriptionDate(
    format: String,
    version: Int = 2,
): FormatDescriptionSerde<Date> {
    val items: OwnedFormatItem = parseOwned(version, format).getOrThrow()
    return FormatDescriptionSerde(
        typeName = "Date",
        formatDescriptionString = format,
        formatter = { it.format(items) },
        parser = { Date.parse(it, items) },
    )
}

fun formatDescriptionDate(description: List<BorrowedFormatItem>): FormatDescriptionSerde<Date> =
    FormatDescriptionSerde(
        typeName = "Date",
        formatDescriptionString = null,
        formatter = { it.format(description) },
        parser = { Date.parse(it, description) },
    )

fun <F> formatDescriptionDate(description: F): FormatDescriptionSerde<Date>
    where F : Formattable, F : Parsable =
    FormatDescriptionSerde(
        typeName = "Date",
        formatDescriptionString = null,
        formatter = { it.format(description) },
        parser = { Date.parse(it, description) },
    )

fun formatDescriptionTime(
    format: String,
    version: Int = 2,
): FormatDescriptionSerde<Time> {
    val items: OwnedFormatItem = parseOwned(version, format).getOrThrow()
    return FormatDescriptionSerde(
        typeName = "Time",
        formatDescriptionString = format,
        formatter = { it.format(items) },
        parser = { Time.parse(it, items) },
    )
}

fun formatDescriptionTime(description: List<BorrowedFormatItem>): FormatDescriptionSerde<Time> =
    FormatDescriptionSerde(
        typeName = "Time",
        formatDescriptionString = null,
        formatter = { it.format(description) },
        parser = { Time.parse(it, description) },
    )

fun <F> formatDescriptionTime(description: F): FormatDescriptionSerde<Time>
    where F : Formattable, F : Parsable =
    FormatDescriptionSerde(
        typeName = "Time",
        formatDescriptionString = null,
        formatter = { it.format(description) },
        parser = { Time.parse(it, description) },
    )

fun formatDescriptionUtcOffset(
    format: String,
    version: Int = 2,
): FormatDescriptionSerde<UtcOffset> {
    val items: OwnedFormatItem = parseOwned(version, format).getOrThrow()
    return FormatDescriptionSerde(
        typeName = "UtcOffset",
        formatDescriptionString = format,
        formatter = { it.format(items) },
        parser = { UtcOffset.parse(it, items) },
    )
}

fun formatDescriptionUtcOffset(description: List<BorrowedFormatItem>): FormatDescriptionSerde<UtcOffset> =
    FormatDescriptionSerde(
        typeName = "UtcOffset",
        formatDescriptionString = null,
        formatter = { it.format(description) },
        parser = { UtcOffset.parse(it, description) },
    )

fun <F> formatDescriptionUtcOffset(description: F): FormatDescriptionSerde<UtcOffset>
    where F : Formattable, F : Parsable =
    FormatDescriptionSerde(
        typeName = "UtcOffset",
        formatDescriptionString = null,
        formatter = { it.format(description) },
        parser = { UtcOffset.parse(it, description) },
    )

fun formatDescriptionPrimitiveDateTime(
    format: String,
    version: Int = 2,
): FormatDescriptionSerde<PrimitiveDateTime> {
    val items: OwnedFormatItem = parseOwned(version, format).getOrThrow()
    return FormatDescriptionSerde(
        typeName = "PrimitiveDateTime",
        formatDescriptionString = format,
        formatter = { it.format(items) },
        parser = { PrimitiveDateTime.parse(it, items) },
    )
}

fun formatDescriptionPrimitiveDateTime(description: List<BorrowedFormatItem>): FormatDescriptionSerde<PrimitiveDateTime> =
    FormatDescriptionSerde(
        typeName = "PrimitiveDateTime",
        formatDescriptionString = null,
        formatter = { it.format(description) },
        parser = { PrimitiveDateTime.parse(it, description) },
    )

fun <F> formatDescriptionPrimitiveDateTime(description: F): FormatDescriptionSerde<PrimitiveDateTime>
    where F : Formattable, F : Parsable =
    FormatDescriptionSerde(
        typeName = "PrimitiveDateTime",
        formatDescriptionString = null,
        formatter = { it.format(description) },
        parser = { PrimitiveDateTime.parse(it, description) },
    )

fun formatDescriptionUtcDateTime(
    format: String,
    version: Int = 2,
): FormatDescriptionSerde<UtcDateTime> {
    val items: OwnedFormatItem = parseOwned(version, format).getOrThrow()
    return FormatDescriptionSerde(
        typeName = "UtcDateTime",
        formatDescriptionString = format,
        formatter = { it.format(items) },
        parser = { UtcDateTime.parse(it, items) },
    )
}

fun formatDescriptionUtcDateTime(description: List<BorrowedFormatItem>): FormatDescriptionSerde<UtcDateTime> =
    FormatDescriptionSerde(
        typeName = "UtcDateTime",
        formatDescriptionString = null,
        formatter = { it.format(description) },
        parser = { UtcDateTime.parse(it, description) },
    )

fun <F> formatDescriptionUtcDateTime(description: F): FormatDescriptionSerde<UtcDateTime>
    where F : Formattable, F : Parsable =
    FormatDescriptionSerde(
        typeName = "UtcDateTime",
        formatDescriptionString = null,
        formatter = { it.format(description) },
        parser = { UtcDateTime.parse(it, description) },
    )

fun formatDescriptionOffsetDateTime(
    format: String,
    version: Int = 2,
): FormatDescriptionSerde<OffsetDateTime> {
    val items: OwnedFormatItem = parseOwned(version, format).getOrThrow()
    return FormatDescriptionSerde(
        typeName = "OffsetDateTime",
        formatDescriptionString = format,
        formatter = { it.format(items) },
        parser = { OffsetDateTime.parse(it, items) },
    )
}

fun formatDescriptionOffsetDateTime(description: List<BorrowedFormatItem>): FormatDescriptionSerde<OffsetDateTime> =
    FormatDescriptionSerde(
        typeName = "OffsetDateTime",
        formatDescriptionString = null,
        formatter = { it.format(description) },
        parser = { OffsetDateTime.parse(it, description) },
    )

fun <F> formatDescriptionOffsetDateTime(description: F): FormatDescriptionSerde<OffsetDateTime>
    where F : Formattable, F : Parsable =
    FormatDescriptionSerde(
        typeName = "OffsetDateTime",
        formatDescriptionString = null,
        formatter = { it.format(description) },
        parser = { OffsetDateTime.parse(it, description) },
    )

