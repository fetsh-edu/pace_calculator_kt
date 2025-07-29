package me.fetsh.pacecalculator.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode

const val DOUBLE_ASSERTION_DELTA = 0.00001

class PaceTest {
    @Test
    fun `creates pace from time and distance`() {
        val time = Time(300_000) // 5 minutes = 300,000 ms
        val distance = Distance.of(1.0, DistanceUnit.Kilometer)

        val pace = Pace.of(time, distance)

        // 5 min = 300_000 ms = 300_000_000 µs
        // 1 km → expected µs/km
        val expectedMicrosPerKilometer =
            BigDecimal(300_000_000)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()

        assertEquals(expectedMicrosPerKilometer, pace.microsecondsPerKilometer)
    }

    @Test
    fun `throws on zero distance`() {
        val time = Time(1_000)
        val distance = Distance.of(0.0, DistanceUnit.Meter)

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                Pace.of(time, distance)
            }

        assertEquals("Distance must be positive", exception.message)
    }

    @Test
    fun `creates pace from time parts and kilometers`() {
        val pace =
            Pace.of(
                minutes = 4,
                seconds = 30,
                fractional = 0,
                precision = TimePrecision.Seconds,
                unit = PaceUnit.PerKilometre,
            )

        // 4:30 = 270_000 ms = 270_000_000 µs → per km
        val expected =
            BigDecimal(270_000_000)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()

        assertEquals(expected, pace.microsecondsPerKilometer)
    }

    @Test
    fun `creates pace from time parts and miles`() {
        val pace =
            Pace.of(
                minutes = 6,
                seconds = 0,
                fractional = 0,
                precision = TimePrecision.Seconds,
                unit = PaceUnit.PerMile,
            )

        val timeMicros = BigDecimal(6 * 60 * 1_000_000L) // 6 minutes in µs
        val distanceMillimeters = BigDecimal(Distance.of(1.0, DistanceUnit.Mile).millimeters)
        val distanceKilometers = distanceMillimeters.divide(BigDecimal(1_000_000), 9, RoundingMode.HALF_UP)
        val expectedMicrosPerKilometer = timeMicros.divide(distanceKilometers, 0, RoundingMode.HALF_UP).longValueExact()

        assertEquals(expectedMicrosPerKilometer, pace.microsecondsPerKilometer)
    }

    @Test
    fun `toSpeed for 5 min per km pace`() {
        // 5 min/km = 300 seconds/km
        // microsecondsPerKilometer = 300 s/km * 1,000,000 µs/s = 300,000,000 µs/km
        val pace = Pace(300_000_000L)
        // Speed = 1000m / 300s = 10/3 m/s = 3.33333... m/s
        val expectedSpeed = Speed(1000.0 / 300.0)
        kotlin.test.assertEquals(expectedSpeed.metersPerSecond, pace.toSpeed().metersPerSecond, DOUBLE_ASSERTION_DELTA)
    }

    @Test
    fun `toSpeed for 4 min per km pace`() {
        // 4 min/km = 240 seconds/km
        // microsecondsPerKilometer = 240 s/km * 1,000,000 µs/s = 240,000,000 µs/km
        val pace = Pace(240_000_000L)
        // Speed = 1000m / 240s = 100/24 m/s = 25/6 m/s = 4.16666... m/s
        val expectedSpeed = Speed(1000.0 / 240.0)
        kotlin.test.assertEquals(expectedSpeed.metersPerSecond, pace.toSpeed().metersPerSecond, DOUBLE_ASSERTION_DELTA)
    }

    @Test
    fun `toSpeed for pace equivalent to 1 m per s`() {
        // 1 m/s speed means 1000m in 1000s (for km pace)
        // Pace = 1000 seconds / 1 kilometer
        // microsecondsPerKilometer = 1000 s/km * 1,000,000 µs/s = 1,000,000,000 µs/km
        val pace = Pace(1_000_000_000L)
        val expectedSpeed = Speed(1.0)
        kotlin.test.assertEquals(expectedSpeed.metersPerSecond, pace.toSpeed().metersPerSecond, DOUBLE_ASSERTION_DELTA)
    }

    @Test
    fun `toSpeed for pace equivalent to 10 m per s (very fast pace)`() {
        // 10 m/s speed means 1000m in 100s
        // Pace = 100 seconds / 1 kilometer
        // microsecondsPerKilometer = 100 s/km * 1,000,000 µs/s = 100,000,000 µs/km
        val pace = Pace(100_000_000L)
        val expectedSpeed = Speed(10.0)
        kotlin.test.assertEquals(expectedSpeed.metersPerSecond, pace.toSpeed().metersPerSecond, DOUBLE_ASSERTION_DELTA)
    }

    @Test
    fun `toSpeed for very slow pace (e g , 1 hour per km)`() {
        // 1 hour/km = 3600 seconds/km
        // microsecondsPerKilometer = 3600 s/km * 1,000,000 µs/s = 3,600,000,000 µs/km
        val pace = Pace(3_600_000_000L)
        // Speed = 1000m / 3600s = 10/36 m/s = 5/18 m/s = 0.2777... m/s
        val expectedSpeed = Speed(1000.0 / 3600.0)
        kotlin.test.assertEquals(expectedSpeed.metersPerSecond, pace.toSpeed().metersPerSecond, DOUBLE_ASSERTION_DELTA)
    }

    @Test
    fun `toSpeed for smallest non-zero pace (1 microsecondPerKilometer)`() {
        // This is an extremely fast pace, almost infinite speed.
        // 1 µs/km = 0.000001 s/km
        // Speed = 1000m / 0.000001s = 1,000,000,000 m/s (very high)
        val pace = Pace(1L)
        val expectedSpeed = Speed(1_000_000_000.0) // 1000m / (1 µs / 1,000,000 µs/s)
        kotlin.test.assertEquals(expectedSpeed.metersPerSecond, pace.toSpeed().metersPerSecond, DOUBLE_ASSERTION_DELTA)
    }
}
