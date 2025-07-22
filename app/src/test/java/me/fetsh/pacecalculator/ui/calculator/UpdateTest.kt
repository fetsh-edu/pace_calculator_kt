package me.fetsh.pacecalculator.ui.calculator

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UpdateTest {
    @Test
    fun `when distance is changed, update the distance in the model`() {
        val initialModel = Model(distance = "10")
        val msg = Msg.DistanceChanged(value = "20")
        val (newModel, effects) = update(msg, initialModel)
        assertThat(newModel.distance).isEqualTo("20")
        assertThat(effects).isEmpty()
    }

    @Test
    fun `when time is changed, update the time in the model`() {
        val initialModel = Model(time = "10")
        val msg = Msg.TimeChanged(value = "20")
        val (newModel, effects) = update(msg, initialModel)
        assertThat(newModel.time).isEqualTo("20")
        assertThat(effects).isEmpty()
    }
}
