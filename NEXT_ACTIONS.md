# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 9/90 (10.0%)
- **Function parity:** 35/1023 matched (target 59) — 3.4%
- **Class/type parity:** 8/155 matched (target 10) — 5.2%
- **Combined symbol parity:** 43/1178 matched (target 69) — 3.7%
- **Average inline-code cosine:** 0.32 (function body across 9 matched files)
- **Average documentation cosine:** 0.81 (doc text across 9 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 7 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

1. **offset_date_time** (11 deps)
   - Path: `offset_date_time.rs`
   - Essential for 11 other files

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. error.component_range

- **Target:** `error.ComponentRange`
- **Similarity:** 0.25
- **Dependents:** 7
- **Priority Score:** 7041007.5
- **Functions:** 5/8 matched (target 7)
- **Missing functions:** `from`, `try_from`, `into_de_error`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 2. ext.digit_count

- **Target:** `ext.DigitCount [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 4
- **Priority Score:** 4010110.0
- **Functions:** 0/0 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 0)
- **Missing types:** `DigitCount`

### 3. month

- **Target:** `time.Month`
- **Similarity:** 0.58
- **Dependents:** 2
- **Priority Score:** 2051604.1
- **Functions:** 10/11 matched (target 12)
- **Missing functions:** `metadata`
- **Types:** 1/5 matched (target 1)
- **Missing types:** `MonthMetadata`, `Metadata`, `Err`, `Error`

### 4. weekday

- **Target:** `time.Weekday`
- **Similarity:** 0.59
- **Dependents:** 1
- **Priority Score:** 1041504.1
- **Functions:** 10/11 matched (target 12)
- **Missing functions:** `metadata`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `WeekdayMetadata`, `Metadata`, `Err`

### 5. error.invalid_variant

- **Target:** `error.InvalidVariant`
- **Similarity:** 0.06
- **Dependents:** 1
- **Priority Score:** 1030509.4
- **Functions:** 1/3 matched
- **Missing functions:** `from`, `try_from`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 6. error.conversion_range

- **Target:** `error.ConversionRange`
- **Similarity:** 0.06
- **Dependents:** 1
- **Priority Score:** 1030509.4
- **Functions:** 1/3 matched
- **Missing functions:** `from`, `try_from`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 7. error.different_variant

- **Target:** `error.DifferentVariant`
- **Similarity:** 0.06
- **Dependents:** 1
- **Priority Score:** 1030509.4
- **Functions:** 1/3 matched
- **Missing functions:** `from`, `try_from`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 8. util

- **Target:** `time.Util`
- **Similarity:** 0.62
- **Dependents:** 0
- **Priority Score:** 20803.8
- **Functions:** 4/6 matched (target 13)
- **Missing functions:** `refresh_tz_unchecked`, `refresh_tz`
- **Types:** 2/2 matched (target 4)
- **Missing types:** _none_

### 9. hint

- **Target:** `time.Hint`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 303.4
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Next Commands

```bash
# Initialize task queue for systematic porting
cd tools/ast_distance
./ast_distance --init-tasks ../../tmp/time/src rust ../../src/commonMain/kotlin/io/github/kotlinmania/time kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `error.mod` | `error.Mod` | 0 | `error/mod.rs` | `error/Mod.kt` |
| `ext.mod` | `ext.Mod` | 0 | `ext/mod.rs` | `ext/Mod.kt` |
| `format_description.mod` | `formatdescription.Mod` | 0 | `format_description/mod.rs` | `formatdescription/Mod.kt` |
| `parse.mod` | `formatdescription.parse.Mod` | 0 | `format_description/parse/mod.rs` | `formatdescription/parse/Mod.kt` |
| `formatting.mod` | `formatting.Mod` | 0 | `formatting/mod.rs` | `formatting/Mod.kt` |
| `interop.mod` | `interop.Mod` | 0 | `interop/mod.rs` | `interop/Mod.kt` |
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |
| `combinator.mod` | `parsing.combinator.Mod` | 0 | `parsing/combinator/mod.rs` | `parsing/combinator/Mod.kt` |
| `rfc.mod` | `parsing.combinator.rfc.Mod` | 0 | `parsing/combinator/rfc/mod.rs` | `parsing/combinator/rfc/Mod.kt` |
| `parsing.mod` | `parsing.Mod` | 0 | `parsing/mod.rs` | `parsing/Mod.kt` |
| `serde.mod` | `serde.Mod` | 0 | `serde/mod.rs` | `serde/Mod.kt` |
| `timestamp.mod` | `serde.timestamp.Mod` | 0 | `serde/timestamp/mod.rs` | `serde/timestamp/Mod.kt` |
| `local_offset_at.mod` | `sys.localoffsetat.Mod` | 0 | `sys/local_offset_at/mod.rs` | `sys/localoffsetat/Mod.kt` |
| `sys.mod` | `sys.Mod` | 0 | `sys/mod.rs` | `sys/Mod.kt` |
| `refresh_tz.mod` | `sys.refreshtz.Mod` | 0 | `sys/refresh_tz/mod.rs` | `sys/refreshtz/Mod.kt` |
