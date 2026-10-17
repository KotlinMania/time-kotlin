# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 73/90 (81.1%)
- **Function parity:** 801/1026 matched (target 1441) — 78.1%
- **Class/type parity:** 78/155 matched (target 224) — 50.3%
- **Combined symbol parity:** 879/1181 matched (target 1665) — 74.4%
- **Average inline-code cosine:** 0.19 (function body across 73 matched files)
- **Average documentation cosine:** 0.65 (doc text across 73 matched files)
- **Cheat-zeroed Files:** 4
- **Critical Issues:** 70 files with <0.60 function similarity
- **Needs Review:** 0 files with 0.60-0.84 function similarity
- **Excellent:** 3 files with >=0.85 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. offset_date_time
- **Similarity:** 0.12 (needs 73% improvement)
- **Dependencies:** 11
- **Priority Score:** 11279009.0
- **Functions:** 62/86 matched (target 94)
- **Missing functions:** `eq`, `partial_cmp`, `cmp`, `hash`, `raw_to_bits`, `to_utc_raw`, `to_offset_raw`, `to_hms`, `to_hms_milli`, `to_hms_micro`, `to_hms_nano`, `metadata`, `fmt_with_metadata`, `fmt`, `fmt`, `add`, `add`, `add_assign`, `add_assign`, `sub`, `sub`, `sub_assign`, `sub_assign`, `sub`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `Timestamp`, `Metadata`, `Output`
- **Symbol Deficit:** 27 (functions: 24, types: 3)
- **Action:** Deep review - likely missing major functionality

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. offset_date_time

- **Target:** `time.OffsetDateTime`
- **Similarity:** 0.12
- **Dependents:** 11
- **Priority Score:** 11279009.0
- **Functions:** 62/86 matched (target 94)
- **Missing functions:** `eq`, `partial_cmp`, `cmp`, `hash`, `raw_to_bits`, `to_utc_raw`, `to_offset_raw`, `to_hms`, `to_hms_milli`, `to_hms_micro`, `to_hms_nano`, `metadata`, `fmt_with_metadata`, `fmt`, `fmt`, `add`, `add`, `add_assign`, `add_assign`, `sub`, `sub`, `sub_assign`, `sub_assign`, `sub`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `Timestamp`, `Metadata`, `Output`

### 2. formatting.formattable

