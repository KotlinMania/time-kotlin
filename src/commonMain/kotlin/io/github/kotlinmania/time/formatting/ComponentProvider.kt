// port-lint: source formatting/component_provider.rs
package io.github.kotlinmania.time.formatting

import io.github.kotlinmania.time.Date
import io.github.kotlinmania.time.Month
import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.PrimitiveDateTime
import io.github.kotlinmania.time.Time
import io.github.kotlinmania.time.UtcDateTime
import io.github.kotlinmania.time.UtcOffset
import io.github.kotlinmania.time.Weekday
import io.github.kotlinmania.time.formatdescription.PeriodCase

/**
 * State used by date-providing types to cache computed values.
 *
 * This is used to avoid redundant computations when multiple date components are almost certainly
 * going to be requested within the same formatting invocation.
 */
internal class DateState {
    var day: UByte? = null
    var month: Month? = null
    var isoWeek: UByte? = null
    var isoYearIsInitialized: Boolean = false
    var isoYear: Int = 0
}

/**
 * A type with the ability to provide date, time, offset, and/or timestamp components on demand.
 */
internal interface ComponentProvider<V, State> {
    fun defaultState(): State

    val suppliesDate: Boolean get() = false
    val suppliesTime: Boolean get() = false
    val suppliesOffset: Boolean get() = false
    val suppliesTimestamp: Boolean get() = false

    fun day(value: V, state: State): UByte =
        error("type does not supply date components")

    fun month(value: V, state: State): Month =
        error("type does not supply date components")

    fun ordinal(value: V, state: State): UShort =
        error("type does not supply date components")

    fun weekday(value: V, state: State): Weekday =
        error("type does not supply date components")

    fun isoWeekNumber(value: V, state: State): UByte =
        error("type does not supply date components")

    fun mondayBasedWeek(value: V, state: State): UByte =
        error("type does not supply date components")

    fun sundayBasedWeek(value: V, state: State): UByte =
        error("type does not supply date components")

    fun calendarYear(value: V, state: State): Int =
        error("type does not supply date components")

    fun isoYear(value: V, state: State): Int =
        error("type does not supply date components")

    fun hour(value: V, state: State): UByte =
        error("type does not supply time components")

    fun minute(value: V, state: State): UByte =
        error("type does not supply time components")

    fun period(value: V, state: State): PeriodCase =
        error("type does not supply time components")

    fun second(value: V, state: State): UByte =
        error("type does not supply time components")

    fun nanosecond(value: V, state: State): UInt =
        error("type does not supply time components")

    fun offsetIsNegative(value: V, state: State): Boolean =
        error("type does not supply offset components")

    fun offsetIsUtc(value: V, state: State): Boolean =
        error("type does not supply offset components")

    fun offsetHour(value: V, state: State): Byte =
        error("type does not supply offset components")

    fun offsetMinute(value: V, state: State): Byte =
        error("type does not supply offset components")

    fun offsetSecond(value: V, state: State): Byte =
        error("type does not supply offset components")

    fun unixTimestampSeconds(value: V, state: State): Long =
        error("type does not supply timestamp components")

    fun unixTimestampMilliseconds(value: V, state: State): Long =
        error("type does not supply timestamp components")

    fun unixTimestampMicroseconds(value: V, state: State): Pair<Boolean, String> =
        error("type does not supply timestamp components")

    fun unixTimestampNanoseconds(value: V, state: State): Pair<Boolean, String> =
        error("type does not supply timestamp components")
}

internal object TimeComponentProvider : ComponentProvider<Time, Unit> {
    override fun defaultState() = Unit

    override val suppliesTime: Boolean = true

    override fun hour(value: Time, state: Unit): UByte = value.hour().toUByte()

    override fun minute(value: Time, state: Unit): UByte = value.minute().toUByte()

    override fun period(value: Time, state: Unit): PeriodCase =
        if (value.hour() < 12) PeriodCase.Am else PeriodCase.Pm

    override fun second(value: Time, state: Unit): UByte = value.second().toUByte()

    override fun nanosecond(value: Time, state: Unit): UInt = value.nanosecond().toUInt()
}

internal object DateComponentProvider : ComponentProvider<Date, DateState> {
    override fun defaultState(): DateState = DateState()

    override val suppliesDate: Boolean = true

    override fun day(value: Date, state: DateState): UByte {
        state.day?.let { return it }
        val (_, month, day) = value.toCalendarDate()
        state.month = month
        val d = day.toUByte()
        state.day = d
        return d
    }

