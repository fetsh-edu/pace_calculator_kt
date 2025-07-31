package me.fetsh.pacecalculator.core

import me.fetsh.pacecalculator.domain.DistanceExtended
import me.fetsh.pacecalculator.domain.Pace
import me.fetsh.pacecalculator.domain.Speed
import me.fetsh.pacecalculator.domain.SpeedUnit
import me.fetsh.pacecalculator.domain.Time

sealed interface Msg {
    data class DistanceChanged(
        val value: DistanceExtended,
    ) : Msg

    data class TimeChanged(
        val value: Time,
    ) : Msg

    data class PaceChanged(
        val value: Pace,
    ) : Msg

    data class SpeedChanged(
        val value: Speed,
        val speedUnit: SpeedUnit,
    ) : Msg

    data class TestEffectRequested(
        val message: String,
    ) : Msg
}
