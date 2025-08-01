package me.fetsh.pacecalculator.domain

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import me.fetsh.pacecalculator.utils.UnitConversions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.to

class DistanceTest {
    @Test
    fun `constructor rejects negative millimeters`() {
        assertFailsWith<IllegalArgumentException> {
            Distance(BigDecimal("-1"))
        }
    }

    @Test
    fun `of creates correct millimeters for kilometers`() {
        val d = Distance.of(1.0, DistanceUnit.Kilometer)
        assertThat(d).isEqualTo(Distance(BigDecimal("1000")))
    }

    @Test
    fun `of creates correct millimeters for meters`() {
        val d = Distance.of(123.0, DistanceUnit.Meter)
        assertThat(d).isEqualTo(Distance(BigDecimal("123")))
    }

    @Test
    fun `of creates correct millimeters for miles`() {
        val d = Distance.of(1.0, DistanceUnit.Mile)
        assertThat(d).isEqualTo(Distance(BigDecimal("1609.344")))
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
            Truth.assertThat(back).isEqualTo(BigDecimal.valueOf(amount))
        }
    }

    @Test
    fun `to converts correctly from millimeters to units`() {
        val d = Distance(BigDecimal("42195")) // marathon mm
        assertThat(d.to(DistanceUnit.Kilometer)).isEqualTo(BigDecimal("42.195"))
        assertThat(d.to(DistanceUnit.Meter)).isEqualTo(BigDecimal("42195"))
        assertThat(d.to(DistanceUnit.Mile)).isEqualTo(BigDecimal("26.218757456454306").round(UnitConversions.CALCULATION_PRECISION))
    }

    @Test
    fun `calculate distance with simple time and pace`() {
        val time = Time.fromMinutes(10) // 10 minutes
        val pace = Pace.of(5, 0, 0, PaceUnit.PerKilometre) // 5:00/km
        val expectedDistance = Distance.of(2.0, DistanceUnit.Kilometer) // 2 km

        val actualDistance = Distance.of(time, pace)
        assertEquals(expectedDistance, actualDistance)
    }

    @Test
    fun `calculate distance testing intermediate kilometer precision (scale 9)`() {
        val time = Time(BigDecimal("0.001"))
        val pace = Pace(BigDecimal("0.000003"))
        val expectedDistance = Distance(BigDecimal("333333.333333"))

        val actualDistance = Distance.of(time, pace)
        assertEquals(expectedDistance, actualDistance)
    }

    @Test
    fun `calculate distance with very large time`() {
        val time = Time(seconds = BigDecimal("1000000"))
        val pace = Pace(BigDecimal(1))
        val expectedDistance = Distance(BigDecimal("1000000000"))

        val actualDistance = Distance.of(time, pace)
        assertEquals(expectedDistance, actualDistance)
    }

    @Test
    fun `specific scenario from marathon test for distance calculation`() {
        val timeForMarathon = Time(seconds = BigDecimal("10799.979")) // 2h 59m 59s 979ms
        val paceForMarathon = Pace(BigDecimal("255.954")) // 4:15.954 /km
        val expectedDistance = NamedDistance.Marathon.distance // Marathon distance

        val actualDistance = Distance.of(timeForMarathon, paceForMarathon)
        val delta = expectedDistance.meters.subtract(actualDistance.meters).abs()
        assertTrue(delta < BigDecimal("0.001"))
    }
}
