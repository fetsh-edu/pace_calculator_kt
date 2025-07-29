package me.fetsh.pacecalculator.domain

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

private const val MILLIS_IN_SECOND = 1_000L
private const val MILLIS_IN_MINUTE = 60_000L // 60 × 1 000
private const val MILLIS_IN_HOUR = 3_600_000L // 60 × 60 × 1 000

data class TimeParts(
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val milliseconds: Int,
) {
    init {
        require(hours >= 0) { "Hours must be non-negative" }
        require(minutes >= 0) { "Minutes must be non-negative" }
        require(seconds >= 0) { "Seconds must be non-negative" }
        require(milliseconds in 0..999) { "milliseconds must be 0‥999" }
    }

    companion object {
        fun of(
            hours: Int,
            minutes: Int,
            seconds: Int,
            fraction: Int, // 0..9 for deci, 0..99 for centi, 0..999 for milli
            precision: TimePrecision,
        ): TimeParts {
            val maxFraction =
                when (precision.fractionalDigits) {
                    0 -> 0
                    1 -> 9
                    2 -> 99
                    3 -> 999
                    else -> error("Unsupported precision")
                }
            require(fraction in 0..maxFraction)
            val fractionAsMilliseconds =
                when (precision) {
                    TimePrecision.Seconds -> 0
                    TimePrecision.Deciseconds -> fraction * 100
                    TimePrecision.Centiseconds -> fraction * 10
                    TimePrecision.Milliseconds -> fraction
                }
            return TimeParts(hours, minutes, seconds, fractionAsMilliseconds)
        }
    }
}

data class Time(
    val milliseconds: Long,
) {
    init {
        require(milliseconds >= 0) { "Time cannot be negative" }
    }

    companion object {
        fun fromSeconds(seconds: Long): Time = Time(seconds * MILLIS_IN_SECOND)

        fun fromMinutes(minutes: Long): Time = Time(minutes * MILLIS_IN_MINUTE)

        fun of(parts: TimeParts): Time =
            Time(parts.hours * MILLIS_IN_HOUR + parts.minutes * MILLIS_IN_MINUTE + parts.seconds * MILLIS_IN_SECOND + parts.milliseconds)

        fun of(
            pace: Pace,
            distance: Distance,
        ): Time {
            val distanceInKilometers =
                BigDecimal(distance.millimeters).divide(BigDecimal(MILLIMETERS_IN_KILOMETER), 9, RoundingMode.HALF_UP)

            val totalMicroseconds =
                BigDecimal(pace.microsecondsPerKilometer).multiply(distanceInKilometers)

            val totalMilliseconds =
                totalMicroseconds.divide(BigDecimal(1_000), 0, RoundingMode.HALF_UP).longValueExactSafe()

            return Time(totalMilliseconds)
        }
    }

    fun roundTo(
        precision: TimePrecision,
        mode: RoundingMode = RoundingMode.HALF_UP,
    ): Time = roundMsToPrecision(milliseconds, precision, mode).let(::Time)

    fun multiply(
        factor: BigDecimal,
        mode: RoundingMode = RoundingMode.HALF_UP,
    ): Time {
        require(factor >= BigDecimal.ZERO) { "Factor must be non-negative" }
        return Time(BigDecimal(milliseconds).multiply(factor).setScale(0, mode).longValueExactSafe())
    }

    fun divide(
        divisor: BigDecimal,
        mode: RoundingMode = RoundingMode.HALF_UP,
    ): Time {
        require(divisor != BigDecimal.ZERO) { "Divisor must not be zero" }
        return Time(BigDecimal(milliseconds).divide(divisor, 0, mode).longValueExactSafe())
    }

    /**
     * Converts this [Time] to canonical [TimeParts].
     *
     * The result is normalised so that `minutes` and `seconds` are in 0‥59.
     */
    fun toParts(): TimeParts {
        val hours = (milliseconds / MILLIS_IN_HOUR).toInt()
        val minutes = ((milliseconds % MILLIS_IN_HOUR) / MILLIS_IN_MINUTE).toInt()
        val seconds = ((milliseconds % MILLIS_IN_MINUTE) / MILLIS_IN_SECOND).toInt()
        val millis = (milliseconds % MILLIS_IN_SECOND).toInt() // 0‥999, matches the precision

        return TimeParts(hours, minutes, seconds, millis)
    }
}

enum class TimePrecision(
    val fractionalDigits: Int,
) {
    Seconds(0),
    Deciseconds(1),
    Centiseconds(2),
    Milliseconds(3),
}

private fun roundMsToPrecision(
    ms: Long,
    precision: TimePrecision,
    mode: RoundingMode,
): Long {
    if (precision == TimePrecision.Milliseconds) return ms
    val fracDigits = precision.fractionalDigits // 0,1,2
    val keepDigits = 3 - fracDigits // digits we keep in ms
    val pow = pow10(keepDigits)
    val bd = BigDecimal(ms).divide(BigDecimal(pow), 0, mode) // integer chunks
    val result = bd.multiply(BigDecimal(pow))
    return result.longValueExactSafe()
}

private fun pow10(exp: Int): Long =
    when (exp) {
        0 -> 1L
        1 -> 10L
        2 -> 100L
        3 -> 1000L
        else -> 10.0.pow(exp).toLong() // unlikely
    }

private fun Double.pow(exp: Int): Double = this.pow(exp.toDouble())