- **Target:** `formatting.Formattable`
- **Similarity:** 0.02
- **Dependents:** 7
- **Priority Score:** 7091110.0
- **Functions:** 1/9 matched
- **Missing functions:** `format`, `format_into`, `format_into`, `format_into`, `format_into`, `format_into`, `format_into`, `format_into`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Sealed`

### 3. parsing.parsable

- **Target:** `parsing.Parsable`
- **Similarity:** 0.04
- **Dependents:** 7
- **Priority Score:** 7081909.5
- **Functions:** 10/17 matched (target 15)
- **Missing functions:** `parse_into`, `parse_into`, `parse_into`, `parse_offset_date_time`, `parse_into`, `parse_offset_date_time`, `parse_into`
- **Types:** 1/2 matched (target 3)
- **Missing types:** `Sealed`

### 4. error.component_range

- **Target:** `error.ComponentRange`
- **Similarity:** 0.18
- **Dependents:** 7
- **Priority Score:** 7021108.0
- **Functions:** 8/9 matched (target 13)
- **Missing functions:** `fmt`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 5. duration

- **Target:** `time.Duration`
- **Similarity:** 0.10
- **Dependents:** 5
- **Priority Score:** 5389609.0
- **Functions:** 55/90 matched (target 71)
- **Missing functions:** `fmt`, `try_from`, `try_from`, `add`, `add`, `add`, `add_assign`, `add_assign`, `add_assign`, `neg`, `sub`, `sub`, `sub`, `sub_assign`, `sub_assign`, `sub_assign`, `mul`, `mul`, `mul`, `mul`, `mul_assign`, `mul_assign`, `div_assign`, `div_assign`, `div`, `eq`, `eq`, `partial_cmp`, `partial_cmp`, `sum`, `sum`, `add`, `add_assign`, `sub`, `sub_assign`
- **Types:** 3/6 matched (target 3)
- **Missing types:** `Nanoseconds`, `Error`, `Output`

### 6. serde.iso8601

- **Target:** `serde.Iso8601`
- **Similarity:** 0.19
- **Dependents:** 5
- **Priority Score:** 5000408.0
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 7. serde.visitor

- **Target:** `serde.Visitor`
- **Similarity:** 0.09
- **Dependents:** 4
- **Priority Score:** 4022909.2
- **Functions:** 27/27 matched (target 55)
- **Missing functions:** _none_
- **Types:** 0/2 matched (target 15)
- **Missing types:** `Visitor`, `Value`

### 8. ext.digit_count

- **Target:** `ext.DigitCount [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 4
- **Priority Score:** 4000110.0
- **Functions:** 0/0 matched (target 4)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 9. date

- **Target:** `time.Date`
- **Similarity:** 0.22
- **Dependents:** 3
- **Priority Score:** 3207007.8
- **Functions:** 49/64 matched (target 69)
- **Missing functions:** `checked_add_std`, `checked_sub_std`, `metadata`, `fmt_with_metadata`, `fmt`, `fmt`, `add`, `add`, `add_assign`, `add_assign`, `sub`, `sub`, `sub_assign`, `sub_assign`, `sub`
- **Types:** 1/6 matched (target 1)
- **Missing types:** `Year`, `JulianDay`, `DateMetadata`, `Metadata`, `Output`

### 10. utc_date_time

- **Target:** `time.UtcDateTime`
- **Similarity:** 0.28
- **Dependents:** 3
- **Priority Score:** 3177807.2
- **Functions:** 60/74 matched (target 88)
- **Missing functions:** `to_offset_raw`, `metadata`, `fmt_with_metadata`, `fmt`, `fmt`, `add`, `add`, `add_assign`, `add_assign`, `sub`, `sub`, `sub_assign`, `sub_assign`, `sub`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `Timestamp`, `Metadata`, `Output`

### 11. format_description.owned_format_item

- **Target:** `formatdescription.OwnedFormatItem`
- **Similarity:** 0.06
- **Dependents:** 3
- **Priority Score:** 3101509.5
- **Functions:** 4/13 matched (target 20)
- **Missing functions:** `fmt`, `from`, `try_from`, `from`, `try_from`, `eq`, `eq`, `eq`, `eq`
- **Types:** 1/2 matched (target 6)
- **Missing types:** `Error`

### 12. ext.systemtime

- **Target:** `ext.SystemTimeExt`
- **Similarity:** 0.25
- **Dependents:** 3
- **Priority Score:** 3020507.5
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 0/2 matched (target 0)
- **Missing types:** `Sealed`, `SystemTimeExt`

### 13. time

- **Target:** `time.Time`
- **Similarity:** 0.06
- **Dependents:** 2
- **Priority Score:** 2286409.2
- **Functions:** 35/55 matched (target 57)
- **Missing functions:** `hash`, `eq`, `partial_cmp`, `cmp`, `from_hms_nanos_ranged`, `as_hms_nano_ranged`, `adjusting_add_std`, `adjusting_sub_std`, `metadata`, `fmt_with_metadata`, `fmt`, `add`, `add_assign`, `add`, `add_assign`, `sub`, `sub_assign`, `sub`, `sub_assign`, `sub`
- **Types:** 1/9 matched (target 1)
- **Missing types:** `Padding`, `Hours`, `Minutes`, `Seconds`, `Nanoseconds`, `TimeMetadata`, `Metadata`, `Output`

### 14. instant

- **Target:** `time.Instant`
- **Similarity:** 0.14
- **Dependents:** 2
- **Priority Score:** 2133008.5
- **Functions:** 16/28 matched (target 32)
- **Missing functions:** `from`, `add`, `add_assign`, `add_assign`, `add_assign`, `sub`, `sub`, `sub_assign`, `sub_assign`, `sub_assign`, `eq`, `partial_cmp`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`

### 15. month

- **Target:** `time.Month`
- **Similarity:** 0.33
- **Dependents:** 2
- **Priority Score:** 2061706.6
- **Functions:** 10/12 matched (target 14)
- **Missing functions:** `metadata`, `fmt`
- **Types:** 1/5 matched (target 1)
- **Missing types:** `MonthMetadata`, `Metadata`, `Err`, `Error`

### 16. formatting.component_provider

- **Target:** `formatting.ComponentProvider`
- **Similarity:** 0.16
- **Dependents:** 2
- **Priority Score:** 2023608.4
- **Functions:** 32/33 matched (target 111)
- **Missing functions:** `default`
- **Types:** 2/3 matched (target 8)
- **Missing types:** `State`

### 17. error.format

- **Target:** `error.Format`
- **Similarity:** 0.10
- **Dependents:** 2
- **Priority Score:** 2021109.0
- **Functions:** 8/9 matched (target 22)
- **Missing functions:** `try_from`
- **Types:** 1/2 matched (target 5)
- **Missing types:** `Error`

### 18. error.try_from_parsed

- **Target:** `error.TryFromParsed`
- **Similarity:** 0.12
- **Dependents:** 2
- **Priority Score:** 2010808.8
- **Functions:** 6/6 matched (target 12)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 3)
- **Missing types:** `Error`

### 19. error.invalid_format_description

- **Target:** `error.InvalidFormatDescription`
- **Similarity:** 0.16
- **Dependents:** 2
- **Priority Score:** 2010508.4
- **Functions:** 3/3 matched (target 24)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 8)
- **Missing types:** `Error`

### 20. parsing.component

- **Target:** `parsing.ComponentParsing`
- **Similarity:** 0.31
- **Dependents:** 2
- **Priority Score:** 2001706.9
- **Functions:** 17/17 matched (target 18)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 21. primitive_date_time

- **Target:** `time.PrimitiveDateTime`
- **Similarity:** 0.21
- **Dependents:** 1
- **Priority Score:** 1207407.9
- **Functions:** 53/71 matched (target 74)
- **Missing functions:** `hash`, `eq`, `partial_cmp`, `cmp`, `as_i128`, `metadata`, `fmt_with_metadata`, `fmt`, `fmt`, `add`, `add`, `add_assign`, `add_assign`, `sub`, `sub`, `sub_assign`, `sub_assign`, `sub`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Metadata`, `Output`

### 22. parsing.parsed

- **Target:** `parsing.Parsed`
- **Similarity:** 0.05
- **Dependents:** 1
- **Priority Score:** 1127509.5
- **Functions:** 62/72 matched (target 99)
- **Missing functions:** `default`, `parse_item`, `try_from`, `adjustment`, `try_from`, `utc_offset_try_from_parsed`, `try_from`, `try_from`, `try_from`, `try_from`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `AnyFormatItem`, `Error`

### 23. utc_offset

- **Target:** `time.UtcOffset`
- **Similarity:** 0.13
- **Dependents:** 1
- **Priority Score:** 1104008.6
- **Functions:** 29/32 matched (target 49)
- **Missing functions:** `metadata`, `fmt_with_metadata`, `fmt`
- **Types:** 1/8 matched (target 1)
- **Missing types:** `Hours`, `Minutes`, `Seconds`, `WholeSeconds`, `UtcOffsetMetadata`, `Metadata`, `Output`

### 24. format_description.borrowed_format_item

- **Target:** `formatdescription.BorrowedFormatItem`
- **Similarity:** 0.06
- **Dependents:** 1
- **Priority Score:** 1081109.4
- **Functions:** 2/9 matched (target 18)
- **Missing functions:** `fmt`, `try_from`, `try_from`, `eq`, `eq`, `eq`, `eq`
- **Types:** 1/2 matched (target 6)
- **Missing types:** `Error`

### 25. weekday

- **Target:** `time.Weekday`
- **Similarity:** 0.34
- **Dependents:** 1
- **Priority Score:** 1051606.6
- **Functions:** 10/12 matched (target 14)
- **Missing functions:** `metadata`, `fmt`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `WeekdayMetadata`, `Metadata`, `Err`

### 26. error.parse

- **Target:** `error.Parse`
- **Similarity:** 0.05
- **Dependents:** 1
- **Priority Score:** 1031009.4
- **Functions:** 6/8 matched (target 15)
- **Missing functions:** `try_from`, `try_from`
- **Types:** 1/2 matched (target 3)
- **Missing types:** `Error`

### 27. ext.numerical_std_duration

- **Target:** `ext.NumericalStdDuration`
- **Similarity:** 0.04
- **Dependents:** 1
- **Priority Score:** 1021809.6
- **Functions:** 16/16 matched (target 35)
- **Missing functions:** _none_
- **Types:** 0/2 matched (target 0)
- **Missing types:** `Sealed`, `NumericalStdDuration`

### 28. ext.numerical_duration

- **Target:** `ext.NumericalDuration`
- **Similarity:** 0.27
- **Dependents:** 1
- **Priority Score:** 1021807.3
- **Functions:** 16/16 matched (target 25)
- **Missing functions:** _none_
- **Types:** 0/2 matched (target 0)
- **Missing types:** `Sealed`, `NumericalDuration`

### 29. error.different_variant

- **Target:** `error.DifferentVariant`
- **Similarity:** 0.14
- **Dependents:** 1
- **Priority Score:** 1010508.6
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 30. error.parse_from_description

- **Target:** `error.ParseFromDescription`
- **Similarity:** 0.15
- **Dependents:** 1
- **Priority Score:** 1010508.4
- **Functions:** 3/3 matched (target 11)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 4)
- **Missing types:** `Error`

