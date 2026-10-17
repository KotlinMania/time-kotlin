// port-lint: source rand08.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import io.github.kotlinmania.rand.distr.StandardUniform

/**
 * Implementation of [Distribution] for various time types (rand 0.8 compatibility surface).
 */
object Rand08 {
    object TimeDistribution : Distribution<Time> {
        override fun sample(rng: Rng): Time =
            Time.fromHmsNanosUnchecked(
                rng.randomRange(0..23),
                rng.randomRange(0..59),
                rng.randomRange(0..59),
                rng.randomRange(0..999_999_999),
            )
    }

    object DateDistribution : Distribution<Date> {
        override fun sample(rng: Rng): Date =
            Date.fromJulianDayUnchecked(
                rng.randomRange(Date.MIN.toJulianDay()..Date.MAX.toJulianDay()),
            )
    }

    object UtcOffsetDistribution : Distribution<UtcOffset> {
        override fun sample(rng: Rng): UtcOffset =
            UtcOffset.fromHmsRanged(
                rng.randomRange(-25..25),
                rng.randomRange(-59..59),
                rng.randomRange(-59..59),
            )
    }

    object PrimitiveDateTimeDistribution : Distribution<PrimitiveDateTime> {
        override fun sample(rng: Rng): PrimitiveDateTime =
            PrimitiveDateTime.new(DateDistribution.sample(rng), TimeDistribution.sample(rng))
    }

    object UtcDateTimeDistribution : Distribution<UtcDateTime> {
        override fun sample(rng: Rng): UtcDateTime =
            UtcDateTime.new(DateDistribution.sample(rng), TimeDistribution.sample(rng))
    }

    object OffsetDateTimeDistribution : Distribution<OffsetDateTime> {
        override fun sample(rng: Rng): OffsetDateTime {
            val dateTime = PrimitiveDateTimeDistribution.sample(rng)
            return dateTime.assumeOffset(UtcOffsetDistribution.sample(rng))
        }
    }

    object DurationDistribution : Distribution<Duration> {
        override fun sample(rng: Rng): Duration =
            Duration.newRanged(
                rng.nextU64().toLong(),
                rng.randomRange(-999_999_999..999_999_999),
            )
    }

    object WeekdayDistribution : Distribution<Weekday> {
        override fun sample(rng: Rng): Weekday =
            when (val v = rng.randomRange(0 until 7)) {
                0 -> Weekday.Monday
                1 -> Weekday.Tuesday
                2 -> Weekday.Wednesday
                3 -> Weekday.Thursday
                4 -> Weekday.Friday
                5 -> Weekday.Saturday
                else -> {
                    check(v == 6)
                    Weekday.Sunday
                }
            }
    }

    object MonthDistribution : Distribution<Month> {
        override fun sample(rng: Rng): Month =
            when (val v = rng.randomRange(1..12)) {
                1 -> Month.January
                2 -> Month.February
                3 -> Month.March
                4 -> Month.April
                5 -> Month.May
                6 -> Month.June
                7 -> Month.July
                8 -> Month.August
                9 -> Month.September
                10 -> Month.October
                11 -> Month.November
                else -> {
                    check(v == 12)
                    Month.December
                }
            }
    }

    val timeDistribution: Distribution<Time> = TimeDistribution
    val dateDistribution: Distribution<Date> = DateDistribution
    val utcOffsetDistribution: Distribution<UtcOffset> = UtcOffsetDistribution
    val primitiveDateTimeDistribution: Distribution<PrimitiveDateTime> = PrimitiveDateTimeDistribution
    val utcDateTimeDistribution: Distribution<UtcDateTime> = UtcDateTimeDistribution
    val offsetDateTimeDistribution: Distribution<OffsetDateTime> = OffsetDateTimeDistribution
    val durationDistribution: Distribution<Duration> = DurationDistribution
    val weekdayDistribution: Distribution<Weekday> = WeekdayDistribution
    val monthDistribution: Distribution<Month> = MonthDistribution
}

fun StandardUniform.genTime(rng: Rng): Time = Rand08.TimeDistribution.sample(rng)

fun StandardUniform.genDate(rng: Rng): Date = Rand08.DateDistribution.sample(rng)

fun StandardUniform.genUtcOffset(rng: Rng): UtcOffset = Rand08.UtcOffsetDistribution.sample(rng)

fun StandardUniform.genPrimitiveDateTime(rng: Rng): PrimitiveDateTime =
    Rand08.PrimitiveDateTimeDistribution.sample(rng)

fun StandardUniform.genUtcDateTime(rng: Rng): UtcDateTime =
    Rand08.UtcDateTimeDistribution.sample(rng)

fun StandardUniform.genOffsetDateTime(rng: Rng): OffsetDateTime =
    Rand08.OffsetDateTimeDistribution.sample(rng)

fun StandardUniform.genDuration(rng: Rng): Duration = Rand08.DurationDistribution.sample(rng)

fun StandardUniform.genWeekday(rng: Rng): Weekday = Rand08.WeekdayDistribution.sample(rng)

fun StandardUniform.genMonth(rng: Rng): Month = Rand08.MonthDistribution.sample(rng)
