package me.fetsh.pacecalculator.domain

import me.fetsh.pacecalculator.utils.UnitConversions
import java.math.BigDecimal

enum class SpeedUnit {
    KmpH,
    MpH,
}

data class Speed(
    val metersPerSecond: BigDecimal,
) : BigDecimalBased {
    init {
        require(metersPerSecond > BigDecimal.ZERO) { "Speed must be non-negative or zero, got: $metersPerSecond" }
    }

    override fun getValue(): BigDecimal = metersPerSecond

    override fun equals(other: Any?): Boolean = isSameValueAs(other)

    override fun hashCode(): Int = normalizedHash()

    val kilometersPerHour: BigDecimal
        get() = metersPerSecond.multiply(UnitConversions.METERS_PER_SECOND_TO_KILOMETERS_PER_HOUR)

    val milesPerHour: BigDecimal
        get() = metersPerSecond.multiply(UnitConversions.METERS_PER_SECOND_TO_MILES_PER_HOUR)

//    override fun toString(): String = "%.2f m/s".format(metersPerSecond)

    companion object {
        fun of(pace: Pace): Speed {
            require(pace.secondsPerKilometer > BigDecimal.ZERO) { "Pace must be positive" }

            return UnitConversions.METERS_IN_KILOMETER
                .divide(pace.secondsPerKilometer, UnitConversions.TIME_CALCULATION_PRECISION)
                .let(::Speed)
        }

        fun fromKilometersPerHour(kilometersPerHour: Double): Speed =
            kilometersPerHour
                .let(BigDecimal::valueOf)
                .multiply(UnitConversions.KILOMETERS_PER_HOUR_TO_METERS_PER_SECOND, UnitConversions.TIME_CALCULATION_PRECISION)
                .let(::Speed)

        fun fromMilesPerHour(milesPerHour: Double): Speed =
            milesPerHour
                .let(BigDecimal::valueOf)
                .multiply(UnitConversions.MILES_PER_HOUR_TO_METERS_PER_SECOND)
                .let(::Speed)
    }
}