    override fun month(value: Date, state: DateState): Month {
        state.month?.let { return it }
        val m = value.month()
        state.month = m
        return m
    }

    override fun ordinal(value: Date, state: DateState): UShort = value.ordinal().toUShort()

    override fun weekday(value: Date, state: DateState): Weekday = value.weekday()

    override fun isoWeekNumber(value: Date, state: DateState): UByte {
        state.isoWeek?.let { return it }
        val (isoYear, isoWeek) = value.isoYearWeek()
        state.isoYear = isoYear
        state.isoYearIsInitialized = true
        val w = isoWeek.toUByte()
        state.isoWeek = w
        return w
    }

    override fun mondayBasedWeek(value: Date, state: DateState): UByte =
        value.mondayBasedWeek().toUByte()

    override fun sundayBasedWeek(value: Date, state: DateState): UByte =
        value.sundayBasedWeek().toUByte()

    override fun calendarYear(value: Date, state: DateState): Int = value.year()

    override fun isoYear(value: Date, state: DateState): Int {
        if (state.isoYearIsInitialized) {
            return state.isoYear
        }
        val (isoYear, isoWeek) = value.isoYearWeek()
        state.isoYear = isoYear
        state.isoYearIsInitialized = true
        state.isoWeek = isoWeek.toUByte()
        return isoYear
    }
}

internal object PrimitiveDateTimeComponentProvider : ComponentProvider<PrimitiveDateTime, DateState> {
    override fun defaultState(): DateState = DateState()

    override val suppliesDate: Boolean = true
    override val suppliesTime: Boolean = true

    override fun day(value: PrimitiveDateTime, state: DateState): UByte =
        DateComponentProvider.day(value.date(), state)

    override fun month(value: PrimitiveDateTime, state: DateState): Month =
        DateComponentProvider.month(value.date(), state)

    override fun ordinal(value: PrimitiveDateTime, state: DateState): UShort =
        DateComponentProvider.ordinal(value.date(), state)

    override fun weekday(value: PrimitiveDateTime, state: DateState): Weekday =
        DateComponentProvider.weekday(value.date(), state)

    override fun isoWeekNumber(value: PrimitiveDateTime, state: DateState): UByte =
        DateComponentProvider.isoWeekNumber(value.date(), state)

    override fun mondayBasedWeek(value: PrimitiveDateTime, state: DateState): UByte =
        DateComponentProvider.mondayBasedWeek(value.date(), state)

    override fun sundayBasedWeek(value: PrimitiveDateTime, state: DateState): UByte =
        DateComponentProvider.sundayBasedWeek(value.date(), state)

    override fun calendarYear(value: PrimitiveDateTime, state: DateState): Int =
        DateComponentProvider.calendarYear(value.date(), state)

    override fun isoYear(value: PrimitiveDateTime, state: DateState): Int =
        DateComponentProvider.isoYear(value.date(), state)

    override fun hour(value: PrimitiveDateTime, state: DateState): UByte =
        TimeComponentProvider.hour(value.time(), Unit)

    override fun minute(value: PrimitiveDateTime, state: DateState): UByte =
        TimeComponentProvider.minute(value.time(), Unit)

    override fun period(value: PrimitiveDateTime, state: DateState): PeriodCase =
        TimeComponentProvider.period(value.time(), Unit)

    override fun second(value: PrimitiveDateTime, state: DateState): UByte =
        TimeComponentProvider.second(value.time(), Unit)

    override fun nanosecond(value: PrimitiveDateTime, state: DateState): UInt =
        TimeComponentProvider.nanosecond(value.time(), Unit)
}

internal object UtcOffsetComponentProvider : ComponentProvider<UtcOffset, Unit> {
    override fun defaultState() = Unit

    override val suppliesOffset: Boolean = true

    override fun offsetIsNegative(value: UtcOffset, state: Unit): Boolean = value.isNegative()

    override fun offsetIsUtc(value: UtcOffset, state: Unit): Boolean = value.isUtc()

    override fun offsetHour(value: UtcOffset, state: Unit): Byte = value.wholeHours().toByte()

    override fun offsetMinute(value: UtcOffset, state: Unit): Byte = value.minutesPastHour().toByte()

    override fun offsetSecond(value: UtcOffset, state: Unit): Byte = value.secondsPastMinute().toByte()
}


