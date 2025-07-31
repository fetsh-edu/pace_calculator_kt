package me.fetsh.pacecalculator.core

sealed interface Effect

sealed interface UiEffect : Effect

sealed interface AppEffect : Effect

data class ShowToastEffect(
    val message: String,
) : UiEffect

data class DelayEffect(
    val message: String,
    val delayMillis: Long,
) : AppEffect
