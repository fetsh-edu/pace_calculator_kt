package me.fetsh.pacecalculator.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode

const val DOUBLE_ASSERTION_DELTA = 0.00001

class PaceTest {
    @Test
    fun `creates pace from time and distance`() {
        val time = Time(BigDecimal(300)) // 5 minutes = 300,000 ms
        val distance = Distance.of(1.0, DistanceUnit.Kilometer)

        val pace = Pace.of(time, distance)

        val expectedSecondsPerKilometer =
            BigDecimal(300)

        assertEquals(0, expectedSecondsPerKilometer.compareTo(pace.secondsPerKilometer))
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
            BigDecimal(270)
                .setScale(3, RoundingMode.HALF_UP)

        assertEquals(expected, pace.secondsPerKilometer)
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

        val timeSeconds = BigDecimal(6 * 60)
        val distanceMeters = Distance.of(1.0, DistanceUnit.Mile).meters
        val distanceKilometers = distanceMeters.divide(BigDecimal(1_000), 12, RoundingMode.HALF_UP)
        val expectedSecondsPerKilometer = timeSeconds.divide(distanceKilometers, 3, RoundingMode.HALF_UP)

        assertEquals(expectedSecondsPerKilometer, pace.secondsPerKilometer)
    }

    @Test
    fun `toSpeed for 5 min per km pace`() {
        val pace = Pace(BigDecimal("300"))
        val expected = 1000.0 / 300.0
        assertThat(pace.toSpeed().metersPerSecond.toDouble()).isWithin(DOUBLE_ASSERTION_DELTA).of(expected)
    }

    @Test
    fun `toSpeed for 4 min per km pace`() {
        val pace = Pace(BigDecimal("240"))
        val expected = 1000.0 / 240.0
        assertThat(pace.toSpeed().metersPerSecond.toDouble()).isWithin(DOUBLE_ASSERTION_DELTA).of(expected)
    }

    @Test
    fun `toSpeed for 1 m per s equivalent pace`() {
        val pace = Pace(BigDecimal("1000"))
        val expected = 1.0
        assertThat(pace.toSpeed().metersPerSecond.toDouble()).isWithin(DOUBLE_ASSERTION_DELTA).of(expected)
    }

    @Test
    fun `toSpeed for 10 m per s equivalent pace`() {
        val pace = Pace(BigDecimal("100"))
        val expected = 10.0
        assertThat(pace.toSpeed().metersPerSecond.toDouble()).isWithin(DOUBLE_ASSERTION_DELTA).of(expected)
    }

    @Test
    fun `toSpeed for very slow pace of 1 hour per km`() {
        val pace = Pace(BigDecimal("3600"))
        val expected = 1000.0 / 3600.0
        assertThat(pace.toSpeed().metersPerSecond.toDouble()).isWithin(DOUBLE_ASSERTION_DELTA).of(expected)
    }

    @Test
    fun `toSpeed for extremely fast pace of 1 microsecond per km`() {
        val pace = Pace(BigDecimal("0.000001"))
        val expected = 1_000_000_000.0
        assertThat(pace.toSpeed().metersPerSecond.toDouble()).isWithin(DOUBLE_ASSERTION_DELTA).of(expected)
    }
}
