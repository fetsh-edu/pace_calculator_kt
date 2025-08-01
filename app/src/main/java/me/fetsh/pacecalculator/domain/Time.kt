package me.fetsh.pacecalculator.domain

import me.fetsh.pacecalculator.utils.UnitConversions
import java.math.BigDecimal
import java.math.RoundingMode

enum class TimePrecision(
    val fractionalDigits: Int,
) {
    Seconds(0),
    Deciseconds(1),
    Centiseconds(2),
    Milliseconds(3),
}

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
                when (precision) {
                    TimePrecision.Seconds -> 0
                    TimePrecision.Deciseconds -> 9
                    TimePrecision.Centiseconds -> 99
                    TimePrecision.Milliseconds -> 999
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
    val seconds: BigDecimal,
) : BigDecimalBased {
    init {
        require(seconds > BigDecimal.ZERO) { "Time must be positive" }
    }

    override fun getValue(): BigDecimal = seconds

    override fun equals(other: Any?): Boolean = isSameValueAs(other)

    override fun hashCode(): Int = normalizedHash()

    val hoursP: Int get() = seconds.divide(UnitConversions.SECONDS_IN_HOUR, 0, RoundingMode.DOWN).toInt()
    val minutesP: Int get() =
        (
            seconds.remainder(
                UnitConversions.SECONDS_IN_HOUR,
            )
        ).divide(UnitConversions.SECONDS_IN_MINUTE, 0, RoundingMode.DOWN).toInt()
    val secondsP: Int get() = seconds.remainder(UnitConversions.SECONDS_IN_MINUTE).setScale(0, RoundingMode.DOWN).toInt()
    val millisecondsP: Int get() =
        (
            (
                seconds.remainder(
                    BigDecimal.ONE,
                )
            ).multiply(UnitConversions.MILLISECONDS_IN_SECOND)
        ).setScale(0, RoundingMode.HALF_UP).toInt()

    companion object {
        fun fromSeconds(seconds: BigDecimal): Time = Time(seconds)

        fun fromMinutes(minutes: BigDecimal): Time = Time(minutes.multiply(UnitConversions.SECONDS_IN_MINUTE))

        fun fromMinutes(minutes: Int): Time = fromMinutes(BigDecimal(minutes))

        fun fromSeconds(minutes: Int): Time = fromSeconds(BigDecimal(minutes))

        fun of(parts: TimeParts): Time {
            val seconds =
                BigDecimal(parts.hours)
                    .multiply(UnitConversions.SECONDS_IN_HOUR)
                    .add(BigDecimal(parts.minutes).multiply(UnitConversions.SECONDS_IN_MINUTE))
                    .add(BigDecimal(parts.seconds))
                    .add(BigDecimal(parts.milliseconds).divide(UnitConversions.MILLISECONDS_IN_SECOND, 6, RoundingMode.HALF_UP))
            return Time(seconds)
        }

        fun of(
            pace: Pace,
            distance: Distance,
        ): Time = Time(pace.secondsPerKilometer.multiply(distance.to(DistanceUnit.Kilometer)))
    }

    fun roundTo(
        precision: TimePrecision,
        mode: RoundingMode = RoundingMode.HALF_UP,
    ): Time = Time(seconds.setScale(precision.fractionalDigits, mode))

    fun multiply(factor: BigDecimal): Time = seconds.multiply(factor, UnitConversions.TIME_CALCULATION_PRECISION).let(::Time)

    fun divide(divisor: BigDecimal): Time {
        require(divisor != BigDecimal.ZERO) { "Divisor must not be zero" }
        return seconds.divide(divisor, UnitConversions.TIME_CALCULATION_PRECISION).let(::Time)
    }

    /**
     * Converts this [Time] to canonical [TimeParts].
     *
     * The result is normalised so that `minutes` and `seconds` are in 0‥59.
     */
    fun toParts(): TimeParts = TimeParts(hoursP, minutesP, secondsP, millisecondsP)
}
