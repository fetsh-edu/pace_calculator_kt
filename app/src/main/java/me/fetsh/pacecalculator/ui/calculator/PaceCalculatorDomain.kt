package me.fetsh.pacecalculator.ui.calculator

data class Model(
    val distance: String = "",
    val time: String = "",
    val pace: String = "",
)

sealed interface Msg {
    data class DistanceChanged(
        val value: String,
    ) : Msg

    data class TimeChanged(
        val value: String,
    ) : Msg

    data class PaceChanged(
        val value: String,
    ) : Msg
}

sealed interface Effect
