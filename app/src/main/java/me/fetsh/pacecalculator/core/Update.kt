package me.fetsh.pacecalculator.core

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
            model.copy(distanceExtended = msg.value) to noEffects
        }
        is Msg.TimeChanged -> {
            model.copy(time = msg.value) to noEffects
        }
        is Msg.PaceChanged -> {
            model.copy(pace = msg.value) to noEffects
        }
    }
