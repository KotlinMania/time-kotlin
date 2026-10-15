# port-lint Proposed Changes

**Generated:** 2026-10-13
**Source:** tmp/time/src
**Target:** src/commonMain/kotlin/io/github/kotlinmania/time

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `src/commonTest/kotlin/io/github/kotlinmania/time/MonthTest.kt` | `// port-lint: tests tests/integration/month.rs` | `// port-lint: tests month.rs` | `month.rs` | `port-lint provenance header matched only by basename: 'tests:tests/integration/month.rs' vs expected 'month.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/time/UtcOffsetTest.kt` | `// port-lint: tests tests/integration/utc_offset.rs` | `// port-lint: tests utc_offset.rs` | `utc_offset.rs` | `port-lint provenance header matched only by basename: 'tests:tests/integration/utc_offset.rs' vs expected 'utc_offset.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/time/WeekdayTest.kt` | `// port-lint: tests tests/integration/weekday.rs` | `// port-lint: tests weekday.rs` | `weekday.rs` | `port-lint provenance header matched only by basename: 'tests:tests/integration/weekday.rs' vs expected 'weekday.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/time/UtilTest.kt` | `// port-lint: tests tests/integration/util.rs` | `// port-lint: tests util.rs` | `util.rs` | `port-lint provenance header matched only by basename: 'tests:tests/integration/util.rs' vs expected 'util.rs'` |