### 31. error.invalid_variant

- **Target:** `error.InvalidVariant`
- **Similarity:** 0.16
- **Dependents:** 1
- **Priority Score:** 1010508.4
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 32. error.conversion_range

- **Target:** `error.ConversionRange`
- **Similarity:** 0.16
- **Dependents:** 1
- **Priority Score:** 1010508.4
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 33. error.indeterminate_offset

- **Target:** `error.IndeterminateOffset`
- **Similarity:** 0.16
- **Dependents:** 1
- **Priority Score:** 1010508.4
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`

### 34. format_description.modifier

- **Target:** `formatdescription.Modifier`
- **Similarity:** 0.23
- **Dependents:** 1
- **Priority Score:** 1005707.8
- **Functions:** 31/31 matched (target 56)
- **Missing functions:** _none_
- **Types:** 26/26 matched
- **Missing types:** _none_

### 35. serde.rfc2822

- **Target:** `serde.Rfc2822`
- **Similarity:** 0.21
- **Dependents:** 1
- **Priority Score:** 1000407.9
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 36. serde.rfc3339

- **Target:** `serde.Rfc3339`
- **Similarity:** 0.21
- **Dependents:** 1
- **Priority Score:** 1000407.9
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 37. parse.format_item

- **Target:** `parse.FormatItemParser`
- **Similarity:** 0.01
- **Dependents:** 0
- **Priority Score:** 70809.9
- **Functions:** 1/6 matched (target 25)
- **Missing functions:** `parse`, `try_from`, `from`, `from`, `parse_from_modifier_value`
- **Types:** 0/2 matched (target 5)
- **Missing types:** `Item`, `Error`

### 38. parse.strftime

- **Target:** `parse.Strftime`
- **Similarity:** 0.07
- **Dependents:** 0
- **Priority Score:** 50709.3
- **Functions:** 2/5 matched (target 6)
- **Missing functions:** `lex`, `into_items`, `parse_component`
- **Types:** 0/2 matched (target 4)
- **Missing types:** `Padding`, `Token`

### 39. parse.ast

- **Target:** `parse.Ast`
- **Similarity:** 0.09
- **Dependents:** 0
- **Priority Score:** 30709.1
- **Functions:** 3/4 matched
- **Missing functions:** `parse`
- **Types:** 1/3 matched (target 8)
- **Missing types:** `Item`, `Modifier`

### 40. ext.instant

- **Target:** `ext.InstantExt`
- **Similarity:** 0.16
- **Dependents:** 0
- **Priority Score:** 20708.4
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 0/2 matched (target 0)
- **Missing types:** `Sealed`, `InstantExt`

### 41. parse.lexer

- **Target:** `parse.Lexer`
- **Similarity:** 0.02
- **Dependents:** 0
- **Priority Score:** 11309.8
- **Functions:** 8/8 matched (target 10)
- **Missing functions:** _none_
- **Types:** 4/5 matched (target 7)
- **Missing types:** `Item`

### 42. well_known.iso8601

- **Target:** `wellknown.Iso8601`
- **Similarity:** 0.19
- **Dependents:** 0
- **Priority Score:** 11308.1
- **Functions:** 6/7 matched (target 9)
- **Missing functions:** `fmt`
- **Types:** 6/6 matched (target 9)
- **Missing types:** _none_

### 43. interop.offsetdatetime_systemtime

- **Target:** `interop.OffsetDateTimeSystemTime`
- **Similarity:** 0.28
- **Dependents:** 0
- **Priority Score:** 10907.2
- **Functions:** 8/8 matched (target 14)
- **Missing functions:** _none_
- **Types:** 0/1 matched
- **Missing types:** `Output`

### 44. interop.utcdatetime_systemtime

- **Target:** `interop.UtcDateTimeSystemTime`
- **Similarity:** 0.28
- **Dependents:** 0
- **Priority Score:** 10907.2
- **Functions:** 8/8 matched (target 14)
- **Missing functions:** _none_
- **Types:** 0/1 matched
- **Missing types:** `Output`

### 45. interop.offsetdatetime_utcdatetime

- **Target:** `interop.OffsetDateTimeUtcDateTime`
- **Similarity:** 0.30
- **Dependents:** 0
- **Priority Score:** 10907.0
- **Functions:** 8/8 matched (target 11)
- **Missing functions:** _none_
- **Types:** 0/1 matched
- **Missing types:** `Output`

### 46. parsing.iso8601

- **Target:** `parsing.Iso8601Parsing`
- **Similarity:** 0.06
- **Dependents:** 0
- **Priority Score:** 10409.4
- **Functions:** 3/4 matched (target 3)
- **Missing functions:** `round`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 47. local_offset_at.unix

- **Target:** `localoffsetat.Unix`
- **Similarity:** 0.27
- **Dependents:** 0
- **Priority Score:** 10407.3
- **Functions:** 3/4 matched (target 5)
- **Missing functions:** `tm_to_offset`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 48. parsing.shim

- **Target:** `parsing.Shim [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10110.0
- **Functions:** 0/0 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 4)
- **Missing types:** `Integer`

