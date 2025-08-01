package me.fetsh.pacecalculator.domain

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import me.fetsh.pacecalculator.normalizedEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.math.BigDecimal
import kotlin.test.Test

class DistanceExtendedTest {
    @Test
    fun `PlainDistance distance matches of(amount, unit)`() {
        val pd = PlainDistance(5.0, DistanceUnit.Kilometer)
        val expected = Distance.of(5.0, DistanceUnit.Kilometer)
        assertThat(pd.distance).isEqualTo(expected)
    }

    @Test
    fun `preset Marathon has correct distance`() {
        val m = NamedDistance.Marathon
        assertTrue(m.distance.meters.normalizedEquals(BigDecimal.valueOf(42195)))
        Truth.assertThat(m.code).isEqualTo("marathon")
    }

    @Test
    fun `preset HalfMarathon has correct distance`() {
        val hm = NamedDistance.HalfMarathon
        assertTrue(hm.distance.meters.normalizedEquals(BigDecimal("21097.5")))
        Truth.assertThat(hm.code).isEqualTo("half_marathon")
    }

    @Test
    fun `distance options list contains exactly expected items in order`() {
        assertThat(DISTANCE_OPTIONS).hasSize(5)

        // 1) 5 km
        val first = DISTANCE_OPTIONS[0]
        assertThat(first).isInstanceOf(PlainDistance::class.java)
        (first as PlainDistance).let {
            assertThat(it.amount).isEqualTo(5.0)
            assertThat(it.unit).isEqualTo(DistanceUnit.Kilometer)
        }

        // 2) 10 km
        val second = DISTANCE_OPTIONS[1] as PlainDistance
        assertThat(second.amount).isEqualTo(10.0)
        assertThat(second.unit).isEqualTo(DistanceUnit.Kilometer)

        // 3) 10 miles
        val third = DISTANCE_OPTIONS[2] as PlainDistance
        assertThat(third.amount).isEqualTo(10.0)
        assertThat(third.unit).isEqualTo(DistanceUnit.Mile)

        // 4) HalfMarathon preset
        val fourth = DISTANCE_OPTIONS[3]
        assertThat(fourth).isEqualTo(NamedDistance.HalfMarathon)

        // 5) Marathon preset
        val fifth = DISTANCE_OPTIONS[4]
        assertThat(fifth).isEqualTo(NamedDistance.Marathon)
    }

    @Test
    fun `PlainDistance and PresetDistanceId expose same Distance interface`() {
        val items: List<DistanceExtended> =
            listOf(
                PlainDistance(1.0, DistanceUnit.Kilometer),
                NamedDistance.Marathon,
            )
        val km = items[0].distance
        val marathon = items[1].distance

        assertTrue(km.meters.normalizedEquals(BigDecimal("1000")))
        assertTrue(marathon.meters.normalizedEquals(BigDecimal("42195")))
    }
}
