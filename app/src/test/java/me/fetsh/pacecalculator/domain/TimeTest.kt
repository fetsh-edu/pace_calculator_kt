package me.fetsh.pacecalculator.domain

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal

private const val DOUBLE_DELTA = 1e-6

class TimeTest {
    /* ---------------------------------------------------------------- *
     *  Constructors / factory methods                                  *
     * ---------------------------------------------------------------- */

    @Test
    fun `fromSeconds multiplies correctly`() {
        val result = Time.fromSeconds(BigDecimal(3))
        assertThat(result.seconds.toDouble()).isWithin(DOUBLE_DELTA).of(3.0)
    }

    @Test
    fun `fromMinutes multiplies correctly`() {
        val result = Time.fromMinutes(2)
        assertThat(result.seconds.toDouble()).isWithin(DOUBLE_DELTA).of(120.0)
    }

    /* ---------------------------------------------------------------- *
     *  Validation                                                      *
     * ---------------------------------------------------------------- */

    @Test
    fun `negative time is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            Time(BigDecimal("-1"))
        }
    }

    @Test
    fun `fraction out of range is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            TimeParts.of(0, 0, 1, 10, TimePrecision.Deciseconds) // deci max = 9
        }
    }

    /* ---------------------------------------------------------------- *
     *  Rounding                                                        *
     * ---------------------------------------------------------------- */

    @Test
    fun `roundTo deciseconds rounds down when below half`() {
        val original = Time(BigDecimal("1.234"))
        val rounded = original.roundTo(TimePrecision.Deciseconds)
        assertThat(rounded.seconds.toDouble()).isWithin(DOUBLE_DELTA).of(1.2)
    }

    @Test
    fun `roundTo deciseconds rounds up on HALF_UP tie`() {
        val original = Time(BigDecimal("1.250"))
        val rounded = original.roundTo(TimePrecision.Deciseconds)
        assertThat(rounded.seconds.toDouble()).isWithin(DOUBLE_DELTA).of(1.3)
    }

    /* ---------------------------------------------------------------- *
     *  Arithmetic                                                      *
     * ---------------------------------------------------------------- */

    @Test
    fun `multiply by positive factor`() {
        val original = Time.fromSeconds(2)
        val result = original.multiply(BigDecimal("1.5"))
        assertThat(result.seconds.toDouble()).isWithin(DOUBLE_DELTA).of(3.0)
    }

    @Test
    fun `divide by positive factor`() {
        val original = Time.fromMinutes(3) // 180 s
        val result = original.divide(BigDecimal("2"))
        assertThat(result.seconds.toDouble()).isWithin(DOUBLE_DELTA).of(90.0)
    }

    @Test
    fun `divide by zero throws`() {
        val original = Time.fromSeconds(1)
        assertThrows(IllegalArgumentException::class.java) {
            original.divide(BigDecimal.ZERO)
        }
    }

    /* ---------------------------------------------------------------- *
     *  Conversion to parts                                             *
     * ---------------------------------------------------------------- */

    @Test
    fun `toParts converts back with requested precision`() {
        val original = Time(BigDecimal("1.234"))
        val rounded = original.roundTo(TimePrecision.Centiseconds)
        val parts = rounded.toParts()
        val expected = TimeParts.of(0, 0, 1, 23, TimePrecision.Centiseconds)
        assertEquals(expected, parts)
    }

    @Test
    fun `round trip Time - toParts - of equals original when rounded`() {
        val original = Time(BigDecimal("65.432"))
        val precision = TimePrecision.Deciseconds
        val rounded = original.roundTo(precision)
        val parts = rounded.toParts()
        val rebuilt = Time.of(parts)
        assertThat(rebuilt.seconds.toDouble()).isWithin(DOUBLE_DELTA).of(rounded.seconds.toDouble())
    }
}