### 49. rfc.iso8601

- **Target:** `rfc.Iso8601Combinator`
- **Similarity:** 0.12
- **Dependents:** 0
- **Priority Score:** 1508.8
- **Functions:** 14/14 matched (target 16)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 50. rfc.rfc2822

- **Target:** `rfc.Rfc2822`
- **Similarity:** 0.26
- **Dependents:** 0
- **Priority Score:** 907.4
- **Functions:** 9/9 matched (target 11)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 51. rand08

- **Target:** `time.Rand08`
- **Similarity:** 0.32
- **Dependents:** 0
- **Priority Score:** 906.8
- **Functions:** 9/9 matched (target 18)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 10)
- **Missing types:** _none_

### 52. rand09

- **Target:** `time.Rand09`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 906.7
- **Functions:** 9/9 matched (target 18)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 9)
- **Missing types:** _none_

### 53. util

- **Target:** `time.Util`
- **Similarity:** 0.41
- **Dependents:** 0
- **Priority Score:** 805.9
- **Functions:** 6/6 matched (target 18)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 4)
- **Missing types:** _none_

### 54. iso8601.adt_hack

- **Target:** `iso8601.AdtHack`
- **Similarity:** 0.15
- **Dependents:** 0
- **Priority Score:** 608.5
- **Functions:** 4/4 matched (target 13)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 2/2 matched

