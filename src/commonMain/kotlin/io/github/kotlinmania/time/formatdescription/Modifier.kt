// port-lint: source format_description/modifier.rs
package io.github.kotlinmania.time.formatdescription

/** Various modifiers for components. */

/** Day of the month. */
data class Day(
    /** The padding to obtain the minimum width. */
    val padding: Padding = Padding.Zero,
) {
    /** Set the padding type. */
    fun withPadding(padding: Padding): Day = copy(padding = padding)

    companion object {
        /** Creates a modifier that indicates the value is padded with zeroes. */
        fun default(): Day = Day()
    }
}

/** The representation of a month. */
enum class MonthRepr {
    /** The number of the month, where January is 1 and December is 12. */
    Numerical,

    /** The long form of the month name, such as "January". */
    Long,

    /** The short form of the month name, such as "Jan". */
    Short,
    ;

    companion object {
        /** Creates a modifier that indicates the value uses the numerical representation. */
        fun default(): MonthRepr = Numerical
    }
}

/** Month of the year. */
data class Month(
    /** The padding to obtain the minimum width. */
    val padding: Padding = Padding.Zero,
    /** What form of representation should be used? */
    val repr: MonthRepr = MonthRepr.Numerical,
    /** Is the value case sensitive when parsing? */
    val caseSensitive: Boolean = true,
) {
    /** Set the padding type. */
    fun withPadding(padding: Padding): Month = copy(padding = padding)

    /** Set the manner in which the month is represented. */
    fun withRepr(repr: MonthRepr): Month = copy(repr = repr)

    /** Set whether the value is case sensitive when parsing. */
    fun withCaseSensitive(caseSensitive: Boolean): Month = copy(caseSensitive = caseSensitive)

    companion object {
        /**
         * Creates an instance that uses the numerical representation, is padded with zeroes, and
         * is case-sensitive when parsing.
         */
        fun default(): Month = Month()
    }
}

/** Ordinal day of the year. */
data class Ordinal(
    /** The padding to obtain the minimum width. */
    val padding: Padding = Padding.Zero,
) {
    /** Set the padding type. */
    fun withPadding(padding: Padding): Ordinal = copy(padding = padding)

    companion object {
        /** Creates a modifier that indicates the value is padded with zeroes. */
        fun default(): Ordinal = Ordinal()
    }
}

/** The representation used for the day of the week. */
enum class WeekdayRepr {
    /** The short form of the weekday, such as "Mon". */
    Short,

    /** The long form of the weekday, such as "Monday". */
    Long,

    /** A numerical representation using Sunday as the first day of the week. */
    Sunday,

    /** A numerical representation using Monday as the first day of the week. */
    Monday,
    ;

    companion object {
        /** Creates a modifier that indicates the value uses the long representation. */
        fun default(): WeekdayRepr = Long
    }
}

/** Day of the week. */
data class Weekday(
    /** What form of representation should be used? */
    val repr: WeekdayRepr = WeekdayRepr.Long,
    /** When using a numerical representation, should it be zero or one-indexed? */
    val oneIndexed: Boolean = true,
    /** Is the value case sensitive when parsing? */
    val caseSensitive: Boolean = true,
) {
    /** Set the manner in which the weekday is represented. */
    fun withRepr(repr: WeekdayRepr): Weekday = copy(repr = repr)

    /** Set whether the value is one-indexed when using a numerical representation. */
    fun withOneIndexed(oneIndexed: Boolean): Weekday = copy(oneIndexed = oneIndexed)

    /** Set whether the value is case sensitive when parsing. */
    fun withCaseSensitive(caseSensitive: Boolean): Weekday = copy(caseSensitive = caseSensitive)

    companion object {
        /**
         * Creates a modifier that uses the long representation and is case-sensitive when parsing.
         * If the representation is changed to a numerical one, the instance defaults to one-based
         * indexing.
         */
        fun default(): Weekday = Weekday()
    }
}

/** The representation used for the week number. */
enum class WeekNumberRepr {
    /** Week 1 is the week that contains January 4. */
    Iso,

    /** Week 1 begins on the first Sunday of the calendar year. */
    Sunday,

    /** Week 1 begins on the first Monday of the calendar year. */
    Monday,
    ;

    companion object {
        /** Creates a modifier that indicates that the value uses the ISO representation. */
        fun default(): WeekNumberRepr = Iso
    }
}

/** Week within the year. */
data class WeekNumber(
    /** The padding to obtain the minimum width. */
    val padding: Padding = Padding.Zero,
    /** What kind of representation should be used? */
    val repr: WeekNumberRepr = WeekNumberRepr.Iso,
) {
    /** Set the padding type. */
    fun withPadding(padding: Padding): WeekNumber = copy(padding = padding)

    /** Set the manner in which the week number is represented. */
    fun withRepr(repr: WeekNumberRepr): WeekNumber = copy(repr = repr)

    companion object {
        /** Creates a modifier that is padded with zeroes and uses the ISO representation. */
        fun default(): WeekNumber = WeekNumber()
    }
}

/** The representation used for a year value. */
enum class YearRepr {
    /** The full value of the year. */
    Full,

