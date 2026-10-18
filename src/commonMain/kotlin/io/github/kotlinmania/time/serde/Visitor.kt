// port-lint: source serde/visitor.rs
package io.github.kotlinmania.time.serde

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.Expected
import io.github.kotlinmania.serdecore.de.I8Deserialize
import io.github.kotlinmania.serdecore.de.I32Deserialize
import io.github.kotlinmania.serdecore.de.I64Deserialize
import io.github.kotlinmania.serdecore.de.SeqAccess
import io.github.kotlinmania.serdecore.de.U8Deserialize
import io.github.kotlinmania.serdecore.de.U16Deserialize
import io.github.kotlinmania.serdecore.de.U32Deserialize
import io.github.kotlinmania.serdecore.de.Unexpected
import io.github.kotlinmania.serdecore.de.Visitor
import io.github.kotlinmania.time.Date
import io.github.kotlinmania.time.Duration
import io.github.kotlinmania.time.Month
import io.github.kotlinmania.time.OffsetDateTime
import io.github.kotlinmania.time.PrimitiveDateTime
import io.github.kotlinmania.time.Time
import io.github.kotlinmania.time.UtcDateTime
import io.github.kotlinmania.time.UtcOffset
import io.github.kotlinmania.time.Weekday
import io.github.kotlinmania.time.error.intoDeError
import io.github.kotlinmania.time.formatdescription.wellknown.Iso8601
import io.github.kotlinmania.time.formatdescription.wellknown.Rfc2822
import io.github.kotlinmania.time.formatdescription.wellknown.Rfc3339

private fun <T> nextItem(
    seq: SeqAccess,
    deserialize: Deserialize<T>,
    name: String,
): SerdeResult<T> =
    when (val res = seq.nextElement(deserialize)) {
        is SerdeResult.Success -> {
            val v = res.value
            if (v != null) {
                SerdeResult.success(v)
            } else {
                SerdeResult.failure(SerdeError.custom("expected $name"))
            }
        }
        is SerdeResult.Failure -> res
    }

internal object DateVisitor : Visitor<Date> {
    override fun expecting(): String = "a `Date`"

