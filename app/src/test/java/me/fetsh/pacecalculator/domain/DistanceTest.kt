package me.fetsh.pacecalculator.domain

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.to

class DistanceTest {
    private val eps = 1e-9

    @Test
    fun `constructor rejects negative millimeters`() {
        assertFailsWith<IllegalArgumentException> {
            Distance(-1)
        }
    }

    @Test
    fun `of creates correct millimeters for kilometers`() {
        val d = Distance.of(1.0, DistanceUnit.Kilometer)
        assertThat(d.millimeters).isEqualTo(1_000_000L)
    }

    @Test
    fun `of creates correct millimeters for meters`() {
        val d = Distance.of(123.0, DistanceUnit.Meter)
        assertThat(d.millimeters).isEqualTo(123_000L)
    }

    @Test
    fun `of creates correct millimeters for miles`() {
        val d = Distance.of(1.0, DistanceUnit.Mile)
        assertThat(d.millimeters).isEqualTo(1_609_344L)
    }

    @Test
    fun `round trip of - to is stable within epsilon`() {
        val samples =
            listOf(
                0.0 to DistanceUnit.Kilometer,
                5.0 to DistanceUnit.Kilometer,
                10.0 to DistanceUnit.Mile,
                42195.0 to DistanceUnit.Meter,
                0.12345 to DistanceUnit.Kilometer,
            )

        for ((amount, unit) in samples) {
            val d = Distance.of(amount, unit)
            val back = d.to(unit)
            Truth.assertThat(back).isWithin(eps).of(amount)
        }
    }

    @Test
    fun `to converts correctly from millimeters to units`() {
        val d = Distance(42_195_000) // marathon mm
        assertThat(d.to(DistanceUnit.Kilometer)).isWithin(eps).of(42.195)
        assertThat(d.to(DistanceUnit.Meter)).isWithin(eps).of(42_195.0)
        assertThat(d.to(DistanceUnit.Mile)).isWithin(eps).of(26.218757456454306)
    }

    @Test
    fun `calculate distance with simple time and pace`() {
        // Pace: 5:00/km = 5 * 60 * 1000 * 1000 = 300,000,000 µs/km
        // Time: 10 minutes = 10 * 60 * 1000 = 600,000 ms
        // Expected distance:
        // totalMicroseconds = 600,000 * 1000 = 600,000,000 µs
        // kilometers = 600,000,000 / 300,000,000 = 2 km
        // millimeters = 2 * 1,000,000 = 2,000,000 mm
        val time = Time(milliseconds = 600_000L) // 10 minutes
        val pace = Pace(microsecondsPerKilometer = 300_000_000L) // 5:00/km
        val expectedDistance = Distance(millimeters = 2_000_000L) // 2 km

        val actualDistance = Distance.of(time, pace)
        assertEquals(expectedDistance, actualDistance)
    }

    @Test
    fun `calculate distance with zero time results in zero distance`() {
        val time = Time(milliseconds = 0L)
        val pace = Pace(microsecondsPerKilometer = 300_000_000L) // 5:00/km
        val expectedDistance = Distance(millimeters = 0L)

        val actualDistance = Distance.of(time, pace)
        assertEquals(expectedDistance, actualDistance)
    }

    @Test
    fun `calculate distance requiring rounding for millimeters (half up)`() {
        // Time: 1 ms
        // Pace: 1,000,000 µs/km (1 sec/km)
        // totalMicroseconds = 1 * 1000 = 1000 µs
        // kilometers = 1000 / 1,000,000 = 0.001 km
        // millimeters = 0.001 * 1,000,000 = 1000 mm
        // Let's pick values that will test rounding:
        // Pace: 1,999 µs/km.
        // Time: 1 ms => 1000 µs
        // kilometers = 1000 / 1999 ≈ 0.500250125...
        // millimeters = 0.500250125 * 1_000_000 = 500250.125
        // setScale(0, RoundingMode.HALF_UP) should round this to 500250 mm
        val time = Time(milliseconds = 1L)
        val pace = Pace(microsecondsPerKilometer = 1999L)
        val expectedDistance = Distance(millimeters = 500250L)

        val actualDistance = Distance.of(time, pace)
        assertEquals(expectedDistance, actualDistance, "Rounding up for millimeters")
    }