### 55. local_offset_at.windows

- **Target:** `localoffsetat.Windows`
- **Similarity:** 0.18
- **Dependents:** 0
- **Priority Score:** 608.2
- **Functions:** 4/4 matched (target 5)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_

### 56. timestamp.milliseconds_i64

- **Target:** `timestamp.MillisecondsI64`
- **Similarity:** 0.14
- **Dependents:** 0
- **Priority Score:** 408.6
- **Functions:** 4/4 matched (target 5)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 57. timestamp.microseconds

- **Target:** `timestamp.Microseconds`
- **Similarity:** 0.14
- **Dependents:** 0
- **Priority Score:** 408.6
- **Functions:** 4/4 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_

### 58. timestamp.milliseconds

- **Target:** `timestamp.Milliseconds`
- **Similarity:** 0.14
- **Dependents:** 0
- **Priority Score:** 408.6
- **Functions:** 4/4 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 59. timestamp.nanoseconds

- **Target:** `timestamp.Nanoseconds`
- **Similarity:** 0.15
- **Dependents:** 0
- **Priority Score:** 408.5
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 60. formatting.iso8601

- **Target:** `formatting.Iso8601Formatting`
- **Similarity:** 0.02
- **Dependents:** 0
- **Priority Score:** 309.8
- **Functions:** 3/3 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 61. hint

