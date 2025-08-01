package me.fetsh.pacecalculator.utils

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object UnitConversions {
    val CALCULATION_PRECISION = MathContext(12, RoundingMode.HALF_UP)
    val TIME_CALCULATION_PRECISION = MathContext(6, RoundingMode.HALF_UP)
    val METERS_IN_KILOMETER = BigDecimal("1000")
    val METERS_IN_MILE = BigDecimal("1609.344")
    val METERS_TO_MILES: BigDecimal = BigDecimal.ONE.divide(METERS_IN_MILE, CALCULATION_PRECISION)

    val SECONDS_IN_MINUTE = BigDecimal("60")
    val SECONDS_IN_HOUR = BigDecimal("3600")
    val MILLISECONDS_IN_SECOND = BigDecimal("1000")

    val METERS_PER_SECOND_TO_KILOMETERS_PER_HOUR = BigDecimal("3.6")
    val METERS_PER_SECOND_TO_MILES_PER_HOUR = BigDecimal("2.23693629205")
    val KILOMETERS_PER_HOUR_TO_METERS_PER_SECOND =
        BigDecimal.ONE.divide(METERS_PER_SECOND_TO_KILOMETERS_PER_HOUR, CALCULATION_PRECISION)!!
    val MILES_PER_HOUR_TO_METERS_PER_SECOND =
        BigDecimal.ONE.divide(METERS_PER_SECOND_TO_MILES_PER_HOUR, CALCULATION_PRECISION)!!
}
