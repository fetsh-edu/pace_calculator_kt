package me.fetsh.pacecalculator.domain

import java.math.BigDecimal
import java.math.RoundingMode

private const val MICROSECONDS_IN_SECOND: Long = 1_000_000
private const val MICROSECONDS_IN_DECISECOND: Long = 100_000
private const val MICROSECONDS_IN_CENTISECOND: Long = 10_000
private const val MICROSECONDS_IN_MILLISECOND: Long = 1_000
const val MILLIMETERS_IN_KILOMETER: Long = 1_000_000

enum class PaceUnit(
    val distance: DistanceUnit,
) {
    PerKilometre(DistanceUnit.Kilometer),
    PerMile(DistanceUnit.Mile),
}

@JvmInline
value class Pace(
    val microsecondsPerKilometer: Long,
) {
    init {
        require(microsecondsPerKilometer > 0) { "Pace cannot be negative or zero" }
    }

    companion object {
        /**
         * Creates a [Pace] from given [time] and [distance].
         */
        fun of(
            time: Time,
            distance: Distance,
        ): Pace {
            require(distance.millimeters > 0) { "Distance must be positive" }
            val totalMicroseconds = time.milliseconds * MICROSECONDS_IN_MILLISECOND
            val microsecondsPerKilometer =
                BigDecimal(totalMicroseconds)
                    .multiply(BigDecimal(MILLIMETERS_IN_KILOMETER))
                    .divide(BigDecimal(distance.millimeters), 0, RoundingMode.HALF_UP)
                    .longValueExactSafe()
            return Pace(microsecondsPerKilometer)
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

        fun of(speed: Speed): Pace {
            if (speed.metersPerSecond <= 0) {
                return Pace(10)
            }

            return Pace(
                BigDecimal(1_000_000_000)
                    .divide(BigDecimal(speed.metersPerSecond), 0, RoundingMode.HALF_UP)
                    .longValueExactSafe(),
            )
        }
    }

    fun toSpeed(): Speed {
        val speedInMetersPerSecond = 1_000_000_000.0 / microsecondsPerKilometer
        return Speed(speedInMetersPerSecond)
    }

    /**
     * Rounds this pace to a specific [TimePrecision] using given [mode].
     */
    fun roundTo(
        precision: TimePrecision,
        mode: RoundingMode = RoundingMode.HALF_UP,
    ): Pace {
        val quantum: Long =
            when (precision) {
                TimePrecision.Seconds -> MICROSECONDS_IN_SECOND
                TimePrecision.Deciseconds -> MICROSECONDS_IN_DECISECOND
                TimePrecision.Centiseconds -> MICROSECONDS_IN_CENTISECOND
                TimePrecision.Milliseconds -> MICROSECONDS_IN_MILLISECOND
            }

        val rounded = roundToMultiple(microsecondsPerKilometer, quantum, mode)
        return Pace(rounded)
    }
}

/**
 * Rounds a [value] to the nearest multiple of [quantum] using [mode].
 */
private fun roundToMultiple(
    value: Long,
    quantum: Long,
    mode: RoundingMode,
): Long {
    require(value >= 0) { "Value must be non-negative" }
    require(quantum > 0) { "Quantum must be positive" }

    return when (mode) {
        RoundingMode.HALF_UP -> ((value + quantum / 2) / quantum) * quantum
        RoundingMode.DOWN -> (value / quantum) * quantum
        RoundingMode.UP -> if (value % quantum == 0L) value else ((value / quantum) + 1) * quantum
        else -> error("Unsupported rounding mode: $mode")
    }
}