    /** All digits except the last two. Includes the sign, if any. */
    Century,

    /** Only the last two digits of the year. */
    LastTwo,
    ;

    companion object {
        /** Creates a modifier that indicates the value uses the full representation. */
        fun default(): YearRepr = Full
    }
}

/**
 * The range of years that are supported.
 *
 * This modifier has no effect when the year representation is `LastTwo`.
 */
enum class YearRange {
    /** Years between -9999 and 9999 are supported. */
    Standard,

    /**
     * Years between -999_999 and 999_999 are supported, with the sign being required if the year
     * contains more than four digits.
     */
    Extended,
    ;

    companion object {
        /** Creates a modifier that indicates the value uses the extended range. */
        fun default(): YearRange = Extended
    }
}

/** Year of the date. */
data class Year(
    /** The padding to obtain the minimum width. */
    val padding: Padding = Padding.Zero,
    /** What kind of representation should be used? */
    val repr: YearRepr = YearRepr.Full,
    /** What range of years is supported? */
    val range: YearRange = YearRange.Extended,
    /** Whether the value is based on the ISO week number or the Gregorian calendar. */
    val isoWeekBased: Boolean = false,
    /** Whether the plus sign is present when a positive year contains fewer than five digits. */
    val signIsMandatory: Boolean = false,
) {
    /** Set the padding type. */
    fun withPadding(padding: Padding): Year = copy(padding = padding)

    /** Set the manner in which the year is represented. */
    fun withRepr(repr: YearRepr): Year = copy(repr = repr)

    /** Set the range of years that are supported. */
    fun withRange(range: YearRange): Year = copy(range = range)

    /** Set whether the year is based on the ISO week number. */
    fun withIsoWeekBased(isoWeekBased: Boolean): Year = copy(isoWeekBased = isoWeekBased)

    /** Set whether the plus sign is mandatory for positive years with fewer than five digits. */
    fun withSignIsMandatory(signIsMandatory: Boolean): Year = copy(signIsMandatory = signIsMandatory)

    companion object {
        /**
         * Creates a modifier that uses the full representation, is padded with zeroes, uses the
         * Gregorian calendar as its base, and only includes the year's sign if necessary.
         */
        fun default(): Year = Year()
    }
}

/** Hour of the day. */
data class Hour(
    /** The padding to obtain the minimum width. */
    val padding: Padding = Padding.Zero,
    /** Is the hour displayed using a 12 or 24-hour clock? */
    val is12HourClock: Boolean = false,
) {
    /** Set the padding type. */
    fun withPadding(padding: Padding): Hour = copy(padding = padding)

    /** Set whether the hour uses a 12-hour clock. */
    fun withIs12HourClock(is12HourClock: Boolean): Hour = copy(is12HourClock = is12HourClock)

    companion object {
        /** Creates a modifier that is padded with zeroes and has the 24-hour representation. */
        fun default(): Hour = Hour()
    }
}

/** Minute within the hour. */
data class Minute(
    /** The padding to obtain the minimum width. */
    val padding: Padding = Padding.Zero,
) {
    /** Set the padding type. */
    fun withPadding(padding: Padding): Minute = copy(padding = padding)

    companion object {
        /** Creates a modifier that indicates the value is padded with zeroes. */
        fun default(): Minute = Minute()
    }
}

/** AM/PM part of the time. */
data class Period(
    /** Is the period uppercase or lowercase? */
    val isUppercase: Boolean = true,
    /** Is the value case sensitive when parsing? */
    val caseSensitive: Boolean = true,
) {
    /** Set whether the period is uppercase. */
    fun withIsUppercase(isUppercase: Boolean): Period = copy(isUppercase = isUppercase)

    /** Set whether the value is case sensitive when parsing. */
    fun withCaseSensitive(caseSensitive: Boolean): Period = copy(caseSensitive = caseSensitive)

    companion object {
        /** Creates a modifier that uses the upper-case representation and is case-sensitive. */
        fun default(): Period = Period()
    }
}

/** Second within the minute. */
data class Second(
    /** The padding to obtain the minimum width. */
    val padding: Padding = Padding.Zero,
) {
    /** Set the padding type. */
    fun withPadding(padding: Padding): Second = copy(padding = padding)

    companion object {
        /** Creates a modifier that indicates the value is padded with zeroes. */
        fun default(): Second = Second()
    }
}

/** The number of digits present in a subsecond representation. */
enum class SubsecondDigits {
    /** Exactly one digit. */
    One,

    /** Exactly two digits. */
    Two,

    /** Exactly three digits. */
    Three,

    /** Exactly four digits. */
    Four,

    /** Exactly five digits. */
    Five,

    /** Exactly six digits. */
    Six,

    /** Exactly seven digits. */
    Seven,

    /** Exactly eight digits. */
    Eight,

    /** Exactly nine digits. */
    Nine,

    /** Any number of digits up to nine that is at least one. */
    OneOrMore,
    ;

    companion object {
        /** Creates a modifier that indicates the stringified value contains one or more digits. */
        fun default(): SubsecondDigits = OneOrMore
    }
}

