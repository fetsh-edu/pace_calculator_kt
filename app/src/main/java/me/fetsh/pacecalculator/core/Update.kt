package me.fetsh.pacecalculator.core

import me.fetsh.pacecalculator.domain.DistanceSolveFor
import me.fetsh.pacecalculator.domain.PaceSolveFor
import me.fetsh.pacecalculator.domain.TimeSolveFor

typealias Effects = List<Effect>

val noEffects: Effects = emptyList()

/**
 * The core logic of the application. A pure function that takes the current
 * state and a message, and returns a new state and a set of side effects.
 *
 * @param msg The event that has occurred.
 * @param model The current state of the application.
 * @return A Pair containing the new state and a set of effects to be executed.
 */
fun update(
    msg: Msg,
    model: Model,
): Pair<Model, Effects> =
    when (msg) {
        is Msg.DistanceChanged -> {
            model.copy(
                kinematics = model.kinematics.withDistance(msg.value, DistanceSolveFor.Time),
            ) to noEffects
        }
        is Msg.TimeChanged -> {
            model.copy(
                kinematics = model.kinematics.withTime(msg.value, TimeSolveFor.Pace),
            ) to noEffects
        }
        is Msg.PaceChanged -> {
            model.copy(
                kinematics = model.kinematics.withPace(msg.value, PaceSolveFor.Time),
            ) to noEffects
        }

        is Msg.TestEffectRequested -> model to listOf(DelayEffect(msg.message, 2000))
        is Msg.SpeedChanged -> {
            model.copy(
                kinematics = model.kinematics.withSpeed(msg.value, PaceSolveFor.Time),
            ) to noEffects
        }
    }
