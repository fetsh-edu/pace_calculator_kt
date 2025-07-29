package me.fetsh.pacecalculator.core

import me.fetsh.pacecalculator.domain.DistanceExtended
import me.fetsh.pacecalculator.domain.NamedDistance
import me.fetsh.pacecalculator.domain.Pace
import me.fetsh.pacecalculator.domain.Time

data class Model(
    val distanceExtended: DistanceExtended = NamedDistance.Marathon,
    val time: Time = Time.fromMinutes(180),
    val pace: Pace = Pace.of(time, distanceExtended.distance),
)