/** Subsecond within the second. */
data class Subsecond(
    /** How many digits are present in the component? */
    val digits: SubsecondDigits = SubsecondDigits.OneOrMore,
) {
    /** Set the number of digits present in the subsecond representation. */
    fun withDigits(digits: SubsecondDigits): Subsecond = copy(digits = digits)

    companion object {
        /** Creates a modifier that indicates the stringified value contains one or more digits. */
        fun default(): Subsecond = Subsecond()
    }
}

/** Hour of the UTC offset. */
data class OffsetHour(
    /** Whether the plus sign is present on positive values. */
    val signIsMandatory: Boolean = false,
    /** The padding to obtain the minimum width. */
    val padding: Padding = Padding.Zero,
) {
    /** Set whether the plus sign is mandatory for positive values. */
    fun withSignIsMandatory(signIsMandatory: Boolean): OffsetHour =
        copy(signIsMandatory = signIsMandatory)

    /** Set the padding type. */
    fun withPadding(padding: Padding): OffsetHour = copy(padding = padding)

    companion object {
        /** Creates a modifier that only uses a sign for negative values and is padded with zeroes. */
        fun default(): OffsetHour = OffsetHour()
    }
}

/** Minute within the hour of the UTC offset. */
data class OffsetMinute(
    /** The padding to obtain the minimum width. */
    val padding: Padding = Padding.Zero,
) {
    /** Set the padding type. */
    fun withPadding(padding: Padding): OffsetMinute = copy(padding = padding)

    companion object {
        /** Creates a modifier that indicates the value is padded with zeroes. */
        fun default(): OffsetMinute = OffsetMinute()
    }
}

/** Second within the minute of the UTC offset. */
data class OffsetSecond(
    /** The padding to obtain the minimum width. */
    val padding: Padding = Padding.Zero,
) {
    /** Set the padding type. */
    fun withPadding(padding: Padding): OffsetSecond = copy(padding = padding)

    companion object {
        /** Creates a modifier that indicates the value is padded with zeroes. */
        fun default(): OffsetSecond = OffsetSecond()
    }
}

/** Type of padding to ensure a minimum width. */
enum class Padding {
    /** A space character should be used as padding. */
    Space,

    /** A zero character should be used as padding. */
    Zero,

    /** There is no padding. */
    None,
    ;

    companion object {
        /** Creates a modifier that indicates the value is padded with zeroes. */
        fun default(): Padding = Zero
    }
}

/**
 * Ignore some number of bytes.
 *
 * This has no effect when formatting.
 */
data class Ignore(
    /** The number of bytes to ignore. */
    val count: UShort,
) {
    init {
        require(count != 0.toUShort()) { "ignore count must be non-zero" }
    }

    /** Set the number of bytes to ignore. */
    fun withCount(count: UShort): Ignore = Ignore(count)

    companion object {
        /** Create an instance with the provided number of bytes to ignore. */
        fun count(count: UShort): Ignore = Ignore(count)
    }
}

/** The precision of a Unix timestamp. */
enum class UnixTimestampPrecision {
    /** Seconds since the Unix epoch. */
    Second,

    /** Milliseconds since the Unix epoch. */
    Millisecond,

    /** Microseconds since the Unix epoch. */
    Microsecond,

    /** Nanoseconds since the Unix epoch. */
    Nanosecond,
    ;

    companion object {
        /** Creates a modifier that indicates the value represents seconds since the Unix epoch. */
        fun default(): UnixTimestampPrecision = Second
    }
}

/** A Unix timestamp. */
data class UnixTimestamp(
    /** The precision of the timestamp. */
    val precision: UnixTimestampPrecision = UnixTimestampPrecision.Second,
    /** Whether the plus sign must be present for a non-negative timestamp. */
    val signIsMandatory: Boolean = false,
) {
    /** Set the precision of the timestamp. */
    fun withPrecision(precision: UnixTimestampPrecision): UnixTimestamp = copy(precision = precision)

    /** Set whether the plus sign is mandatory for non-negative timestamps. */
    fun withSignIsMandatory(signIsMandatory: Boolean): UnixTimestamp =
        copy(signIsMandatory = signIsMandatory)

    companion object {
        /** Creates a modifier for seconds since the Unix epoch where the sign is not mandatory. */
        fun default(): UnixTimestamp = UnixTimestamp()
    }
}

/** Whether trailing input after the declared end is permitted. */
enum class TrailingInput {
    /** Trailing input is not permitted and will cause an error. */
    Prohibit,

    /** Trailing input is permitted but discarded. */
    Discard,
    ;

    companion object {
        /** Indicates that any trailing characters after the end of input are prohibited. */
        fun default(): TrailingInput = Prohibit
    }
}

/** The end of input. */
data class End(
    /** How to handle any input after this component. */
    val trailingInput: TrailingInput = TrailingInput.Prohibit,
) {
    /** Set how to handle any input after this component. */
    fun withTrailingInput(trailingInput: TrailingInput): End = copy(trailingInput = trailingInput)

    companion object {
        /** Creates a modifier used to represent the end of input, not allowing trailing input. */
        fun default(): End = End()
    }
}
