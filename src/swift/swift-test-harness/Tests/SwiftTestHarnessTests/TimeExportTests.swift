import Testing
import Time

@Suite struct TimeExportTests {
    @Test func durationPreservesSecondsAndSign() {
        let positive = Duration.Companion.shared.seconds(seconds: 90)
        #expect(positive.wholeSeconds() == 90)
        #expect(positive.wholeMinutes() == 1)
        #expect(positive.isPositive())
        let negative = Duration.Companion.shared.seconds(seconds: -90)
        #expect(negative.wholeSeconds() == -90)
        #expect(negative.wholeMinutes() == -1)
        #expect(!negative.isPositive())
    }

    @Test func utcOffsetIsZero() {
        let utc = UtcOffset.Companion.shared.UTC
        #expect(utc.wholeSeconds() == 0)
        #expect(utc.wholeHours() == 0)
    }
}
