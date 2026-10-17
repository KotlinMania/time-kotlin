# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 25/90 (27.8%)
- **Function parity:** 374/1026 matched (target 524) — 36.5%
- **Class/type parity:** 52/155 matched (target 89) — 33.5%
- **Combined symbol parity:** 426/1181 matched (target 613) — 36.1%
- **Average inline-code cosine:** 0.19 (function body across 25 matched files)
- **Average documentation cosine:** 0.75 (doc text across 25 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 24 files with <0.60 function similarity
- **Needs Review:** 0 files with 0.60-0.84 function similarity
- **Excellent:** 1 files with >=0.85 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. offset_date_time
- **Similarity:** 0.09 (needs 76% improvement)
- **Dependencies:** 11
- **Priority Score:** 11429009.0
- **Functions:** 47/86 matched (target 59)
- **Missing functions:** `eq`, `partial_cmp`, `cmp`, `hash`, `raw_to_bits`, `now_utc`, `now_local`, `checked_to_utc`, `to_utc_raw`, `to_offset_raw`, `date_time`, `to_hms`, `to_hms_milli`, `to_hms_micro`, `to_hms_nano`, `replace_date_time`, `truncate_to_day`, `truncate_to_hour`, `truncate_to_minute`, `truncate_to_second`, `truncate_to_millisecond`, `truncate_to_microsecond`, `format_into`, `format`, `parse`, `is_valid_leap_second_stand_in`, `metadata`, `fmt_with_metadata`, `fmt`, `fmt`, `add`, `add`, `add_assign`, `add_assign`, `sub`, `sub`, `sub_assign`, `sub_assign`, `sub`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `Timestamp`, `Metadata`, `Output`
- **Symbol Deficit:** 42 (functions: 39, types: 3)
- **Action:** Deep review - likely missing major functionality

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. offset_date_time

- **Target:** `time.OffsetDateTime`
- **Similarity:** 0.09
- **Dependents:** 11
- **Priority Score:** 11429009.0
- **Functions:** 47/86 matched (target 59)
- **Missing functions:** `eq`, `partial_cmp`, `cmp`, `hash`, `raw_to_bits`, `now_utc`, `now_local`, `checked_to_utc`, `to_utc_raw`, `to_offset_raw`, `date_time`, `to_hms`, `to_hms_milli`, `to_hms_micro`, `to_hms_nano`, `replace_date_time`, `truncate_to_day`, `truncate_to_hour`, `truncate_to_minute`, `truncate_to_second`, `truncate_to_millisecond`, `truncate_to_microsecond`, `format_into`, `format`, `parse`, `is_valid_leap_second_stand_in`, `metadata`, `fmt_with_metadata`, `fmt`, `fmt`, `add`, `add`, `add_assign`, `add_assign`, `sub`, `sub`, `sub_assign`, `sub_assign`, `sub`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `Timestamp`, `Metadata`, `Output`

### 2. error.component_range

- **Target:** `error.ComponentRange`
- **Similarity:** 0.18
- **Dependents:** 7
- **Priority Score:** 7021108.0
- **Functions:** 8/9 matched (target 10)
- **Missing functions:** `fmt`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 3. duration

- **Target:** `time.Duration`
- **Similarity:** 0.10
- **Dependents:** 5
- **Priority Score:** 5389609.0
- **Functions:** 55/90 matched (target 69)
- **Missing functions:** `fmt`, `try_from`, `try_from`, `add`, `add`, `add`, `add_assign`, `add_assign`, `add_assign`, `neg`, `sub`, `sub`, `sub`, `sub_assign`, `sub_assign`, `sub_assign`, `mul`, `mul`, `mul`, `mul`, `mul_assign`, `mul_assign`, `div_assign`, `div_assign`, `div`, `eq`, `eq`, `partial_cmp`, `partial_cmp`, `sum`, `sum`, `add`, `add_assign`, `sub`, `sub_assign`
- **Types:** 3/6 matched (target 3)
- **Missing types:** `Nanoseconds`, `Error`, `Output`

### 4. ext.digit_count

- **Target:** `ext.DigitCount [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 4
- **Priority Score:** 4000110.0
- **Functions:** 0/0 matched (target 4)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 5. date

- **Target:** `time.Date`
- **Similarity:** 0.21
- **Dependents:** 3
- **Priority Score:** 3237008.0
- **Functions:** 46/64 matched (target 53)
- **Missing functions:** `checked_add_std`, `checked_sub_std`, `format_into`, `format`, `parse`, `metadata`, `fmt_with_metadata`, `fmt`, `fmt`, `add`, `add`, `add_assign`, `add_assign`, `sub`, `sub`, `sub_assign`, `sub_assign`, `sub`
- **Types:** 1/6 matched (target 1)
- **Missing types:** `Year`, `JulianDay`, `DateMetadata`, `Metadata`, `Output`

### 6. time

- **Target:** `time.Time`
- **Similarity:** 0.05
- **Dependents:** 2
- **Priority Score:** 2316409.5
- **Functions:** 32/55 matched (target 41)
- **Missing functions:** `hash`, `eq`, `partial_cmp`, `cmp`, `from_hms_nanos_ranged`, `as_hms_nano_ranged`, `adjusting_add_std`, `adjusting_sub_std`, `format_into`, `format`, `parse`, `metadata`, `fmt_with_metadata`, `fmt`, `add`, `add_assign`, `add`, `add_assign`, `sub`, `sub_assign`, `sub`, `sub_assign`, `sub`
- **Types:** 1/9 matched (target 1)
- **Missing types:** `Padding`, `Hours`, `Minutes`, `Seconds`, `Nanoseconds`, `TimeMetadata`, `Metadata`, `Output`

### 7. instant

- **Target:** `time.Instant`
- **Similarity:** 0.02
- **Dependents:** 2
- **Priority Score:** 2253009.8
- **Functions:** 4/28 matched (target 11)
- **Missing functions:** `into_inner`, `from`, `from`, `sub`, `sub`, `sub`, `add`, `add`, `add`, `add_assign`, `add_assign`, `add_assign`, `sub`, `sub`, `sub`, `sub_assign`, `sub_assign`, `sub_assign`, `eq`, `eq`, `partial_cmp`, `partial_cmp`, `as_ref`, `borrow`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`

### 8. month

- **Target:** `time.Month`
- **Similarity:** 0.33
- **Dependents:** 2
- **Priority Score:** 2061706.6
- **Functions:** 10/12 matched
- **Missing functions:** `metadata`, `fmt`
- **Types:** 1/5 matched (target 1)
- **Missing types:** `MonthMetadata`, `Metadata`, `Err`, `Error`

### 9. error.format

- **Target:** `error.Format`
- **Similarity:** 0.09
- **Dependents:** 2
- **Priority Score:** 2031109.0
- **Functions:** 7/9 matched (target 13)
- **Missing functions:** `try_from`, `into_invalid_serde_value`
- **Types:** 1/2 matched (target 5)
- **Missing types:** `Error`

### 10. error.try_from_parsed

- **Target:** `error.TryFromParsed`
- **Similarity:** 0.12
- **Dependents:** 2
- **Priority Score:** 2010808.8
- **Functions:** 6/6 matched (target 8)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 3)
- **Missing types:** `Error`

