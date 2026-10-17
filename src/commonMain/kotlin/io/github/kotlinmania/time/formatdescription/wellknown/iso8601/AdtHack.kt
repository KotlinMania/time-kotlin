// port-lint: source format_description/well_known/iso8601/adt_hack.rs
package io.github.kotlinmania.time.formatdescription.wellknown.iso8601

import io.github.kotlinmania.time.formatdescription.wellknown.Config
import io.github.kotlinmania.time.formatdescription.wellknown.DateKind
import io.github.kotlinmania.time.formatdescription.wellknown.FormattedComponents
import io.github.kotlinmania.time.formatdescription.wellknown.Iso8601
import io.github.kotlinmania.time.formatdescription.wellknown.OffsetPrecision
import io.github.kotlinmania.time.formatdescription.wellknown.TimePrecision

/**
 * Hackery to work around not being able to use ADTs in const generics on stable.
 */

@PublishedApi
internal typealias DoNotRelyOnWhatThisIs = EncodedConfig

/**
 * An encoded [Config] that can be used as a configuration parameter to [Iso8601].
 *
 * The type this is aliased to must not be relied upon. It can change in any release without
 * notice.
 */
class EncodedConfig internal constructor(
    private val bytes: ByteArray,
) {
    init {
        require(bytes.size == BYTES_LEN) { "invalid configuration" }
    }

    internal fun toBeBytes(): ByteArray = bytes.copyOf()

    override fun equals(other: Any?): Boolean =
        other is EncodedConfig && bytes.contentEquals(other.bytes)

    override fun hashCode(): Int = bytes.contentHashCode()

    override fun toString(): String =
        "0x" + bytes.joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }

    companion object {
        internal const val BITS: Int = 128
        internal const val BYTES_LEN: Int = BITS / 8

        internal fun fromBeBytes(bytes: ByteArray): EncodedConfig =
            EncodedConfig(bytes.copyOf())

        internal fun fromHighLow(high: ULong, low: ULong): EncodedConfig {
            val b = ByteArray(16)
            for (i in 0 until 8) {
                b[i] = ((high shr ((7 - i) * 8)) and 0xFFuL).toByte()
                b[8 + i] = ((low shr ((7 - i) * 8)) and 0xFFuL).toByte()
            }
            return EncodedConfig(b)
        }
    }
}

/** The user-provided configuration for the ISO 8601 format. */
internal val Iso8601.decodedConfig: Config
    get() = Config.decode(config)

/** Whether the date should be formatted. */
internal val Iso8601.formatDate: Boolean
    get() =
        when (decodedConfig.formattedComponents) {
            FormattedComponents.Date,
            FormattedComponents.DateTime,
            FormattedComponents.DateTimeOffset,
            -> true
            else -> false
        }

/** Whether the time should be formatted. */
internal val Iso8601.formatTime: Boolean
    get() =
        when (decodedConfig.formattedComponents) {
            FormattedComponents.Time,
            FormattedComponents.DateTime,
            FormattedComponents.DateTimeOffset,
            FormattedComponents.TimeOffset,
            -> true
            else -> false
        }

/** Whether the UTC offset should be formatted. */
internal val Iso8601.formatOffset: Boolean
    get() =
        when (decodedConfig.formattedComponents) {
            FormattedComponents.Offset,
            FormattedComponents.DateTimeOffset,
            FormattedComponents.TimeOffset,
            -> true
            else -> false
        }

/** Whether the year is six digits. */
internal val Iso8601.yearIsSixDigits: Boolean
    get() = decodedConfig.yearIsSixDigits

/** Whether the format contains separators (such as `-` or `:`). */
internal val Iso8601.useSeparators: Boolean
    get() = decodedConfig.useSeparators

/** Which format to use for the date. */
internal val Iso8601.dateKind: DateKind
    get() = decodedConfig.dateKind

/** The precision and number of decimal digits to use for the time. */
internal val Iso8601.timePrecision: TimePrecision
    get() = decodedConfig.timePrecision

/** The precision for the UTC offset. */
internal val Iso8601.offsetPrecision: OffsetPrecision
    get() = decodedConfig.offsetPrecision

