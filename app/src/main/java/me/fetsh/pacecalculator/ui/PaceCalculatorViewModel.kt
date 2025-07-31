package me.fetsh.pacecalculator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.fetsh.pacecalculator.core.AppEffect
import me.fetsh.pacecalculator.core.DelayEffect
import me.fetsh.pacecalculator.core.Effects
import me.fetsh.pacecalculator.core.Model
import me.fetsh.pacecalculator.core.Msg
import me.fetsh.pacecalculator.core.ShowToastEffect
import me.fetsh.pacecalculator.core.UiEffect
import me.fetsh.pacecalculator.core.update

class PaceCalculatorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(Model())
    val uiState: StateFlow<Model> = _uiState.asStateFlow()

    private val _sideEffects = MutableSharedFlow<UiEffect>()
    val sideEffects: SharedFlow<UiEffect> = _sideEffects.asSharedFlow()

    /**
     * Processes an incoming [Msg], updates UI state, and handles side effects.
     */
    fun processMessage(msg: Msg) {
        val (updatedModel, effects) = update(msg, _uiState.value)
        println("updatedModel: $updatedModel")
        _uiState.value = updatedModel
        handleSideEffects(effects)
    }

    private fun handleSideEffects(effects: Effects) {
        effects.forEach { effect ->
            when (effect) {
                is AppEffect -> handleAppEffect(effect)
                is UiEffect -> emitUiEffect(effect)
            }
        }
    }

    private fun handleAppEffect(effect: AppEffect) {
        when (effect) {
            is DelayEffect ->
                viewModelScope.launch {
                    delay(effect.delayMillis)
                    _sideEffects.emit(ShowToastEffect(effect.message)) // emit UiEffect
                }
        }
    }

    private fun emitUiEffect(effect: UiEffect) {
        viewModelScope.launch { _sideEffects.emit(effect) }
    }
}
