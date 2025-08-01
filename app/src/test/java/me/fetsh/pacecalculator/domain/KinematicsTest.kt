package me.fetsh.pacecalculator.domain

import me.fetsh.pacecalculator.normalizedEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class KinematicsTest {
    @Test
    fun `marathon at 4_min_15_954 pace with precision in seconds gives 3_00_02`() {
        val distance = NamedDistance.Marathon
        val pace =
            Pace.of(
                minutes = 4,
                seconds = 15,
                fractional = 954,
                precision = TimePrecision.Milliseconds,
                unit = PaceUnit.PerKilometre,
            )
        val kinematics =
            Kinematics(
                distance = distance,
                pace = pace,
                precision = TimePrecision.Seconds,
            )

        val parts = kinematics.time.roundTo(kinematics.precision).toParts()
        assertEquals(3, parts.hours)
        assertEquals(0, parts.minutes)
        assertEquals(2, parts.seconds)
        assertEquals(0, parts.milliseconds)
    }

    @Test
    fun `marathon at 4_min_15_954 pace with precision in milliseconds gives 2_59_59_979`() {
        val distance = NamedDistance.Marathon
        val pace =
            Pace.of(
                minutes = 4,
                seconds = 15,
                fractional = 954,
                precision = TimePrecision.Milliseconds,
                unit = PaceUnit.PerKilometre,
            )
        val kinematics =
            Kinematics(
                distance = distance,
                pace = pace,
                precision = TimePrecision.Milliseconds,
            )

        val parts = kinematics.time.roundTo(kinematics.precision).toParts()
        assertEquals(2, parts.hours)
        assertEquals(59, parts.minutes)
        assertEquals(59, parts.seconds)
        assertEquals(979, parts.milliseconds)
    }

    @Test
    fun `marathon for 3 hours with precision seconds gives 3_00_02 as time and 4_16 as pace`() {
        val distance = NamedDistance.Marathon
        val time = Time.of(TimeParts(3, 0, 0, 0))
        val pace =
            Pace.of(
                minutes = 4,
                seconds = 15,
                fractional = 954,
                precision = TimePrecision.Milliseconds,
                unit = PaceUnit.PerKilometre,
            )
        val kinematics = Kinematics.of(distance, time, TimePrecision.Seconds)
        val expectedTime = Time.of(TimeParts(3, 0, 2, 0))
        assertEquals(0, expectedTime.seconds.compareTo(kinematics.time.roundTo(kinematics.precision).seconds))
        assertEquals(pace, kinematics.pace.roundTo(TimePrecision.Milliseconds))
    }

    @Test
    fun `changing kinematics pace with Time solver changes time`() {
        val kinematics = Kinematics.INIT
        val newPace = Pace.of(5, 0, 0, TimePrecision.Seconds, PaceUnit.PerKilometre)
        val newKinematics = kinematics.withPace(newPace, PaceSolveFor.Time)
        val expectedTime = Time.of(TimeParts(3, 30, 59, 0))
        assertEquals(0, expectedTime.seconds.compareTo(newKinematics.time.roundTo(kinematics.precision).seconds))
    }

    @Test
    fun `changing kinematics pace with Distance solver changes distance`() {
        val kinematics = Kinematics.INIT

        val newPace =
            Pace.of(
                Time.of(TimeParts.of(hours = 0, minutes = 5, seconds = 0, fraction = 0, precision = kinematics.precision)),
                Distance.of(1.0, DistanceUnit.Kilometer),
            )

        val newKinematics = kinematics.withPace(newPace, PaceSolveFor.Distance)

        val expectedDistance = Distance.of(kinematics.time, newPace)

        assertEquals(newPace, newKinematics.pace)
        assertEquals(0, kinematics.time.seconds.compareTo(newKinematics.time.seconds), "Time should remain unchanged")
        assertEquals(
            expectedDistance.meters,
            newKinematics.distance.distance.meters,
            "Expected recomputed distance in millimeters to match",
        )
    }

    @Test
    fun `withTime when solving for Pace updates time and recalculates pace, distance remains`() {
        val initialKinematics = Kinematics.INIT // Marathon, 3h, TimePrecision.Seconds
        val marathonDistance = NamedDistance.Marathon

        val newTime = Time(BigDecimal(4 * 60 * 60)) // Example: Change time to 4 hours

        val updatedKinematics = initialKinematics.withTime(newTime, TimeSolveFor.Pace)
        // Assertions
        val idealPaceFromIntendedTime = Pace.of(newTime, marathonDistance.distance)
        val expectedRoundedPace = idealPaceFromIntendedTime.roundTo(initialKinematics.precision)
        assertEquals(
            expectedRoundedPace,
            updatedKinematics.pace.roundTo(initialKinematics.precision),
            "Pace should be calculated based on intended time and then rounded",
        )

        // Assertion 3: The time in the updated Kinematics should be the result of using this expectedRoundedPace
        val expectedActualTime = Time.of(expectedRoundedPace, marathonDistance.distance)
        assertEquals(
            expectedActualTime.seconds,
            updatedKinematics.time.seconds,
            "Time should be recalculated based on the rounded pace and original distance",
        )
        assertEquals(
            TimeParts(hours = 3, minutes = 59, seconds = 48, milliseconds = 0),
            updatedKinematics.time.roundTo(updatedKinematics.precision).toParts(),
            "Time parts should match the debugged output",
        )
    }

    @Test
    fun `withTime when solving for Distance updates time and recalculates distance, pace remains`() {
        val initialKinematics = Kinematics.INIT // Marathon, 3h, TimePrecision.Seconds
        val initialPace = initialKinematics.pace // Pace of 3h over Marathon, rounded to seconds precision
        val newTime = Time(BigDecimal(2 * 60 * 60)) // Example: Change time to 2 hours
        val updatedKinematics = initialKinematics.withTime(newTime, TimeSolveFor.Distance)
        val expectedNewDistance = Distance.of(newTime, initialPace)

        assertEquals(
            Time.of(initialPace.roundTo(initialKinematics.precision), expectedNewDistance),
            updatedKinematics.time,
            "Time should be updated",
        )
        // Pace remains the same as initialKinematics.pace
        assertEquals(initialPace, updatedKinematics.pace, "Pace should remain unchanged")

        // Expected new distance: Distance covered in 2 hours at the "3h/Marathon" pace
        assertEquals(expectedNewDistance.meters, updatedKinematics.distance.distance.meters, "Distance should be recalculated")
    }

    @Test
    fun `withDistance when solving for Pace updates distance and recalculates pace, time remains`() {
        val initialKinematics = Kinematics.INIT // Marathon, 3h, TimePrecision.Seconds
        val newDistance = NamedDistance.HalfMarathon
        val updatedKinematics = initialKinematics.withDistance(newDistance, DistanceSolveFor.Pace)
        val expectedNewPace = Pace.of(initialKinematics.time, newDistance.distance)
        assertEquals(expectedNewPace, updatedKinematics.pace, "Pace should be recalculated")
        assertTrue(initialKinematics.time.seconds.normalizedEquals(updatedKinematics.time.seconds), "Time should remain unchanged")
        assertEquals(newDistance, updatedKinematics.distance, "Distance should be updated")
        assertTrue(
            Pace
                .of(
                    8,
                    32,
                    0,
                    TimePrecision.Seconds,
                    PaceUnit.PerKilometre,
                ).secondsPerKilometer
                .normalizedEquals(updatedKinematics.pace.roundTo(TimePrecision.Seconds).secondsPerKilometer),
        )
    }

    @Test
    fun `withDistance when solving for Time updates distance and recalculates time, pace remains`() {
        val initialKinematics = Kinematics.INIT // Marathon, 3h, TimePrecision.Seconds
        val newDistance = NamedDistance.HalfMarathon
        val expectedNewTime = Time.of(initialKinematics.pace.roundTo(initialKinematics.precision), newDistance.distance)
        val updatedKinematics = initialKinematics.withDistance(newDistance, DistanceSolveFor.Time)
        assertEquals(newDistance, updatedKinematics.distance, "Distance should be updated")
        assertEquals(initialKinematics.pace, updatedKinematics.pace, "Pace should remain unchanged")
        assertEquals(expectedNewTime, updatedKinematics.time, "Time should be recalculated")
    }

    @Test
    fun `withPrecision updates precision`() {
        val initialKinematics = Kinematics.INIT // Marathon, 3h, TimePrecision.Seconds
        val updatedKinematics = initialKinematics.withPrecision(TimePrecision.Centiseconds)
        val expectedNewTime = Time.of(initialKinematics.pace.roundTo(TimePrecision.Centiseconds), initialKinematics.distance.distance)
        assertEquals(TimePrecision.Centiseconds, updatedKinematics.precision, "Precision should be updated")
        assertEquals(expectedNewTime, updatedKinematics.time, "Time should be recalculated")
    }

    @Test
    fun `changing speed with Time solver changes time and pace, distance remains`() {
        val initialKinematics = Kinematics.INIT // Marathon, 3h, TimePrecision.Seconds
        val newSpeed = Speed.fromKilometersPerHour(10.0)
        val updatedKinematics = initialKinematics.withSpeed(newSpeed, PaceSolveFor.Time)

        val expectedNewPace = Pace.of(6, 0, 0, TimePrecision.Seconds, PaceUnit.PerKilometre)
        assertEquals(
            expectedNewPace,
            updatedKinematics.pace.roundTo(initialKinematics.precision),
            "Pace should be recalculated",
        )

        val expectedNewTime = Time.of(updatedKinematics.pace.roundTo(initialKinematics.precision), initialKinematics.distance.distance)
        assertEquals(expectedNewTime, updatedKinematics.time, "Time should be recalculated")

        assertEquals(newSpeed, updatedKinematics.speed, "Speed should be updated")
    }
}
