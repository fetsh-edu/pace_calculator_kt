package me.fetsh.pacecalculator.domain

enum class PaceSolveFor { Time, Distance }

enum class TimeSolveFor { Pace, Distance }

enum class DistanceSolveFor { Pace, Time }

data class Kinematics(
    val distance: DistanceExtended,
    val pace: Pace,
    val precision: TimePrecision,
) {
    val time: Time by lazy { Time.of(pace.roundTo(precision), distance.distance) }

    val speed: Speed by lazy { pace.toSpeed() }

    companion object {
        val INIT =
            of(
                NamedDistance.Marathon,
                Time.of(TimeParts(3, 0, 0, 0)),
                TimePrecision.Seconds,
            )

        /**
         * Creates a [Kinematics] object from a known [distance], [time], and [precision].
         * The [pace] is computed from [distance] and [time].
         */
        fun of(
            distance: DistanceExtended,
            time: Time,
            precision: TimePrecision,
        ): Kinematics {
            val pace = Pace.of(time, distance.distance)
            return Kinematics(distance = distance, pace = pace, precision = precision)
        }
    }

    fun withPace(
        pace: Pace,
        solveFor: PaceSolveFor,
    ): Kinematics =
        when (solveFor) {
            PaceSolveFor.Time -> copy(pace = pace)
            PaceSolveFor.Distance -> copy(distance = NakedDistance(Distance.of(time, pace)), pace = pace)
        }

    fun withTime(
        time: Time,
        solveFor: TimeSolveFor,
    ): Kinematics =
        when (solveFor) {
            TimeSolveFor.Pace -> copy(pace = Pace.of(time, distance.distance))
            TimeSolveFor.Distance -> copy(distance = NakedDistance(Distance.of(time, pace)))
        }

    fun withDistance(
        distance: DistanceExtended,
        solveFor: DistanceSolveFor,
    ): Kinematics =
        when (solveFor) {
            DistanceSolveFor.Pace -> copy(distance = distance, pace = Pace.of(time, distance.distance))
            DistanceSolveFor.Time -> copy(distance = distance)
        }

    fun withPrecision(precision: TimePrecision): Kinematics = copy(precision = precision)

    fun withSpeed(
        speed: Speed,
        solveFor: PaceSolveFor,
    ): Kinematics = withPace(Pace.of(speed), solveFor)
}
