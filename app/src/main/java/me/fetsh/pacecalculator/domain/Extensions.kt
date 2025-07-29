package me.fetsh.pacecalculator.domain

import java.math.BigDecimal

fun BigDecimal.longValueExactSafe(): Long =
    try {
        this.longValueExact()
    } catch (e: ArithmeticException) {
        throw ArithmeticException("Value $this does not fit in Long")
    }
