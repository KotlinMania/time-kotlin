# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 19/90 (21.1%)
- **Function parity:** 134/1028 matched (target 215) — 13.0%
- **Class/type parity:** 45/156 matched (target 92) — 28.8%
- **Combined symbol parity:** 179/1184 matched (target 307) — 15.1%
- **Average inline-code cosine:** 0.22 (function body across 18 matched files)
- **Average documentation cosine:** 0.88 (doc text across 18 matched files)
- **Cheat-zeroed Files:** 2
- **Critical Issues:** 18 files with <0.60 function similarity
- **Needs Review:** 0 files with 0.60-0.84 function similarity
- **Excellent:** 1 files with >=0.85 function similarity

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
- **Similarity:** 0.18
- **Dependents:** 7
- **Priority Score:** 7021108.0
- **Functions:** 8/9 matched (target 10)
- **Missing functions:** `fmt`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 2. ext.digit_count

- **Target:** `ext.DigitCount [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 4
- **Priority Score:** 4000110.0
- **Functions:** 0/0 matched (target 4)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 3. month

- **Target:** `time.Month`
- **Similarity:** 0.33
- **Dependents:** 2
- **Priority Score:** 2061706.6
- **Functions:** 10/12 matched
- **Missing functions:** `metadata`, `fmt`
- **Types:** 1/5 matched (target 1)
- **Missing types:** `MonthMetadata`, `Metadata`, `Err`, `Error`

### 4. error.format

- **Target:** `error.Format`
- **Similarity:** 0.10
- **Dependents:** 2
- **Priority Score:** 2031109.0
- **Functions:** 7/9 matched (target 13)
- **Missing functions:** `try_from`, `into_invalid_serde_value`
- **Types:** 1/2 matched (target 5)
- **Missing types:** `Error`

### 5. error.try_from_parsed

- **Target:** `error.TryFromParsed`
- **Similarity:** 0.13
- **Dependents:** 2
- **Priority Score:** 2010808.8
- **Functions:** 6/6 matched (target 8)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 3)
- **Missing types:** `Error`

### 6. error.invalid_format_description

- **Target:** `error.InvalidFormatDescription`
- **Similarity:** 0.17
- **Dependents:** 2
- **Priority Score:** 2010508.4
- **Functions:** 3/3 matched (target 10)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 8)
- **Missing types:** `Error`

### 7. utc_offset

- **Target:** `time.UtcOffset`
- **Similarity:** 0.10
- **Dependents:** 1
- **Priority Score:** 1124009.0
- **Functions:** 27/32 matched (target 34)
- **Missing functions:** `local_offset_at`, `format_into`, `metadata`, `fmt_with_metadata`, `fmt`
- **Types:** 1/8 matched (target 1)
- **Missing types:** `Hours`, `Minutes`, `Seconds`, `WholeSeconds`, `UtcOffsetMetadata`, `Metadata`, `Output`

### 8. weekday

- **Target:** `time.Weekday`
- **Similarity:** 0.34
- **Dependents:** 1
- **Priority Score:** 1051606.6
- **Functions:** 10/12 matched
- **Missing functions:** `metadata`, `fmt`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `WeekdayMetadata`, `Metadata`, `Err`

### 9. error.parse

- **Target:** `error.Parse`
- **Similarity:** 0.06
- **Dependents:** 1
- **Priority Score:** 1031009.4
- **Functions:** 6/8 matched (target 11)
- **Missing functions:** `try_from`, `try_from`
- **Types:** 1/2 matched (target 3)
- **Missing types:** `Error`

### 10. error.different_variant

- **Target:** `error.DifferentVariant`
- **Similarity:** 0.15
- **Dependents:** 1
- **Priority Score:** 1010508.5
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 11. error.invalid_variant

- **Target:** `error.InvalidVariant`
- **Similarity:** 0.17
- **Dependents:** 1
- **Priority Score:** 1010508.3
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 12. error.conversion_range

- **Target:** `error.ConversionRange`
- **Similarity:** 0.17
- **Dependents:** 1
- **Priority Score:** 1010508.3
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 13. error.indeterminate_offset

- **Target:** `error.IndeterminateOffset`
- **Similarity:** 0.17
- **Dependents:** 1
- **Priority Score:** 1010508.3
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 14. error.parse_from_description

- **Target:** `error.ParseFromDescription`
- **Similarity:** 0.17
- **Dependents:** 1
- **Priority Score:** 1010508.3
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 4)
- **Missing types:** `Error`

### 15. format_description.modifier

- **Target:** `formatdescription.Modifier`
- **Similarity:** 0.23
- **Dependents:** 1
- **Priority Score:** 1005707.8
- **Functions:** 31/31 matched (target 56)
- **Missing functions:** _none_
- **Types:** 26/26 matched
- **Missing types:** _none_

### 16. util

- **Target:** `time.Util`
- **Similarity:** 0.35
- **Dependents:** 0
- **Priority Score:** 806.5
- **Functions:** 6/6 matched (target 15)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 4)
- **Missing types:** _none_

### 17. error.mod

- **Target:** `time.Error [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 310.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 11)
- **Missing types:** _none_

### 18. hint

- **Target:** `time.Hint`
- **Similarity:** 0.18
- **Dependents:** 0
- **Priority Score:** 308.2
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 19. format_description.component

- **Target:** `formatdescription.Component`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 200.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 19)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
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