internal fun computeTimestampMicrosSignAndAbs(sec: Long, nano: Int): Pair<Boolean, String> =
    if (sec >= 0L) {
        val microRem = nano / 1_000
        val absStr =
            if (sec == 0L) {
                microRem.toString()
            } else {
                "$sec${microRem.toString().padStart(6, '0')}"
            }
        false to absStr
    } else if (nano == 0) {
        val absSec = -sec
        true to "${absSec}000000"
    } else {
        val absSec = (-sec) - 1L
        val absNano = 1_000_000_000 - nano
        val absMicro = absNano / 1_000
        if (absSec == 0L && absMicro == 0) {
            false to "0"
        } else if (absSec == 0L) {
            true to absMicro.toString()
        } else {
            true to "$absSec${absMicro.toString().padStart(6, '0')}"
        }
    }

internal fun computeTimestampNanosSignAndAbs(sec: Long, nano: Int): Pair<Boolean, String> =
    if (sec >= 0L) {
        val absStr =
            if (sec == 0L) {
                nano.toString()
            } else {
                "$sec${nano.toString().padStart(9, '0')}"
            }
        false to absStr
    } else if (nano == 0) {
        val absSec = -sec
        true to "${absSec}000000000"
    } else {
        val absSec = (-sec) - 1L
        val absNano = 1_000_000_000 - nano
        val absStr =
            if (absSec == 0L) {
                absNano.toString()
            } else {
                "$absSec${absNano.toString().padStart(9, '0')}"
            }
        true to absStr
    }

internal fun computeTimestampMillis(sec: Long, nano: Int): Long =
    if (sec >= 0L || nano == 0) {
        sec * 1_000L + (nano / 1_000_000)
    } else {
        val absSec = (-sec) - 1L
        val absNano = 1_000_000_000 - nano
        -(absSec * 1_000L + (absNano / 1_000_000))
    }

internal object UtcDateTimeComponentProvider : ComponentProvider<UtcDateTime, DateState> {
    override fun defaultState(): DateState = DateState()

    override val suppliesDate: Boolean = true
    override val suppliesTime: Boolean = true
    override val suppliesOffset: Boolean = true
    override val suppliesTimestamp: Boolean = true

    override fun day(value: UtcDateTime, state: DateState): UByte =
        DateComponentProvider.day(value.date(), state)

    override fun month(value: UtcDateTime, state: DateState): Month =
        DateComponentProvider.month(value.date(), state)

    override fun ordinal(value: UtcDateTime, state: DateState): UShort =
        DateComponentProvider.ordinal(value.date(), state)

    override fun weekday(value: UtcDateTime, state: DateState): Weekday =
        DateComponentProvider.weekday(value.date(), state)

    override fun isoWeekNumber(value: UtcDateTime, state: DateState): UByte =
        DateComponentProvider.isoWeekNumber(value.date(), state)

    override fun mondayBasedWeek(value: UtcDateTime, state: DateState): UByte =
        DateComponentProvider.mondayBasedWeek(value.date(), state)

    override fun sundayBasedWeek(value: UtcDateTime, state: DateState): UByte =
        DateComponentProvider.sundayBasedWeek(value.date(), state)

    override fun calendarYear(value: UtcDateTime, state: DateState): Int =
        DateComponentProvider.calendarYear(value.date(), state)

    override fun isoYear(value: UtcDateTime, state: DateState): Int =
        DateComponentProvider.isoYear(value.date(), state)

    override fun hour(value: UtcDateTime, state: DateState): UByte =
        TimeComponentProvider.hour(value.time(), Unit)

    override fun minute(value: UtcDateTime, state: DateState): UByte =
        TimeComponentProvider.minute(value.time(), Unit)

    override fun period(value: UtcDateTime, state: DateState): PeriodCase =
        TimeComponentProvider.period(value.time(), Unit)

    override fun second(value: UtcDateTime, state: DateState): UByte =
        TimeComponentProvider.second(value.time(), Unit)

    override fun nanosecond(value: UtcDateTime, state: DateState): UInt =
        TimeComponentProvider.nanosecond(value.time(), Unit)

    override fun offsetIsNegative(value: UtcDateTime, state: DateState): Boolean = false

    override fun offsetIsUtc(value: UtcDateTime, state: DateState): Boolean = true

    override fun offsetHour(value: UtcDateTime, state: DateState): Byte = 0

    override fun offsetMinute(value: UtcDateTime, state: DateState): Byte = 0

    override fun offsetSecond(value: UtcDateTime, state: DateState): Byte = 0

    override fun unixTimestampSeconds(value: UtcDateTime, state: DateState): Long =
        value.unixTimestamp()

