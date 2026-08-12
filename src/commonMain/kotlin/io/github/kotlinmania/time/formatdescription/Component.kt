// port-lint: source format_description/component.rs
package io.github.kotlinmania.time.formatdescription

/** Part of a format description. */

/** Indicate whether the hour is "am" or "pm". */
internal enum class PeriodCase {
    Am,
    Pm,
}

/** A component of a larger format description. */
sealed class Component {
    /** Day of the month. */
    data class Day(
        val modifier: io.github.kotlinmania.time.formatdescription.Day,
    ) : Component()

    /** Month of the year. */
    data class Month(
        val modifier: io.github.kotlinmania.time.formatdescription.Month,
    ) : Component()

    /** Ordinal day of the year. */
    data class Ordinal(
        val modifier: io.github.kotlinmania.time.formatdescription.Ordinal,
    ) : Component()

    /** Day of the week. */
    data class Weekday(
        val modifier: io.github.kotlinmania.time.formatdescription.Weekday,
    ) : Component()

    /** Week within the year. */
    data class WeekNumber(
        val modifier: io.github.kotlinmania.time.formatdescription.WeekNumber,
    ) : Component()

    /** Year of the date. */
    data class Year(
        val modifier: io.github.kotlinmania.time.formatdescription.Year,
    ) : Component()

    /** Hour of the day. */
    data class Hour(
        val modifier: io.github.kotlinmania.time.formatdescription.Hour,
    ) : Component()

    /** Minute within the hour. */
    data class Minute(
        val modifier: io.github.kotlinmania.time.formatdescription.Minute,
    ) : Component()

    /** AM/PM part of the time. */
    data class Period(
        val modifier: io.github.kotlinmania.time.formatdescription.Period,
    ) : Component()

    /** Second within the minute. */
    data class Second(
        val modifier: io.github.kotlinmania.time.formatdescription.Second,
    ) : Component()

    /** Subsecond within the second. */
    data class Subsecond(
        val modifier: io.github.kotlinmania.time.formatdescription.Subsecond,
    ) : Component()

    /** Hour of the UTC offset. */
    data class OffsetHour(
        val modifier: io.github.kotlinmania.time.formatdescription.OffsetHour,
    ) : Component()

    /** Minute within the hour of the UTC offset. */
    data class OffsetMinute(
        val modifier: io.github.kotlinmania.time.formatdescription.OffsetMinute,
    ) : Component()

    /** Second within the minute of the UTC offset. */
    data class OffsetSecond(
        val modifier: io.github.kotlinmania.time.formatdescription.OffsetSecond,
    ) : Component()

    /** A number of bytes to ignore when parsing. This has no effect on formatting. */
    data class Ignore(
        val modifier: io.github.kotlinmania.time.formatdescription.Ignore,
    ) : Component()

    /** A Unix timestamp. */
    data class UnixTimestamp(
        val modifier: io.github.kotlinmania.time.formatdescription.UnixTimestamp,
    ) : Component()

    /**
     * The end of input. Parsing this component will fail if there is any input remaining. This
     * component neither affects formatting nor consumes any input when parsing.
     */
    data class End(
        val modifier: io.github.kotlinmania.time.formatdescription.End,
    ) : Component()
}
