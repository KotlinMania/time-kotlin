package io.github.kotlinmania.time.convert

/**
 * Conversion between units of time.
 */
sealed class TimeUnit(
    internal val nanosFactor: ULong,
) {
    fun per(larger: TimeUnit): ULong {
        require(larger.nanosFactor >= this.nanosFactor && larger.nanosFactor % this.nanosFactor == 0uL) {
            "invalid unit conversion from $this to $larger"
        }
        return larger.nanosFactor / this.nanosFactor
    }

    inline fun <reified T> perT(larger: TimeUnit): T {
        val ratio = per(larger)
        return when (T::class) {
            UByte::class -> ratio.toUByte() as T
            UShort::class -> ratio.toUShort() as T
            UInt::class -> ratio.toUInt() as T
            ULong::class -> ratio as T
            Byte::class -> ratio.toByte() as T
            Short::class -> ratio.toShort() as T
            Int::class -> ratio.toInt() as T
            Long::class -> ratio.toLong() as T
            Float::class -> ratio.toFloat() as T
            Double::class -> ratio.toDouble() as T
            else -> error("unsupported target type for perT: ${T::class}")
        }
    }
}

object Nanosecond : TimeUnit(1uL) {
    const val PER_MICROSECOND: UShort = 1_000u
    const val PER_MILLISECOND: UInt = 1_000_000u
    const val PER_SECOND: UInt = 1_000_000_000u
    const val PER_MINUTE: ULong = 60_000_000_000uL
    const val PER_HOUR: ULong = 3_600_000_000_000uL
    const val PER_DAY: ULong = 86_400_000_000_000uL
    const val PER_WEEK: ULong = 604_800_000_000_000uL
}

object Microsecond : TimeUnit(1_000uL) {
    const val PER_MILLISECOND: UShort = 1_000u
    const val PER_SECOND: UInt = 1_000_000u
    const val PER_MINUTE: UInt = 60_000_000u
    const val PER_HOUR: ULong = 3_600_000_000uL
    const val PER_DAY: ULong = 86_400_000_000uL
    const val PER_WEEK: ULong = 604_800_000_000uL
}

object Millisecond : TimeUnit(1_000_000uL) {
    const val PER_SECOND: UShort = 1_000u
    const val PER_MINUTE: UInt = 60_000u
    const val PER_HOUR: UInt = 3_600_000u
    const val PER_DAY: UInt = 86_400_000u
    const val PER_WEEK: UInt = 604_800_000u
}

object Second : TimeUnit(1_000_000_000uL) {
    const val PER_MINUTE: UByte = 60u
    const val PER_HOUR: UShort = 3_600u
    const val PER_DAY: UInt = 86_400u
    const val PER_WEEK: UInt = 604_800u
}

object Minute : TimeUnit(60_000_000_000uL) {
    const val PER_HOUR: UByte = 60u
    const val PER_DAY: UShort = 1_440u
    const val PER_WEEK: UShort = 10_080u
}

object Hour : TimeUnit(3_600_000_000_000uL) {
    const val PER_DAY: UByte = 24u
    const val PER_WEEK: UByte = 168u
}

object Day : TimeUnit(86_400_000_000_000uL) {
    const val PER_WEEK: UByte = 7u
}

object Week : TimeUnit(604_800_000_000_000uL)
