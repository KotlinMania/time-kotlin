// port-lint: source format_description/well_known/iso8601.rs
package io.github.kotlinmania.time.formatdescription.wellknown

import io.github.kotlinmania.time.formatdescription.wellknown.iso8601.EncodedConfig
import io.github.kotlinmania.time.formatting.Formattable
import io.github.kotlinmania.time.parsing.Parsable
import io.github.kotlinmania.time.formatdescription.wellknown.iso8601.decode as decodeIso8601Config
import io.github.kotlinmania.time.formatdescription.wellknown.iso8601.encode as encodeIso8601Config

/**
 * The format described in ISO 8601.
 *
 * This implementation is of ISO 8601-1:2019. It may not be compatible with other versions.
 *
 * The `config` parameter **must** be a value that was returned by [Config.encode].
 * Passing any other value is **unspecified behavior**.
 *
 * Example: 1997-11-21T09:55:06.000000000-06:00
 */
data class Iso8601(
    val config: EncodedConfig = Config.DEFAULT.encode(),
) : Formattable,
    Parsable {
    constructor(config: Config) : this(config.encode())

    override fun toString(): String = "Iso8601(config = ${Config.decode(config)})"

    companion object {
        /**
         * An [Iso8601] with the default configuration.
         *
         * The following is the default behavior:
         * - The configuration can be used for both formatting and parsing.
         * - The date, time, and UTC offset are all formatted.
         * - Separators (such as `-` and `:`) are included.
         * - The year contains four digits, such that the year must be between 0 and 9999.
         * - The date uses the calendar format.
         * - The time has precision to the second and nine decimal digits.
         * - The UTC offset has precision to the minute.
         */
        val DEFAULT: Iso8601 = Iso8601(Config.DEFAULT.encode())

        /**
         * An [Iso8601] that can only be used for parsing. Using this to format a value is
         * unspecified behavior.
         */
        val PARSING: Iso8601 = Iso8601(Config.PARSING.encode())

        /**
         * An [Iso8601] that handles only the date, but is otherwise the same as [Config.DEFAULT].
         */
        val DATE: Iso8601 =
            Iso8601(Config.DEFAULT.setFormattedComponents(FormattedComponents.Date).encode())

        /**
         * An [Iso8601] that handles only the time, but is otherwise the same as [Config.DEFAULT].
         */
        val TIME: Iso8601 =
            Iso8601(Config.DEFAULT.setFormattedComponents(FormattedComponents.Time).encode())

        /**
         * An [Iso8601] that handles only the UTC offset, but is otherwise the same as
         * [Config.DEFAULT].
         */
        val OFFSET: Iso8601 =
            Iso8601(Config.DEFAULT.setFormattedComponents(FormattedComponents.Offset).encode())

        /**
         * An [Iso8601] that handles the date and time, but is otherwise the same as
         * [Config.DEFAULT].
         */
        val DATE_TIME: Iso8601 =
            Iso8601(Config.DEFAULT.setFormattedComponents(FormattedComponents.DateTime).encode())

        /**
         * An [Iso8601] that handles the date, time, and UTC offset. This is the same as
         * [Config.DEFAULT].
         */
        val DATE_TIME_OFFSET: Iso8601 = Iso8601(Config.DEFAULT.encode())

        /**
         * An [Iso8601] that handles the time and UTC offset, but is otherwise the same as
         * [Config.DEFAULT].
         */
        val TIME_OFFSET: Iso8601 =
            Iso8601(Config.DEFAULT.setFormattedComponents(FormattedComponents.TimeOffset).encode())
    }
}

/** Which components to format. */
enum class FormattedComponents {
    /**
     * The configuration can only be used for parsing. Using this to format a value is
     * unspecified behavior.
     */
    None,

    /** Format only the date. */
    Date,

    /** Format only the time. */
    Time,

    /** Format only the UTC offset. */
    Offset,

    /** Format the date and time. */
    DateTime,

    /** Format the date, time, and UTC offset. */
    DateTimeOffset,

    /** Format the time and UTC offset. */
    TimeOffset,
}

/** Which format to use for the date. */
enum class DateKind {
    /** Use the year-month-day format. */
    Calendar,

    /** Use the year-week-weekday format. */
    Week,

    /** Use the week-ordinal format. */
    Ordinal,
}

