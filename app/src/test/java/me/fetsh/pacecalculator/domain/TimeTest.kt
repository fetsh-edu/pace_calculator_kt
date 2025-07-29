package me.fetsh.pacecalculator.domain

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class TimeTest {
    /* ---------------------------------------------------------------- *
     *  Constructors / factory methods                                  *
     * ---------------------------------------------------------------- */

    @Test
    fun `fromSeconds multiplies by 1000`() {
        Assertions.assertEquals(3_000L, Time.fromSeconds(3).milliseconds)
    }

    @Test
    fun `fromMinutes multiplies by 60_000`() {
        Assertions.assertEquals(120_000L, Time.fromMinutes(2).milliseconds)
    }

    /* ---------------------------------------------------------------- *
     *  Validation                                                      *
     * ---------------------------------------------------------------- */

    @Test
    fun `negative time is rejected`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) { Time(-1) }
    }

    @Test
    fun `fraction out of range is rejected`() {
        // 10 deciseconds would equal a full second – invalid
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            TimeParts.of(0, 0, 1, 10, TimePrecision.Deciseconds)
        }
    }

    /* ---------------------------------------------------------------- *
     *  Rounding                                                        *
     * ---------------------------------------------------------------- */

    @Test
    fun `roundTo deciseconds rounds down when below half`() {
        val rounded = Time(1_234).roundTo(TimePrecision.Deciseconds) // 1.234 s
        Assertions.assertEquals(1_200L, rounded.milliseconds) // 1.2 s
    }

    @Test
    fun `roundTo deciseconds rounds up on HALF_UP tie`() {
        val rounded = Time(1_250).roundTo(TimePrecision.Deciseconds) // 1.250 s
        Assertions.assertEquals(1_300L, rounded.milliseconds) // 1.3 s
    }

    /* ---------------------------------------------------------------- *
     *  Arithmetic                                                      *
     * ---------------------------------------------------------------- */

    @Test
    fun `multiply by positive factor`() {
        val t = Time.fromSeconds(2) // 2 s
        val result =
            t.multiply(
                BigDecimal("1.5"),
            ) // 3 s
        Assertions.assertEquals(3_000L, result.milliseconds)
    }

    @Test
    fun `multiply rejects negative factor`() {
        val t = Time.fromSeconds(1)
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            t.multiply(BigDecimal("-1"))
        }
    }

    @Test
    fun `divide by positive factor`() {
        val t = Time.fromMinutes(3) // 180 s
        val result =
            t.divide(
                BigDecimal("2"),
            ) // 90 s
        Assertions.assertEquals(90_000L, result.milliseconds)
    }

    @Test
    fun `divide by zero throws ArithmeticException`() {
        val t = Time.fromSeconds(1)
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            t.divide(BigDecimal.ZERO)
        }
    }

    /* ---------------------------------------------------------------- *
     *  Conversion to parts                                             *
     * ---------------------------------------------------------------- */

    @Test
    fun `toParts converts back with requested precision`() {
        val original = Time(1_234) // 1 234 ms
        val parts = original.roundTo(TimePrecision.Centiseconds).toParts() // 2 frac-digits
        val expected = TimeParts.of(0, 0, 1, 23, TimePrecision.Centiseconds) // 01.23
        assertEquals(expected, parts)
    }

    @Test
    fun `round trip Time - toParts - of equals original when rounded`() {
        val t = Time(65_432) // ~65.432 s
        val precision = TimePrecision.Deciseconds
        val parts = t.roundTo(precision).toParts()
        val rebuilt = Time.of(parts)
        assertEquals(t.roundTo(precision).milliseconds, rebuilt.milliseconds)
    }
}