- **Target:** `time.Hint`
- **Similarity:** 0.18
- **Dependents:** 0
- **Priority Score:** 308.2
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 62. refresh_tz.unix

- **Target:** `refreshtz.Unix`
- **Similarity:** 0.04
- **Dependents:** 0
- **Priority Score:** 209.6
- **Functions:** 2/2 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 63. interop.js_sys_date_offsetdatetime

- **Target:** `interop.JsSysDateOffsetDateTime`
- **Similarity:** 0.07
- **Dependents:** 0
- **Priority Score:** 209.3
- **Functions:** 2/2 matched (target 8)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_

### 64. interop.js_sys_date_utcdatetime

- **Target:** `interop.JsSysDateUtcDateTime`
- **Similarity:** 0.08
- **Dependents:** 0
- **Priority Score:** 209.2
- **Functions:** 2/2 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 65. refresh_tz.imp

- **Target:** `refreshtz.Imp`
- **Similarity:** 0.50
- **Dependents:** 0
- **Priority Score:** 205.0
- **Functions:** 2/2 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 66. format_description.component

- **Target:** `formatdescription.Component`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 200.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 19)
- **Missing types:** _none_

### 67. rfc.rfc2234

- **Target:** `rfc.Rfc2234`
- **Similarity:** 0.07
- **Dependents:** 0
- **Priority Score:** 109.3
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 68. local_offset_at.imp

- **Target:** `localoffsetat.Imp`
- **Similarity:** 0.13
- **Dependents:** 0
- **Priority Score:** 108.7
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 69. local_offset_at.wasm_js

- **Target:** `localoffsetat.WasmJs`
- **Similarity:** 0.55
- **Dependents:** 0
- **Priority Score:** 104.5
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 70. well_known.rfc3339

- **Target:** `wellknown.Rfc3339`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 71. well_known.rfc2822

- **Target:** `wellknown.Rfc2822`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 72. macros

- **Target:** `time.Macros [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 8)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 73. internal_macros

- **Target:** `time.InternalMacros [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
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

