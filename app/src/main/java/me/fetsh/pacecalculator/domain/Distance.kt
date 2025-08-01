package me.fetsh.pacecalculator.domain

import me.fetsh.pacecalculator.utils.UnitConversions
import java.math.BigDecimal

enum class SystemOfUnits { Metric, Imperial }

enum class DistanceUnit(
    val system: SystemOfUnits,
    val code: String,
) {
    Meter(SystemOfUnits.Metric, "m"),
    Kilometer(SystemOfUnits.Metric, "km"),
    Mile(SystemOfUnits.Imperial, "mi"),
    ;

    val isMetric get() = system == SystemOfUnits.Metric
}

data class Distance(
    val meters: BigDecimal,
) : BigDecimalBased {
    init {
        require(meters >= BigDecimal.ZERO) { "Distance cannot be negative" }
    }

    override fun getValue(): BigDecimal = meters

    override fun equals(other: Any?): Boolean = isSameValueAs(other)

    override fun hashCode(): Int = normalizedHash()

    fun to(unit: DistanceUnit): BigDecimal =
        when (unit) {
            DistanceUnit.Meter -> meters
            DistanceUnit.Kilometer -> meters.divide(UnitConversions.METERS_IN_KILOMETER, UnitConversions.CALCULATION_PRECISION)
            DistanceUnit.Mile -> meters.divide(UnitConversions.METERS_IN_MILE, UnitConversions.CALCULATION_PRECISION)
        }

    companion object {
        fun of(
            amount: Double,
            unit: DistanceUnit,
        ): Distance = of(BigDecimal.valueOf(amount), unit)

        fun of(
            amount: BigDecimal,
            unit: DistanceUnit,
        ): Distance =
            when (unit) {
                DistanceUnit.Meter -> Distance(amount)
                DistanceUnit.Kilometer ->
                    Distance(
                        amount.multiply(UnitConversions.METERS_IN_KILOMETER, UnitConversions.CALCULATION_PRECISION),
                    )
                DistanceUnit.Mile -> Distance(amount.multiply(UnitConversions.METERS_IN_MILE, UnitConversions.CALCULATION_PRECISION))
            }

        fun of(
            time: Time,
            pace: Pace,
        ): Distance =
            time
                .seconds
                .divide(pace.secondsPerKilometer, UnitConversions.CALCULATION_PRECISION)
                .let { of(it, DistanceUnit.Kilometer) }
    }
}

sealed interface DistanceExtended {
    val distance: Distance
}

data class PlainDistance(
    val amount: Double,
    val unit: DistanceUnit,
) : DistanceExtended {
    override val distance: Distance = Distance.of(amount, unit)

//    companion object {
//        fun of(
//            millimeters: Long,
//            distanceUnit: DistanceUnit,
//        ): PlainDistance =
//            PlainDistance(
//                amount = millimeters / distanceUnit.millimetersPerUnit.toDouble(),
//                unit = distanceUnit,
//            )
//    }
}

data class NakedDistance(
    override val distance: Distance,
) : DistanceExtended

enum class NamedDistance(
    val code: String,
    val distance_: Distance,
) : DistanceExtended {
    Marathon("marathon", Distance.of(42.195, DistanceUnit.Kilometer)),
    HalfMarathon("half_marathon", Distance.of(21.0975, DistanceUnit.Kilometer)),
    ;

    override val distance: Distance get() = distance_
}

val DISTANCE_OPTIONS: List<DistanceExtended> =
    listOf(
        PlainDistance(5.0, DistanceUnit.Kilometer),
        PlainDistance(10.0, DistanceUnit.Kilometer),
        PlainDistance(10.0, DistanceUnit.Mile),
        NamedDistance.HalfMarathon,
        NamedDistance.Marathon,
    )
