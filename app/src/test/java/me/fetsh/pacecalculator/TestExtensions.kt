package me.fetsh.pacecalculator

import java.math.BigDecimal

fun BigDecimal.normalizedEquals(other: BigDecimal): Boolean {
    val maxScale = maxOf(this.scale(), other.scale())
    return this.setScale(maxScale) == other.setScale(maxScale)
}
