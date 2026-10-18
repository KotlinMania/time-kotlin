// port-lint: source time.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serde.serdeCatching
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.time.error.ComponentRange
import io.github.kotlinmania.time.formatdescription.BorrowedFormatItem
import io.github.kotlinmania.time.formatting.BorrowedFormatItemSliceFormattable
import io.github.kotlinmania.time.formatting.Formattable
import io.github.kotlinmania.time.parsing.BorrowedFormatItemSliceParsable
import io.github.kotlinmania.time.parsing.Parsable
import io.github.kotlinmania.time.serde.TIME_FORMAT
import io.github.kotlinmania.time.serde.TimeVisitor
import io.github.kotlinmania.time.serde.U8Serialize
import io.github.kotlinmania.time.serde.U32Serialize

/**
 * The clock time within a given date. Nanosecond precision.
 *
 * All minutes are assumed to have exactly 60 seconds; no attempt is made to handle leap seconds
 * (either positive or negative).
 *
 * When comparing two [Time]s, they are assumed to be in the same calendar date.
 */
class Time internal constructor(
    private val hour: Int,
    private val minute: Int,
    private val second: Int,
    private val nanosecond: Int,
    internal val padding: Padding = Padding.Optimize,
) : Comparable<Time>, Serialize {
    init {
        require(hour in 0..23) { "hour must be in range 0..=23, got $hour" }
        require(minute in 0..59) { "minute must be in range 0..=59, got $minute" }
        require(second in 0..59) { "second must be in range 0..=59, got $second" }
        require(nanosecond in 0..999_999_999) {
            "nanosecond must be in range 0..=999_999_999, got $nanosecond"
        }
    }

    internal fun asU64(): ULong =
        (hour.toULong() shl 48) or
            (minute.toULong() shl 40) or
            (second.toULong() shl 32) or
            nanosecond.toULong()

    /** Get the clock hour, minute, and second. */
    fun asHms(): Triple<Int, Int, Int> = Triple(hour, minute, second)

    /** Get the clock hour, minute, second, and millisecond. */
    fun asHmsMilli(): List<Int> = listOf(hour, minute, second, millisecond())

    /** Get the clock hour, minute, second, and microsecond. */
    fun asHmsMicro(): List<Int> = listOf(hour, minute, second, microsecond())

    /** Get the clock hour, minute, second, and nanosecond. */
    fun asHmsNano(): List<Int> = listOf(hour, minute, second, nanosecond)

    /** Get the clock hour. */
    fun hour(): Int = hour

    /** Get the minute within the hour. */
    fun minute(): Int = minute

    /** Get the second within the minute. */
    fun second(): Int = second

    /** Get the milliseconds within the second. */
    fun millisecond(): Int = nanosecond / 1_000_000

    /** Get the microseconds within the second. */
    fun microsecond(): Int = nanosecond / 1_000

    /** Get the nanoseconds within the second. */
    fun nanosecond(): Int = nanosecond

    /** Determine the [Duration] that, if added to `this`, would result in `other`. */
    fun durationUntil(other: Time): Duration {
        var nanos = other.nanosecond - this.nanosecond
        val seconds = other.second - this.second
        val minutes = other.minute - this.minute
        val hours = other.hour - this.hour

        var totalSeconds = hours * 3600 + minutes * 60 + seconds
        if (nanos < 0) {
            nanos += 1_000_000_000
            totalSeconds -= 1
        } else if (nanos >= 1_000_000_000) {
            nanos -= 1_000_000_000
            totalSeconds += 1
        }

        if (totalSeconds < 0) {
            totalSeconds += 86_400
        }

        return Duration.new(totalSeconds.toLong(), nanos)
    }

    /** Determine the [Duration] that, if added to `other`, would result in `this`. */
    fun durationSince(other: Time): Duration = other.durationUntil(this)

    internal fun adjustingAdd(duration: Duration): Pair<DateAdjustment, Time> {
        var nanos = nanosecond + duration.subsecNanoseconds()
        var secs = second + (duration.wholeSeconds() % 60).toInt()
        var mins = minute + (duration.wholeMinutes() % 60).toInt()
        var hrs = hour + (duration.wholeHours() % 24).toInt()
        var adj = DateAdjustment.None

        if (nanos < 0) {
            nanos += 1_000_000_000
            secs -= 1
        } else if (nanos >= 1_000_000_000) {
            nanos -= 1_000_000_000
            secs += 1
        }

        if (secs < 0) {
            secs += 60
            mins -= 1
        } else if (secs >= 60) {
            secs -= 60
            mins += 1
        }

        if (mins < 0) {
            mins += 60
            hrs -= 1
        } else if (mins >= 60) {
            mins -= 60
            hrs += 1
        }

        if (hrs >= 24) {
            hrs -= 24
            adj = DateAdjustment.Next
        } else if (hrs < 0) {
            hrs += 24
            adj = DateAdjustment.Previous
        }

        return Pair(adj, fromHmsNanosUnchecked(hrs, mins, secs, nanos))
    }

    internal fun adjustingSub(duration: Duration): Pair<DateAdjustment, Time> {
        var nanos = nanosecond - duration.subsecNanoseconds()
        var secs = second - (duration.wholeSeconds() % 60).toInt()
        var mins = minute - (duration.wholeMinutes() % 60).toInt()
        var hrs = hour - (duration.wholeHours() % 24).toInt()
        var adj = DateAdjustment.None

        if (nanos < 0) {
            nanos += 1_000_000_000
            secs -= 1
        } else if (nanos >= 1_000_000_000) {
            nanos -= 1_000_000_000
            secs += 1
        }

        if (secs < 0) {
            secs += 60
            mins -= 1
        } else if (secs >= 60) {
            secs -= 60
            mins += 1
        }

        if (mins < 0) {
            mins += 60
            hrs -= 1
        } else if (mins >= 60) {
            mins -= 60
            hrs += 1
        }

        if (hrs >= 24) {
            hrs -= 24
            adj = DateAdjustment.Next
        } else if (hrs < 0) {
            hrs += 24
            adj = DateAdjustment.Previous
        }

        return Pair(adj, fromHmsNanosUnchecked(hrs, mins, secs, nanos))
    }

    /** Replace the clock hour. */
    fun replaceHour(hour: Int): Result<Time> {
        if (hour !in 0..23) return Result.failure(ComponentRange.unconditional("hour"))
        return Result.success(fromHmsNanosUnchecked(hour, minute, second, nanosecond))
    }

    /** Truncate the time to the hour. */
    fun truncateToHour(): Time = fromHmsNanosUnchecked(hour, 0, 0, 0)

    /** Replace the minute within the hour. */
    fun replaceMinute(minute: Int): Result<Time> {
        if (minute !in 0..59) return Result.failure(ComponentRange.unconditional("minute"))
        return Result.success(fromHmsNanosUnchecked(hour, minute, second, nanosecond))
    }

    /** Truncate the time to the minute. */
    fun truncateToMinute(): Time = fromHmsNanosUnchecked(hour, minute, 0, 0)

    /** Replace the second within the minute. */
    fun replaceSecond(second: Int): Result<Time> {
        if (second !in 0..59) return Result.failure(ComponentRange.unconditional("second"))
        return Result.success(fromHmsNanosUnchecked(hour, minute, second, nanosecond))
    }

    /** Truncate the time to the second. */
    fun truncateToSecond(): Time = fromHmsNanosUnchecked(hour, minute, second, 0)

    /** Replace the milliseconds within the second. */
    fun replaceMillisecond(millisecond: Int): Result<Time> {
        if (millisecond !in 0..999) return Result.failure(ComponentRange.unconditional("millisecond"))
        return Result.success(fromHmsNanosUnchecked(hour, minute, second, millisecond * 1_000_000))
    }

    /** Truncate the time to the millisecond. */
    fun truncateToMillisecond(): Time =
        fromHmsNanosUnchecked(hour, minute, second, nanosecond - (nanosecond % 1_000_000))

    /** Replace the microseconds within the second. */
    fun replaceMicrosecond(microsecond: Int): Result<Time> {
        if (microsecond !in 0..999_999) return Result.failure(ComponentRange.unconditional("microsecond"))
        return Result.success(fromHmsNanosUnchecked(hour, minute, second, microsecond * 1_000))
    }

    /** Truncate the time to the microsecond. */
    fun truncateToMicrosecond(): Time =
        fromHmsNanosUnchecked(hour, minute, second, nanosecond - (nanosecond % 1_000))

    /** Replace the nanoseconds within the second. */
    fun replaceNanosecond(nanosecond: Int): Result<Time> {
        if (nanosecond !in 0..999_999_999) return Result.failure(ComponentRange.unconditional("nanosecond"))
        return Result.success(fromHmsNanosUnchecked(hour, minute, second, nanosecond))
    }

    fun fmt(): String {
        val h = hour.toString().padStart(2, '0')
        val m = minute.toString().padStart(2, '0')
        val s = second.toString().padStart(2, '0')
        return if (nanosecond == 0) {
            "$h:$m:$s"
        } else {
            val nanoStr = nanosecond.toString().padStart(9, '0').trimEnd('0')
            "$h:$m:$s.$nanoStr"
        }
    }

    override fun toString(): String {
        val m = minute.toString().padStart(2, '0')
        val s = second.toString().padStart(2, '0')
        val nanoStr =
            if (nanosecond == 0) {
                "0"
            } else {
                nanosecond.toString().padStart(9, '0').trimEnd('0')
            }
        return "$hour:$m:$s.$nanoStr"
    }

    override fun compareTo(other: Time): Int {
        var cmp = hour.compareTo(other.hour)
        if (cmp != 0) return cmp
        cmp = minute.compareTo(other.minute)
        if (cmp != 0) return cmp
        cmp = second.compareTo(other.second)
        if (cmp != 0) return cmp
        return nanosecond.compareTo(other.nanosecond)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Time) return false
        return hour == other.hour &&
            minute == other.minute &&
            second == other.second &&
            nanosecond == other.nanosecond
    }

    override fun hashCode(): Int {
        var result = hour
        result = 31 * result + minute
        result = 31 * result + second
        result = 31 * result + nanosecond
        return result
    }

    operator fun plus(duration: Duration): Time =
        adjustingAdd(duration).second

    operator fun plus(duration: kotlin.time.Duration): Time =
        duration.toComponents { seconds, nanoseconds ->
            adjustingAdd(Duration.new(seconds, nanoseconds)).second
        }

    operator fun minus(duration: Duration): Time =
        adjustingSub(duration).second

    operator fun minus(duration: kotlin.time.Duration): Time =
        duration.toComponents { seconds, nanoseconds ->
            adjustingSub(Duration.new(seconds, nanoseconds)).second
        }

    operator fun minus(other: Time): Duration {
        val hourDiff = this.hour - other.hour
        val minuteDiff = this.minute - other.minute
        val secondDiff = this.second - other.second
        val nanosecondDiff = this.nanosecond - other.nanosecond

        var seconds = hourDiff * 3600 + minuteDiff * 60 + secondDiff
        var nanoseconds = nanosecondDiff

        if (seconds > 0 && nanoseconds < 0) {
            seconds -= 1
            nanoseconds += 1_000_000_000
        } else if (seconds < 0 && nanoseconds > 0) {
            seconds += 1
            nanoseconds -= 1_000_000_000
        }

        return Duration.new(seconds.toLong(), nanoseconds)
    }

    /** Format the [Time] using the provided [Formattable] into the given [output] buffer. */
    fun formatInto(
        output: StringBuilder,
        description: Formattable,
    ): Result<Int> = description.formatInto(output, this)

    fun formatInto(
        output: StringBuilder,
        description: List<BorrowedFormatItem>,
    ): Result<Int> = BorrowedFormatItemSliceFormattable(description).formatInto(output, this)

    /** Format the [Time] using the provided [Formattable]. */
    fun format(description: Formattable): Result<String> = description.format(this)

    fun format(description: List<BorrowedFormatItem>): Result<String> =
        BorrowedFormatItemSliceFormattable(description).format(this)

    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
        if (serializer.isHumanReadable()) {
            val s =
                format(TIME_FORMAT).getOrElse {
                    return SerdeResult.failure(SerdeError.custom("failed formatting `Time`"))
                }
            return serializer.serializeStr(s)
        }
        return serdeCatching {
            val tuple = serializer.serializeTuple(4).getOrThrow()
            tuple.serializeElement(U8Serialize(hour().toUByte())).getOrThrow()
            tuple.serializeElement(U8Serialize(minute().toUByte())).getOrThrow()
            tuple.serializeElement(U8Serialize(second().toUByte())).getOrThrow()
            tuple.serializeElement(U32Serialize(nanosecond().toUInt())).getOrThrow()
            tuple.end().getOrThrow()
        }
    }

    companion object : Deserialize<Time> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Time> =
            if (deserializer.isHumanReadable()) {
                deserializer.deserializeAny(TimeVisitor)
            } else {
                deserializer.deserializeTuple(4, TimeVisitor)
            }

        val MIDNIGHT: Time = fromHmsNanosUnchecked(0, 0, 0, 0)
        val MAX: Time = fromHmsNanosUnchecked(23, 59, 59, 999_999_999)

        fun fromHmsNanosUnchecked(
            hour: Int,
            minute: Int,
            second: Int,
            nanosecond: Int,
        ): Time = Time(hour, minute, second, nanosecond, Padding.Optimize)

        fun fromHmsNanoUnchecked(
            hour: Int,
            minute: Int,
            second: Int,
            nanosecond: Int,
        ): Time = fromHmsNanosUnchecked(hour, minute, second, nanosecond)

        fun fromHmsUnchecked(
            hour: Int,
            minute: Int,
            second: Int,
        ): Time = fromHmsNanosUnchecked(hour, minute, second, 0)

        fun fromHms(hour: Int, minute: Int, second: Int): Result<Time> {
            if (hour !in 0..23) return Result.failure(ComponentRange.unconditional("hour"))
            if (minute !in 0..59) return Result.failure(ComponentRange.unconditional("minute"))
            if (second !in 0..59) return Result.failure(ComponentRange.unconditional("second"))
            return Result.success(fromHmsNanosUnchecked(hour, minute, second, 0))
        }

        fun fromHmsMilli(hour: Int, minute: Int, second: Int, millisecond: Int): Result<Time> {
            if (hour !in 0..23) return Result.failure(ComponentRange.unconditional("hour"))
            if (minute !in 0..59) return Result.failure(ComponentRange.unconditional("minute"))
            if (second !in 0..59) return Result.failure(ComponentRange.unconditional("second"))
            if (millisecond !in 0..999) return Result.failure(ComponentRange.unconditional("millisecond"))
            return Result.success(fromHmsNanosUnchecked(hour, minute, second, millisecond * 1_000_000))
        }

        fun fromHmsMicro(hour: Int, minute: Int, second: Int, microsecond: Int): Result<Time> {
            if (hour !in 0..23) return Result.failure(ComponentRange.unconditional("hour"))
            if (minute !in 0..59) return Result.failure(ComponentRange.unconditional("minute"))
            if (second !in 0..59) return Result.failure(ComponentRange.unconditional("second"))
            if (microsecond !in 0..999_999) return Result.failure(ComponentRange.unconditional("microsecond"))
            return Result.success(fromHmsNanosUnchecked(hour, minute, second, microsecond * 1_000))
        }

        fun fromHmsNano(hour: Int, minute: Int, second: Int, nanosecond: Int): Result<Time> {
            if (hour !in 0..23) return Result.failure(ComponentRange.unconditional("hour"))
            if (minute !in 0..59) return Result.failure(ComponentRange.unconditional("minute"))
            if (second !in 0..59) return Result.failure(ComponentRange.unconditional("second"))
            if (nanosecond !in 0..999_999_999) return Result.failure(ComponentRange.unconditional("nanosecond"))
            return Result.success(fromHmsNanosUnchecked(hour, minute, second, nanosecond))
        }

        /** Parse a [Time] from the input using the provided [Parsable] format description. */
        fun parse(input: String, description: Parsable): Result<Time> =
            description.parseTime(input.encodeToByteArray())

        fun parse(input: String, description: List<BorrowedFormatItem>): Result<Time> =
            BorrowedFormatItemSliceParsable(description).parseTime(input.encodeToByteArray())

        fun tryFromParsed(parsed: io.github.kotlinmania.time.parsing.Parsed): Result<Time> =
            parsed.toTime()
    }
}
