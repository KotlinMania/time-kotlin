// port-lint: source rand08.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import io.github.kotlinmania.rand.distr.StandardUniform

/**
 * Implementation of [Distribution] for various time types (rand 0.8 compatibility surface).
 */
object Rand08 {
    val timeDistribution: Distribution<Time> =
        Distribution { rng ->
            Time.fromHmsNanosUnchecked(
                rng.randomRange(0..23),
                rng.randomRange(0..59),
                rng.randomRange(0..59),
                rng.randomRange(0..999_999_999),
            )
        }

    val dateDistribution: Distribution<Date> =
        Distribution { rng ->
            Date.fromJulianDayUnchecked(
                rng.randomRange(Date.MIN.toJulianDay()..Date.MAX.toJulianDay()),
            )
        }

    val utcOffsetDistribution: Distribution<UtcOffset> =
        Distribution { rng ->
            UtcOffset.fromHmsRanged(
                rng.randomRange(-25..25),
                rng.randomRange(-59..59),
                rng.randomRange(-59..59),
            )
        }

    val primitiveDateTimeDistribution: Distribution<PrimitiveDateTime> =
        Distribution { rng ->
            PrimitiveDateTime.new(dateDistribution.sample(rng), timeDistribution.sample(rng))
        }

    val utcDateTimeDistribution: Distribution<UtcDateTime> =
        Distribution { rng ->
            UtcDateTime.new(dateDistribution.sample(rng), timeDistribution.sample(rng))
        }

    val offsetDateTimeDistribution: Distribution<OffsetDateTime> =
        Distribution { rng ->
            val dateTime = primitiveDateTimeDistribution.sample(rng)
            dateTime.assumeOffset(utcOffsetDistribution.sample(rng))
        }

    val durationDistribution: Distribution<Duration> =
        Distribution { rng ->
            Duration.newRanged(
                rng.nextU64().toLong(),
                rng.randomRange(-999_999_999..999_999_999),
            )
        }

    val weekdayDistribution: Distribution<Weekday> =
        Distribution { rng ->
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

    val monthDistribution: Distribution<Month> =
        Distribution { rng ->
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
}

fun StandardUniform.genTime(rng: Rng): Time = Rand08.timeDistribution.sample(rng)

fun StandardUniform.genDate(rng: Rng): Date = Rand08.dateDistribution.sample(rng)

fun StandardUniform.genUtcOffset(rng: Rng): UtcOffset = Rand08.utcOffsetDistribution.sample(rng)

fun StandardUniform.genPrimitiveDateTime(rng: Rng): PrimitiveDateTime =
    Rand08.primitiveDateTimeDistribution.sample(rng)

fun StandardUniform.genUtcDateTime(rng: Rng): UtcDateTime =
    Rand08.utcDateTimeDistribution.sample(rng)

fun StandardUniform.genOffsetDateTime(rng: Rng): OffsetDateTime =
    Rand08.offsetDateTimeDistribution.sample(rng)

fun StandardUniform.genDuration(rng: Rng): Duration = Rand08.durationDistribution.sample(rng)

fun StandardUniform.genWeekday(rng: Rng): Weekday = Rand08.weekdayDistribution.sample(rng)

fun StandardUniform.genMonth(rng: Rng): Month = Rand08.monthDistribution.sample(rng)
