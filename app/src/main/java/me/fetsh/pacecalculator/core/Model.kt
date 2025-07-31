package me.fetsh.pacecalculator.core

import me.fetsh.pacecalculator.domain.Kinematics
import me.fetsh.pacecalculator.domain.SpeedUnit

data class Model(
    val kinematics: Kinematics = Kinematics.INIT,
    val speedUnit: SpeedUnit = SpeedUnit.KmpH,
)