    override fun visitStr(v: String): SerdeResult<Date> =
        Date.parse(v, DATE_FORMAT).fold(
            onSuccess = { SerdeResult.success(it) },
            onFailure = { SerdeResult.failure(SerdeError.custom(it.message ?: it.toString())) },
        )

    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<Date> {
        val year =
            when (val r = nextItem(access, I32Deserialize, "year")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val ordinal =
            when (val r = nextItem(access, U16Deserialize, "day of year")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        return Date.fromOrdinalDate(year, ordinal.toInt()).fold(
            onSuccess = { SerdeResult.success(it) },
            onFailure = { SerdeResult.failure(it.intoDeError()) },
        )
    }
}

internal object DurationVisitor : Visitor<Duration> {
    override fun expecting(): String = "a `Duration`"

    override fun visitStr(v: String): SerdeResult<Duration> {
        val dotIdx = v.indexOf('.')
        if (dotIdx < 0) {
            return SerdeResult.failure(
                SerdeError.invalidValue(
                    Unexpected.Str(v),
                    object : Expected {
                        override fun expecting(): String = "a decimal point"
                    },
                ),
            )
        }
        val secondsStr = v.substring(0, dotIdx)
        val nanosecondsStr = v.substring(dotIdx + 1)
        val seconds =
            secondsStr.toLongOrNull()
                ?: return SerdeResult.failure(
                    SerdeError.invalidValue(
                        Unexpected.Str(secondsStr),
                        object : Expected {
                            override fun expecting(): String = "seconds"
                        },
                    ),
                )
        var nanoseconds =
            nanosecondsStr.toIntOrNull()
                ?: return SerdeResult.failure(
                    SerdeError.invalidValue(
                        Unexpected.Str(nanosecondsStr),
                        object : Expected {
                            override fun expecting(): String = "nanoseconds"
                        },
                    ),
                )
        if (seconds < 0L || (seconds == 0L && v.startsWith("-"))) {
            nanoseconds *= -1
        }
        return SerdeResult.success(Duration.new(seconds, nanoseconds))
    }

    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<Duration> {
        val seconds =
            when (val r = nextItem(access, I64Deserialize, "seconds")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val nanoseconds =
            when (val r = nextItem(access, I32Deserialize, "nanoseconds")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        return SerdeResult.success(Duration.new(seconds, nanoseconds))
    }
}

internal object OffsetDateTimeVisitor : Visitor<OffsetDateTime> {
    override fun expecting(): String = "an `OffsetDateTime`"

    override fun visitStr(v: String): SerdeResult<OffsetDateTime> =
        OffsetDateTime.parse(v, OFFSET_DATE_TIME_FORMAT).fold(
            onSuccess = { SerdeResult.success(it) },
            onFailure = { SerdeResult.failure(SerdeError.custom(it.message ?: it.toString())) },
        )

    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<OffsetDateTime> {
        val year =
            when (val r = nextItem(access, I32Deserialize, "year")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val ordinal =
            when (val r = nextItem(access, U16Deserialize, "day of year")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val hour =
            when (val r = nextItem(access, U8Deserialize, "hour")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val minute =
            when (val r = nextItem(access, U8Deserialize, "minute")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val second =
            when (val r = nextItem(access, U8Deserialize, "second")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val nanosecond =
            when (val r = nextItem(access, U32Deserialize, "nanosecond")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val offsetHours =
            when (val r = nextItem(access, I8Deserialize, "offset hours")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val offsetMinutes =
            when (val r = nextItem(access, I8Deserialize, "offset minutes")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val offsetSeconds =
            when (val r = nextItem(access, I8Deserialize, "offset seconds")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }

        val date =
            Date.fromOrdinalDate(year, ordinal.toInt()).getOrElse {
                return SerdeResult.failure(it.intoDeError())
            }
        val dt =
            date
                .withHmsNano(hour.toInt(), minute.toInt(), second.toInt(), nanosecond.toInt())
                .getOrElse { return SerdeResult.failure(it.intoDeError()) }
        val offset =
            UtcOffset
                .fromHms(offsetHours.toInt(), offsetMinutes.toInt(), offsetSeconds.toInt())
                .getOrElse { return SerdeResult.failure(it.intoDeError()) }
        return SerdeResult.success(dt.assumeOffset(offset))
    }
}

internal object PrimitiveDateTimeVisitor : Visitor<PrimitiveDateTime> {
    override fun expecting(): String = "a `PrimitiveDateTime`"

    override fun visitStr(v: String): SerdeResult<PrimitiveDateTime> =
        PrimitiveDateTime.parse(v, PRIMITIVE_DATE_TIME_FORMAT).fold(
            onSuccess = { SerdeResult.success(it) },
            onFailure = { SerdeResult.failure(SerdeError.custom(it.message ?: it.toString())) },
        )

    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<PrimitiveDateTime> {
        val year =
            when (val r = nextItem(access, I32Deserialize, "year")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val ordinal =
            when (val r = nextItem(access, U16Deserialize, "day of year")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val hour =
            when (val r = nextItem(access, U8Deserialize, "hour")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val minute =
            when (val r = nextItem(access, U8Deserialize, "minute")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val second =
            when (val r = nextItem(access, U8Deserialize, "second")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val nanosecond =
            when (val r = nextItem(access, U32Deserialize, "nanosecond")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }

        val date =
            Date.fromOrdinalDate(year, ordinal.toInt()).getOrElse {
                return SerdeResult.failure(it.intoDeError())
            }
        val dt =
            date
                .withHmsNano(hour.toInt(), minute.toInt(), second.toInt(), nanosecond.toInt())
                .getOrElse { return SerdeResult.failure(it.intoDeError()) }
        return SerdeResult.success(dt)
    }
}

internal object UtcDateTimeVisitor : Visitor<UtcDateTime> {
    override fun expecting(): String = "a `PrimitiveDateTime`"

    override fun visitStr(v: String): SerdeResult<UtcDateTime> =
        UtcDateTime.parse(v, UTC_DATE_TIME_FORMAT).fold(
            onSuccess = { SerdeResult.success(it) },
            onFailure = { SerdeResult.failure(SerdeError.custom(it.message ?: it.toString())) },
        )

    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<UtcDateTime> =
        PrimitiveDateTimeVisitor.visitSeq(access).map { UtcDateTime.fromPrimitive(it) }
}

internal object TimeVisitor : Visitor<Time> {
    override fun expecting(): String = "a `Time`"

    override fun visitStr(v: String): SerdeResult<Time> =
        Time.parse(v, TIME_FORMAT).fold(
            onSuccess = { SerdeResult.success(it) },
            onFailure = { SerdeResult.failure(SerdeError.custom(it.message ?: it.toString())) },
        )

    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<Time> {
        val hour =
            when (val r = nextItem(access, U8Deserialize, "hour")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val minute =
            when (val r = nextItem(access, U8Deserialize, "minute")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val second =
            when (val r = nextItem(access, U8Deserialize, "second")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        val nanosecond =
            when (val r = nextItem(access, U32Deserialize, "nanosecond")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }

        return Time.fromHmsNano(hour.toInt(), minute.toInt(), second.toInt(), nanosecond.toInt()).fold(
            onSuccess = { SerdeResult.success(it) },
            onFailure = { SerdeResult.failure(it.intoDeError()) },
        )
    }
}

internal object UtcOffsetVisitor : Visitor<UtcOffset> {
    override fun expecting(): String = "a `UtcOffset`"

    override fun visitStr(v: String): SerdeResult<UtcOffset> =
        UtcOffset.parse(v, UTC_OFFSET_FORMAT).fold(
            onSuccess = { SerdeResult.success(it) },
            onFailure = { SerdeResult.failure(SerdeError.custom(it.message ?: it.toString())) },
        )

    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<UtcOffset> {
        val hours =
            when (val r = nextItem(access, I8Deserialize, "offset hours")) {
                is SerdeResult.Success -> r.value
                is SerdeResult.Failure -> return r
            }
        var minutes: Byte = 0
        var seconds: Byte = 0

        val minRes = access.nextElement(I8Deserialize)
        if (minRes is SerdeResult.Success) {
            val minVal = minRes.value
            if (minVal != null) {
                minutes = minVal
                val secRes = access.nextElement(I8Deserialize)
                if (secRes is SerdeResult.Success) {
                    val secVal = secRes.value
                    if (secVal != null) {
                        seconds = secVal
                    }
                }
            }
        }


        return UtcOffset.fromHms(hours.toInt(), minutes.toInt(), seconds.toInt()).fold(
            onSuccess = { SerdeResult.success(it) },
            onFailure = { SerdeResult.failure(it.intoDeError()) },
        )
    }
}

internal object WeekdayVisitor : Visitor<Weekday> {
    override fun expecting(): String = "a `Weekday`"

    override fun visitStr(v: String): SerdeResult<Weekday> =
        when (v) {
            "Monday" -> SerdeResult.success(Weekday.Monday)
            "Tuesday" -> SerdeResult.success(Weekday.Tuesday)
            "Wednesday" -> SerdeResult.success(Weekday.Wednesday)
            "Thursday" -> SerdeResult.success(Weekday.Thursday)
            "Friday" -> SerdeResult.success(Weekday.Friday)
            "Saturday" -> SerdeResult.success(Weekday.Saturday)
            "Sunday" -> SerdeResult.success(Weekday.Sunday)
            else ->
                SerdeResult.failure(
                    SerdeError.invalidValue(
                        Unexpected.Str(v),
                        this,
                    ),
                )
        }

    override fun visitU64(v: ULong): SerdeResult<Weekday> =
        when (v) {
            1uL -> SerdeResult.success(Weekday.Monday)
            2uL -> SerdeResult.success(Weekday.Tuesday)
            3uL -> SerdeResult.success(Weekday.Wednesday)
            4uL -> SerdeResult.success(Weekday.Thursday)
            5uL -> SerdeResult.success(Weekday.Friday)
            6uL -> SerdeResult.success(Weekday.Saturday)
            7uL -> SerdeResult.success(Weekday.Sunday)
            else ->
                SerdeResult.failure(
                    SerdeError.invalidValue(
                        Unexpected.Unsigned(v),
                        object : Expected {
                            override fun expecting(): String = "a value in the range 1..=7"
                        },
                    ),
                )
        }

    override fun visitI64(v: Long): SerdeResult<Weekday> =
        if (v >= 0L) {
            visitU64(v.toULong())
        } else {
            SerdeResult.failure(
                SerdeError.invalidValue(
                    Unexpected.Signed(v),
                    object : Expected {
                        override fun expecting(): String = "a value in the range 1..=7"
                    },
                ),
            )
        }
}

internal object MonthVisitor : Visitor<Month> {
    override fun expecting(): String = "a `Month`"

    override fun visitStr(v: String): SerdeResult<Month> =
        when (v) {
            "January" -> SerdeResult.success(Month.January)
            "February" -> SerdeResult.success(Month.February)
            "March" -> SerdeResult.success(Month.March)
            "April" -> SerdeResult.success(Month.April)
            "May" -> SerdeResult.success(Month.May)
            "June" -> SerdeResult.success(Month.June)
            "July" -> SerdeResult.success(Month.July)
            "August" -> SerdeResult.success(Month.August)
            "September" -> SerdeResult.success(Month.September)
            "October" -> SerdeResult.success(Month.October)
            "November" -> SerdeResult.success(Month.November)
            "December" -> SerdeResult.success(Month.December)
            else ->
                SerdeResult.failure(
                    SerdeError.invalidValue(
                        Unexpected.Str(v),
                        this,
                    ),
                )
        }

    override fun visitU64(v: ULong): SerdeResult<Month> =
        when (v) {
            1uL -> SerdeResult.success(Month.January)
            2uL -> SerdeResult.success(Month.February)
            3uL -> SerdeResult.success(Month.March)
            4uL -> SerdeResult.success(Month.April)
            5uL -> SerdeResult.success(Month.May)
            6uL -> SerdeResult.success(Month.June)
            7uL -> SerdeResult.success(Month.July)
            8uL -> SerdeResult.success(Month.August)
            9uL -> SerdeResult.success(Month.September)
            10uL -> SerdeResult.success(Month.October)
            11uL -> SerdeResult.success(Month.November)
            12uL -> SerdeResult.success(Month.December)
            else ->
                SerdeResult.failure(
                    SerdeError.invalidValue(
                        Unexpected.Unsigned(v),
                        object : Expected {
                            override fun expecting(): String = "a value in the range 1..=12"
                        },
                    ),
                )
        }

    override fun visitI64(v: Long): SerdeResult<Month> =
        if (v >= 0L) {
            visitU64(v.toULong())
        } else {
            SerdeResult.failure(
                SerdeError.invalidValue(
                    Unexpected.Signed(v),
                    object : Expected {
                        override fun expecting(): String = "a value in the range 1..=12"
                    },
                ),
            )
        }
}

internal object Rfc2822Visitor : Visitor<OffsetDateTime> {
    override fun expecting(): String = "an RFC2822-formatted `OffsetDateTime`"

    override fun visitStr(v: String): SerdeResult<OffsetDateTime> =
        OffsetDateTime.parse(v, Rfc2822).fold(
            onSuccess = { SerdeResult.success(it) },
            onFailure = { SerdeResult.failure(SerdeError.custom(it.message ?: it.toString())) },
        )
}

internal object OptionRfc2822Visitor : Visitor<OffsetDateTime?> {
    override fun expecting(): String = "an RFC2822-formatted `Option<OffsetDateTime>`"

    override fun <D : Deserializer> visitSome(deserializer: D): SerdeResult<OffsetDateTime?> =
        deserializer.deserializeAny(Rfc2822Visitor).map { it }

    override fun visitNone(): SerdeResult<OffsetDateTime?> = SerdeResult.success(null)

    override fun visitUnit(): SerdeResult<OffsetDateTime?> = SerdeResult.success(null)
}

internal object Rfc3339Visitor : Visitor<OffsetDateTime> {
    override fun expecting(): String = "an RFC3339-formatted `OffsetDateTime`"

    override fun visitStr(v: String): SerdeResult<OffsetDateTime> =
        OffsetDateTime.parse(v, Rfc3339).fold(
            onSuccess = { SerdeResult.success(it) },
            onFailure = { SerdeResult.failure(SerdeError.custom(it.message ?: it.toString())) },
        )
}

internal object OptionRfc3339Visitor : Visitor<OffsetDateTime?> {
    override fun expecting(): String = "an RFC3339-formatted `Option<OffsetDateTime>`"

    override fun <D : Deserializer> visitSome(deserializer: D): SerdeResult<OffsetDateTime?> =
        deserializer.deserializeAny(Rfc3339Visitor).map { it }

    override fun visitNone(): SerdeResult<OffsetDateTime?> = SerdeResult.success(null)

    override fun visitUnit(): SerdeResult<OffsetDateTime?> = SerdeResult.success(null)
}

internal object Iso8601SerdeVisitor : Visitor<OffsetDateTime> {
    override fun expecting(): String = "an ISO 8601-formatted `OffsetDateTime`"

    override fun visitStr(v: String): SerdeResult<OffsetDateTime> =
        OffsetDateTime.parse(v, Iso8601(SERDE_CONFIG)).fold(
            onSuccess = { SerdeResult.success(it) },
            onFailure = { SerdeResult.failure(SerdeError.custom(it.message ?: it.toString())) },
        )
}

internal object OptionIso8601SerdeVisitor : Visitor<OffsetDateTime?> {
    override fun expecting(): String = "an ISO 8601-formatted `Option<OffsetDateTime>`"

    override fun <D : Deserializer> visitSome(deserializer: D): SerdeResult<OffsetDateTime?> =
        deserializer.deserializeAny(Iso8601SerdeVisitor).map { it }

    override fun visitNone(): SerdeResult<OffsetDateTime?> = SerdeResult.success(null)

    override fun visitUnit(): SerdeResult<OffsetDateTime?> = SerdeResult.success(null)
}