internal fun encodeConfig(config: Config): EncodedConfig {
    val bytes = ByteArray(EncodedConfig.BYTES_LEN)

    bytes[0] =
        when (config.formattedComponents) {
            FormattedComponents.None -> 0
            FormattedComponents.Date -> 1
            FormattedComponents.Time -> 2
            FormattedComponents.Offset -> 3
            FormattedComponents.DateTime -> 4
            FormattedComponents.DateTimeOffset -> 5
            FormattedComponents.TimeOffset -> 6
        }
    bytes[1] = if (config.useSeparators) 1 else 0
    bytes[2] = if (config.yearIsSixDigits) 1 else 0
    bytes[3] =
        when (config.dateKind) {
            DateKind.Calendar -> 0
            DateKind.Week -> 1
            DateKind.Ordinal -> 2
        }
    bytes[4] =
        when (config.timePrecision) {
            is TimePrecision.Hour -> 0
            is TimePrecision.Minute -> 1
            is TimePrecision.Second -> 2
        }
    bytes[5] =
        when (val tp = config.timePrecision) {
            is TimePrecision.Hour -> tp.decimalDigits?.toByte() ?: 0
            is TimePrecision.Minute -> tp.decimalDigits?.toByte() ?: 0
            is TimePrecision.Second -> tp.decimalDigits?.toByte() ?: 0
        }
    bytes[6] =
        when (config.offsetPrecision) {
            OffsetPrecision.Hour -> 0
            OffsetPrecision.Minute -> 1
        }

    return EncodedConfig.fromBeBytes(bytes)
}

internal fun decodeConfig(encoded: EncodedConfig): Config {
    val bytes = encoded.toBeBytes()

    val formattedComponents =
        when (bytes[0].toInt() and 0xFF) {
            0 -> FormattedComponents.None
            1 -> FormattedComponents.Date
            2 -> FormattedComponents.Time
            3 -> FormattedComponents.Offset
            4 -> FormattedComponents.DateTime
            5 -> FormattedComponents.DateTimeOffset
            6 -> FormattedComponents.TimeOffset
            else -> throw IllegalArgumentException("invalid configuration")
        }
    val useSeparators =
        when (bytes[1].toInt() and 0xFF) {
            0 -> false
            1 -> true
            else -> throw IllegalArgumentException("invalid configuration")
        }
    val yearIsSixDigits =
        when (bytes[2].toInt() and 0xFF) {
            0 -> false
            1 -> true
            else -> throw IllegalArgumentException("invalid configuration")
        }
    val dateKind =
        when (bytes[3].toInt() and 0xFF) {
            0 -> DateKind.Calendar
            1 -> DateKind.Week
            2 -> DateKind.Ordinal
            else -> throw IllegalArgumentException("invalid configuration")
        }
    val decimalDigitsByte = (bytes[5].toInt() and 0xFF).toUByte()
    val decimalDigits: UByte? = if (decimalDigitsByte == 0.toUByte()) null else decimalDigitsByte
    val timePrecision =
        when (bytes[4].toInt() and 0xFF) {
            0 -> TimePrecision.Hour(decimalDigits = decimalDigits)
            1 -> TimePrecision.Minute(decimalDigits = decimalDigits)
            2 -> TimePrecision.Second(decimalDigits = decimalDigits)
            else -> throw IllegalArgumentException("invalid configuration")
        }
    val offsetPrecision =
        when (bytes[6].toInt() and 0xFF) {
            0 -> OffsetPrecision.Hour
            1 -> OffsetPrecision.Minute
            else -> throw IllegalArgumentException("invalid configuration")
        }

    var idx = 7
    while (idx < EncodedConfig.BYTES_LEN) {
        if (bytes[idx].toInt() != 0) {
            throw IllegalArgumentException("invalid configuration")
        }
        idx += 1
    }

    return Config(
        formattedComponents = formattedComponents,
        useSeparators = useSeparators,
        yearIsSixDigits = yearIsSixDigits,
        dateKind = dateKind,
        timePrecision = timePrecision,
        offsetPrecision = offsetPrecision,
    )
}
