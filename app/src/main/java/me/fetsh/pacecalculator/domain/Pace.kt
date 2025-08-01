package me.fetsh.pacecalculator.domain

import me.fetsh.pacecalculator.utils.UnitConversions
import java.math.BigDecimal
import java.math.RoundingMode

enum class PaceUnit(
    val distance: DistanceUnit,
) {
    PerKilometre(DistanceUnit.Kilometer),
    PerMile(DistanceUnit.Mile),
}

data class Pace(
    val secondsPerKilometer: BigDecimal,
) : BigDecimalBased {
    init {
        require(secondsPerKilometer > BigDecimal.ZERO) { "Pace must be positive" }
    }

    override fun getValue(): BigDecimal = secondsPerKilometer

    override fun equals(other: Any?): Boolean = isSameValueAs(other)

    override fun hashCode(): Int = normalizedHash()

    companion object {
        fun of(
            time: Time,
            distance: Distance,
        ): Pace {
            require(distance.meters > BigDecimal.ZERO) { "Distance must be positive" }

            return time
                .seconds
                .multiply(UnitConversions.METERS_IN_KILOMETER)
                .divide(distance.meters, UnitConversions.TIME_CALCULATION_PRECISION)
                .let(::Pace)
        }

        /**
         * Creates a [Pace] from human-readable minutes, seconds, and fractional parts per unit distance.
         */
        fun of(
            minutes: Int,
            seconds: Int,
            fractional: Int,
            precision: TimePrecision,
            unit: PaceUnit,
        ): Pace {
            val time = Time.of(TimeParts.of(0, minutes, seconds, fractional, precision))
            val distance = Distance.of(1.0, unit.distance)
            return of(time, distance)
        }

        fun of(
            minutes: Int,
            seconds: Int,
            milliseconds: Int,
            unit: PaceUnit,
        ): Pace =
            of(
                Time.of(TimeParts(0, minutes, seconds, milliseconds)),
                Distance.of(1.0, unit.distance),
            )

        fun of(speed: Speed): Pace {
            require(speed.metersPerSecond > BigDecimal.ZERO) { "Speed must be positive" }
            return UnitConversions.METERS_IN_KILOMETER
                .divide(speed.metersPerSecond, UnitConversions.TIME_CALCULATION_PRECISION)
                .let(::Pace)
        }
    }

    fun toSpeed(): Speed = Speed.of(this)

    fun toTimeParts(): TimeParts = Time.of(this, Distance.of(1.0, DistanceUnit.Kilometer)).toParts()

    fun roundTo(
        precision: TimePrecision,
        mode: RoundingMode = RoundingMode.HALF_UP,
    ): Pace = Pace(secondsPerKilometer.setScale(precision.fractionalDigits, mode))
}
