package me.fetsh.pacecalculator.domain

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToLong

enum class SystemOfUnits { Metric, Imperial }

enum class DistanceUnit(
    val system: SystemOfUnits,
    val code: String,
    val millimetersPerUnit: Int,
) {
    Meter(SystemOfUnits.Metric, "m", 1_000),
    Kilometer(SystemOfUnits.Metric, "km", 1_000_000),
    Mile(SystemOfUnits.Imperial, "mi", 1_609_344),
    ;

    val isMetric get() = system == SystemOfUnits.Metric
}

@JvmInline
value class Distance(
    val millimeters: Long,
) {
    init {
        require(millimeters >= 0) { "Distance cannot be negative" }
    }

    fun to(unit: DistanceUnit): Double = millimeters / unit.millimetersPerUnit.toDouble()

    companion object {
        fun of(
            amount: Double,
            unit: DistanceUnit,
        ): Distance = Distance((amount * unit.millimetersPerUnit).roundToLong())

        fun of(
            time: Time,
            pace: Pace,
        ): Distance {
            require(pace.microsecondsPerKilometer > 0) { "Pace must be positive" }

            val totalMicroseconds = BigDecimal(time.milliseconds).multiply(BigDecimal(1_000))
            val kilometers =
                totalMicroseconds
                    .divide(BigDecimal(pace.microsecondsPerKilometer), 9, RoundingMode.HALF_UP)

            val millimeters =
                kilometers
                    .multiply(BigDecimal(MILLIMETERS_IN_KILOMETER))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExactSafe()

            return Distance(millimeters)
        }
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

    companion object {
        fun of(
            millimeters: Long,
            distanceUnit: DistanceUnit,
        ): PlainDistance =
            PlainDistance(
                amount = millimeters / distanceUnit.millimetersPerUnit.toDouble(),
                unit = distanceUnit,
            )
    }
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