/** The precision and number of decimal digits present for the time. */
sealed class TimePrecision {
    /**
     * Format the hour only. Minutes, seconds, and nanoseconds will be represented with the
     * specified number of decimal digits, if any.
     */
    data class Hour(
        val decimalDigits: UByte? = null,
    ) : TimePrecision() {
        init {
            require(decimalDigits == null || decimalDigits != 0.toUByte()) {
                "decimalDigits must be non-zero"
            }
        }
    }

    /**
     * Format the hour and minute. Seconds and nanoseconds will be represented with the specified
     * number of decimal digits, if any.
     */
    data class Minute(
        val decimalDigits: UByte? = null,
    ) : TimePrecision() {
        init {
            require(decimalDigits == null || decimalDigits != 0.toUByte()) {
                "decimalDigits must be non-zero"
            }
        }
    }

    /**
     * Format the hour, minute, and second. Nanoseconds will be represented with the specified
     * number of decimal digits, if any.
     */
    data class Second(
        val decimalDigits: UByte? = null,
    ) : TimePrecision() {
        init {
            require(decimalDigits == null || decimalDigits != 0.toUByte()) {
                "decimalDigits must be non-zero"
            }
        }
    }
}

/** The precision for the UTC offset. */
enum class OffsetPrecision {
    /** Format only the offset hour. Requires the offset minute to be zero. */
    Hour,

    /** Format both the offset hour and minute. */
    Minute,
}

/** Configuration for [Iso8601]. */
@ConsistentCopyVisibility
data class Config internal constructor(
    /** Which components, if any, will be formatted. */
    internal val formattedComponents: FormattedComponents,
    /** Whether the format contains separators (such as `-` or `:`). */
    internal val useSeparators: Boolean,
    /** Whether the year is six digits. */
    internal val yearIsSixDigits: Boolean,
    /** The format used for the date. */
    internal val dateKind: DateKind,
    /** The precision and number of decimal digits present for the time. */
    internal val timePrecision: TimePrecision,
    /** The precision for the UTC offset. */
    internal val offsetPrecision: OffsetPrecision,
) {
    /** Set whether to format the date, time, and/or UTC offset. */
    fun setFormattedComponents(formattedComponents: FormattedComponents): Config =
        copy(formattedComponents = formattedComponents)

    /** Set whether the format contains separators (such as `-` or `:`). */
    fun setUseSeparators(useSeparators: Boolean): Config =
        copy(useSeparators = useSeparators)

    /** Set whether the year is six digits. */
    fun setYearIsSixDigits(yearIsSixDigits: Boolean): Config =
        copy(yearIsSixDigits = yearIsSixDigits)

    /** Set the format used for the date. */
    fun setDateKind(dateKind: DateKind): Config =
        copy(dateKind = dateKind)

    /** Set the precision and number of decimal digits present for the time. */
    fun setTimePrecision(timePrecision: TimePrecision): Config =
        copy(timePrecision = timePrecision)

    /** Set the precision for the UTC offset. */
    fun setOffsetPrecision(offsetPrecision: OffsetPrecision): Config =
        copy(offsetPrecision = offsetPrecision)

    /**
     * Encode the configuration, permitting it to be used as a configuration parameter of [Iso8601].
     */
    fun encode(): EncodedConfig = encodeIso8601Config(this)

    companion object {
        /**
         * A configuration for the [Iso8601] format.
         */
        val DEFAULT: Config =
            Config(
                formattedComponents = FormattedComponents.DateTimeOffset,
                useSeparators = true,
                yearIsSixDigits = false,
                dateKind = DateKind.Calendar,
                timePrecision = TimePrecision.Second(decimalDigits = 9.toUByte()),
                offsetPrecision = OffsetPrecision.Minute,
            )

        /**
         * A configuration that can only be used for parsing. Using this to format a value is
         * unspecified behavior.
         */
        internal val PARSING: Config =
            Config(
                formattedComponents = FormattedComponents.None,
                useSeparators = false,
                yearIsSixDigits = false,
                dateKind = DateKind.Calendar,
                timePrecision = TimePrecision.Hour(decimalDigits = null),
                offsetPrecision = OffsetPrecision.Hour,
            )

        /**
         * Decode the configuration. The configuration must have been generated from
         * [Config.encode].
         */
        internal fun decode(encoded: EncodedConfig): Config = decodeIso8601Config(encoded)
    }
}