    override fun unixTimestampMilliseconds(value: UtcDateTime, state: DateState): Long =
        computeTimestampMillis(value.unixTimestamp(), value.nanosecond())

    override fun unixTimestampMicroseconds(value: UtcDateTime, state: DateState): Pair<Boolean, String> =
        computeTimestampMicrosSignAndAbs(value.unixTimestamp(), value.nanosecond())

    override fun unixTimestampNanoseconds(value: UtcDateTime, state: DateState): Pair<Boolean, String> =
        computeTimestampNanosSignAndAbs(value.unixTimestamp(), value.nanosecond())
}

internal object OffsetDateTimeComponentProvider : ComponentProvider<OffsetDateTime, DateState> {
    override fun defaultState(): DateState = DateState()

    override val suppliesDate: Boolean = true
    override val suppliesTime: Boolean = true
    override val suppliesOffset: Boolean = true
    override val suppliesTimestamp: Boolean = true

    override fun day(value: OffsetDateTime, state: DateState): UByte =
        DateComponentProvider.day(value.date(), state)

    override fun month(value: OffsetDateTime, state: DateState): Month =
        DateComponentProvider.month(value.date(), state)

    override fun ordinal(value: OffsetDateTime, state: DateState): UShort =
        DateComponentProvider.ordinal(value.date(), state)

    override fun weekday(value: OffsetDateTime, state: DateState): Weekday =
        DateComponentProvider.weekday(value.date(), state)

    override fun isoWeekNumber(value: OffsetDateTime, state: DateState): UByte =
        DateComponentProvider.isoWeekNumber(value.date(), state)

    override fun mondayBasedWeek(value: OffsetDateTime, state: DateState): UByte =
        DateComponentProvider.mondayBasedWeek(value.date(), state)

    override fun sundayBasedWeek(value: OffsetDateTime, state: DateState): UByte =
        DateComponentProvider.sundayBasedWeek(value.date(), state)

    override fun calendarYear(value: OffsetDateTime, state: DateState): Int =
        DateComponentProvider.calendarYear(value.date(), state)

    override fun isoYear(value: OffsetDateTime, state: DateState): Int =
        DateComponentProvider.isoYear(value.date(), state)

    override fun hour(value: OffsetDateTime, state: DateState): UByte =
        TimeComponentProvider.hour(value.time(), Unit)

    override fun minute(value: OffsetDateTime, state: DateState): UByte =
        TimeComponentProvider.minute(value.time(), Unit)

    override fun period(value: OffsetDateTime, state: DateState): PeriodCase =
        TimeComponentProvider.period(value.time(), Unit)

    override fun second(value: OffsetDateTime, state: DateState): UByte =
        TimeComponentProvider.second(value.time(), Unit)

    override fun nanosecond(value: OffsetDateTime, state: DateState): UInt =
        TimeComponentProvider.nanosecond(value.time(), Unit)

    override fun offsetIsNegative(value: OffsetDateTime, state: DateState): Boolean =
        UtcOffsetComponentProvider.offsetIsNegative(value.offset(), Unit)

    override fun offsetIsUtc(value: OffsetDateTime, state: DateState): Boolean =
        UtcOffsetComponentProvider.offsetIsUtc(value.offset(), Unit)

    override fun offsetHour(value: OffsetDateTime, state: DateState): Byte =
        UtcOffsetComponentProvider.offsetHour(value.offset(), Unit)

    override fun offsetMinute(value: OffsetDateTime, state: DateState): Byte =
        UtcOffsetComponentProvider.offsetMinute(value.offset(), Unit)

    override fun offsetSecond(value: OffsetDateTime, state: DateState): Byte =
        UtcOffsetComponentProvider.offsetSecond(value.offset(), Unit)

    override fun unixTimestampSeconds(value: OffsetDateTime, state: DateState): Long =
        value.unixTimestamp()

    override fun unixTimestampMilliseconds(value: OffsetDateTime, state: DateState): Long =
        computeTimestampMillis(value.unixTimestamp(), value.nanosecond())

    override fun unixTimestampMicroseconds(value: OffsetDateTime, state: DateState): Pair<Boolean, String> =
        computeTimestampMicrosSignAndAbs(value.unixTimestamp(), value.nanosecond())

    override fun unixTimestampNanoseconds(value: OffsetDateTime, state: DateState): Pair<Boolean, String> =
        computeTimestampNanosSignAndAbs(value.unixTimestamp(), value.nanosecond())
}