### 11. error.invalid_format_description

- **Target:** `error.InvalidFormatDescription`
- **Similarity:** 0.16
- **Dependents:** 2
- **Priority Score:** 2010508.4
- **Functions:** 3/3 matched (target 10)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 8)
- **Missing types:** `Error`

### 12. primitive_date_time

- **Target:** `time.PrimitiveDateTime`
- **Similarity:** 0.15
- **Dependents:** 1
- **Priority Score:** 1317408.5
- **Functions:** 42/71 matched (target 50)
- **Missing functions:** `hash`, `eq`, `partial_cmp`, `cmp`, `as_i128`, `to_julian_day`, `as_utc`, `truncate_to_day`, `truncate_to_hour`, `truncate_to_minute`, `truncate_to_second`, `truncate_to_millisecond`, `truncate_to_microsecond`, `format_into`, `format`, `parse`, `metadata`, `fmt_with_metadata`, `fmt`, `fmt`, `add`, `add`, `add_assign`, `add_assign`, `sub`, `sub`, `sub_assign`, `sub_assign`, `sub`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Metadata`, `Output`

### 13. utc_offset

- **Target:** `time.UtcOffset`
- **Similarity:** 0.10
- **Dependents:** 1
- **Priority Score:** 1124009.0
- **Functions:** 27/32 matched (target 34)
- **Missing functions:** `local_offset_at`, `format_into`, `metadata`, `fmt_with_metadata`, `fmt`
- **Types:** 1/8 matched (target 1)
- **Missing types:** `Hours`, `Minutes`, `Seconds`, `WholeSeconds`, `UtcOffsetMetadata`, `Metadata`, `Output`

### 14. weekday

- **Target:** `time.Weekday`
- **Similarity:** 0.34
- **Dependents:** 1
- **Priority Score:** 1051606.6
- **Functions:** 10/12 matched
- **Missing functions:** `metadata`, `fmt`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `WeekdayMetadata`, `Metadata`, `Err`

### 15. error.parse

- **Target:** `error.Parse`
- **Similarity:** 0.05
- **Dependents:** 1
- **Priority Score:** 1031009.4
- **Functions:** 6/8 matched (target 11)
- **Missing functions:** `try_from`, `try_from`
- **Types:** 1/2 matched (target 3)
- **Missing types:** `Error`

### 16. ext.numerical_duration

- **Target:** `ext.NumericalDuration`
- **Similarity:** 0.23
- **Dependents:** 1
- **Priority Score:** 1021807.7
- **Functions:** 16/16 matched (target 25)
- **Missing functions:** _none_
- **Types:** 0/2 matched (target 0)
- **Missing types:** `Sealed`, `NumericalDuration`

### 17. error.different_variant

- **Target:** `error.DifferentVariant`
- **Similarity:** 0.14
- **Dependents:** 1
- **Priority Score:** 1010508.6
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 18. error.invalid_variant

- **Target:** `error.InvalidVariant`
- **Similarity:** 0.16
- **Dependents:** 1
- **Priority Score:** 1010508.4
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 19. error.conversion_range

- **Target:** `error.ConversionRange`
- **Similarity:** 0.16
- **Dependents:** 1
- **Priority Score:** 1010508.4
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 20. error.indeterminate_offset

- **Target:** `error.IndeterminateOffset`
- **Similarity:** 0.16
- **Dependents:** 1
- **Priority Score:** 1010508.4
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 21. error.parse_from_description

- **Target:** `error.ParseFromDescription`
- **Similarity:** 0.15
- **Dependents:** 1
- **Priority Score:** 1010508.4
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 4)
- **Missing types:** `Error`

### 22. format_description.modifier

- **Target:** `formatdescription.Modifier`
- **Similarity:** 0.23
- **Dependents:** 1
- **Priority Score:** 1005707.8
- **Functions:** 31/31 matched (target 56)
- **Missing functions:** _none_
- **Types:** 26/26 matched
- **Missing types:** _none_

### 23. util

- **Target:** `time.Util`
- **Similarity:** 0.35
- **Dependents:** 0
- **Priority Score:** 806.5
- **Functions:** 6/6 matched (target 18)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 4)
- **Missing types:** _none_

### 24. hint

- **Target:** `time.Hint`
- **Similarity:** 0.18
- **Dependents:** 0
- **Priority Score:** 308.2
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 25. format_description.component

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