    @Test
    fun `calculate distance requiring rounding for millimeters (half down)`() {
        // Similar to above, but ensuring .4 rounds down
        // Pace: 2001 µs/km
        // Time: 1 ms => 1000 µs
        // kilometers = 1000 / 2001 ≈ 0.4997501249...
        // millimeters = 0.4997501249 * 1_000_000 = 499750.1249...
        // setScale(0, RoundingMode.HALF_UP) should round this to 499750 mm
        val time = Time(milliseconds = 1L)
        val pace = Pace(microsecondsPerKilometer = 2001L)
        val expectedDistance = Distance(millimeters = 499750L)

        val actualDistance = Distance.of(time, pace)
        assertEquals(expectedDistance, actualDistance, "Rounding down for millimeters")
    }

    @Test
    fun `calculate distance testing intermediate kilometer precision (scale 9)`() {
        // Time: 1 ms = 1000 µs
        // Pace: 3 µs/km (very fast pace)
        // kilometers = 1000 / 3 = 333.333333333... (scale 9 will capture this)
        // millimeters = 333.333333333 * 1,000,000 = 333,333,333.333
        // Rounded to 0 decimal places (HALF_UP): 333,333,333 mm
        val time = Time(milliseconds = 1L)
        val pace = Pace(microsecondsPerKilometer = 3L)
        val expectedDistance = Distance(millimeters = 333_333_333L)

        val actualDistance = Distance.of(time, pace)
        assertEquals(expectedDistance, actualDistance)
    }

    @Test
    fun `calculate distance with very slow pace (large microsecondsPerKilometer)`() {
        // Time: 1,000,000 ms (1000 seconds)
        // Pace: Long.MAX_VALUE µs/km (extremely slow)
        // totalMicroseconds = 1,000,000 * 1000 = 1,000,000,000 µs
        // kilometers = 1,000,000,000 / Long.MAX_VALUE. This will be very small.
        // millimeters = kilometers * 1,000,000
        // Since Long.MAX_VALUE is ~9e18, and numerator is 1e9, kilometers is ~1e-10
        // millimeters is ~1e-10 * 1e6 = 1e-4 = 0.0001 mm. Rounded to 0.
        val time = Time(milliseconds = 1_000_000L)
        val pace = Pace(microsecondsPerKilometer = Long.MAX_VALUE)
        val expectedDistance = Distance(millimeters = 0L) // Expect it to round to zero

        val actualDistance = Distance.of(time, pace)
        assertEquals(expectedDistance, actualDistance)
    }

    @Test
    fun `calculate distance with very large time`() {
        // Time: 1_000_000_000 ms (1 million seconds)
        // Pace: 1_000_000 µs/km (1 s/km)
        // totalMicroseconds = 1_000_000_000 * 1000 = 1_000_000_000_000 µs
        // kilometers = 1_000_000_000_000 / 1_000_000 = 1_000_000 km
        // millimeters = 1_000_000 * 1_000_000 = 1_000_000_000_000 mm
        val time = Time(milliseconds = 1_000_000_000L)
        val pace = Pace(microsecondsPerKilometer = 1_000_000L)
        val expectedDistance = Distance(millimeters = 1_000_000_000_000L)

        val actualDistance = Distance.of(time, pace)
        assertEquals(expectedDistance, actualDistance)
    }

    @Test
    fun `specific scenario from marathon test for distance calculation`() {
        // Pace: 4:15.954 /km.
        // This means 255954 ms/km.
        // Or 255_954_000 µs/km (if pace.microsecondsPerKilometer is actually µs/mm, this needs adjustment)
        // ASSUMING Pace is µs/km as per its name in the function.
        // Pace_µs_per_km = (4 * 60 * 1000 + 15 * 1000 + 954) * 1000L = 255954000L
        // Time for marathon (target): 2h 59m 59s 979ms
        // totalTimeMs = (2*3600 + 59*60 + 59)*1000 + 979 = (7200 + 3540 + 59)*1000 + 979
        //             = (10799)*1000 + 979 = 10799000 + 979 = 10799979 ms
        val timeForMarathon = Time(milliseconds = 10799979L)
        val paceForMarathon = Pace(microsecondsPerKilometer = 255954000L) // 4:15.954 /km

        // Expected distance is 42.195 km = 42,195,000 mm
        // Calculation:
        // totalMicroseconds = 10799979 * 1000 = 10799979000
        // kilometersBD = 10799979000 / 255954000 = 42.195000000 (with scale 9)
        // millimeters = 42.195000000 * 1_000_000 = 42195000.000...
        // Rounded: 42,195,000 mm
        val expectedDistance = Distance(millimeters = 42_195_000L) // Marathon distance

        val actualDistance = Distance.of(timeForMarathon, paceForMarathon)
        assertEquals(expectedDistance, actualDistance)
    }
}
