package me.fetsh.pacecalculator.ui.calculator

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
): Pair<Model, Set<Effect>> =
    when (msg) {
        is Msg.DistanceChanged -> {
            model.copy(distance = msg.value) to emptySet()
        }
        is Msg.TimeChanged -> {
            model.copy(time = msg.value) to emptySet()
        }
        else -> model to emptySet() // TODO: Implement other messages
    }
