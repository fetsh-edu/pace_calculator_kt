package me.fetsh.pacecalculator.domain

enum class SpeedUnit {
    KmpH,
    MpH,
}

@JvmInline
value class Speed(
    val metersPerSecond: Double,
) {
    init {
        require(metersPerSecond > 0) { "Speed must be non-negative or zero, got: $metersPerSecond" }
    }

    val kilometersPerHour: Double
        get() = metersPerSecond * METERS_PER_SECOND_TO_KILOMETERS_PER_HOUR

    val milesPerHour: Double
        get() = metersPerSecond * METERS_PER_SECOND_TO_MILES_PER_HOUR

    override fun toString(): String = "%.2f m/s".format(metersPerSecond)

    companion object {
        private const val METERS_PER_SECOND_TO_KILOMETERS_PER_HOUR = 3.6
        private const val METERS_PER_SECOND_TO_MILES_PER_HOUR = 2.23693629205
        private const val KILOMETERS_PER_HOUR_TO_METERS_PER_SECOND = 1 / METERS_PER_SECOND_TO_KILOMETERS_PER_HOUR
        private const val MILES_PER_HOUR_TO_METERS_PER_SECOND = 1 / METERS_PER_SECOND_TO_MILES_PER_HOUR

        fun fromKilometersPerHour(kilometersPerHour: Double): Speed = Speed(kilometersPerHour * KILOMETERS_PER_HOUR_TO_METERS_PER_SECOND)

        fun fromMilesPerHour(milesPerHour: Double): Speed = Speed(milesPerHour * MILES_PER_HOUR_TO_METERS_PER_SECOND)
    }
}
