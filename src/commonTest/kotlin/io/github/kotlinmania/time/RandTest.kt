// port-lint: tests tests/integration/rand.rs
package io.github.kotlinmania.time

import io.github.kotlinmania.rand.StepRng
import io.github.kotlinmania.rand.distr.StandardUniform
import kotlin.test.Test

class RandTest {
    @Test
    fun support08() {
        val rng = StepRng(0uL, 656_175_560uL)

        repeat(7) {
            StandardUniform.genWeekday(rng)
        }
        repeat(12) {
            StandardUniform.genMonth(rng)
        }
        StandardUniform.genTime(rng)
        StandardUniform.genDate(rng)
        StandardUniform.genUtcOffset(rng)
        StandardUniform.genPrimitiveDateTime(rng)
        StandardUniform.genUtcDateTime(rng)
        StandardUniform.genOffsetDateTime(rng)
        StandardUniform.genDuration(rng)
    }

    @Test
    fun support09() {
        val rng = StepRng(0uL, 656_175_560uL)

        repeat(7) {
            rng.sample(WeekdayDistribution)
        }
        repeat(12) {
            rng.sample(MonthDistribution)
        }
        rng.sample(TimeDistribution)
        rng.sample(DateDistribution)
        rng.sample(UtcOffsetDistribution)
        rng.sample(PrimitiveDateTimeDistribution)
        rng.sample(UtcDateTimeDistribution)
        rng.sample(OffsetDateTimeDistribution)
        rng.sample(DurationDistribution)
    }
}
