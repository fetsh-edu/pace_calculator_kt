package me.fetsh.pacecalculator.domain

import java.math.BigDecimal

interface BigDecimalBased {
    fun getValue(): BigDecimal

    fun isSameValueAs(other: Any?): Boolean =
        other is BigDecimalBased &&
            this::class == other::class &&
            this.getValue().compareTo(other.getValue()) == 0

    fun normalizedHash(): Int = getValue().stripTrailingZeros().hashCode()
}
