package me.fetsh.pacecalculator.core

import com.google.common.truth.Truth.assertThat
import me.fetsh.pacecalculator.domain.Distance
import me.fetsh.pacecalculator.domain.DistanceUnit
import me.fetsh.pacecalculator.domain.Pace
import me.fetsh.pacecalculator.domain.PlainDistance
import me.fetsh.pacecalculator.domain.Time
import org.junit.jupiter.api.Test

class UpdateTest {
    @Test
    fun `when distance is changed, update the distance in the model`() {
        val initialModel = Model(distanceExtended = PlainDistance(10.0, DistanceUnit.Kilometer))
        val newDistance = PlainDistance(20.0, DistanceUnit.Kilometer)
        val msg = Msg.DistanceChanged(value = newDistance)
        val (newModel, effects) = update(msg, initialModel)
        assertThat(newModel.distanceExtended.distance).isEqualTo(newDistance.distance)
        assertThat(effects).isEmpty()
    }

    @Test
    fun `when time is changed, update the time in the model`() {
        val initialModel = Model(time = Time.fromMinutes(10))
        val newTime = Time.fromMinutes(120)
        val msg = Msg.TimeChanged(value = Time.fromMinutes(120))
        val (newModel, effects) = update(msg, initialModel)
        assertThat(newModel.time).isEqualTo(newTime)
        assertThat(effects).isEmpty()
    }

    @Test
    fun `when pace is changed, update the pace in the model`() {
        val initialModel = Model(pace = Pace.of(Time.fromMinutes(6), Distance.of(1.0, DistanceUnit.Kilometer)))
        val newPace = Pace.of(Time.fromMinutes(5), Distance.of(1.0, DistanceUnit.Kilometer))
        val msg = Msg.PaceChanged(value = newPace)
        val (newModel, effects) = update(msg, initialModel)
        assertThat(newModel.pace).isEqualTo(newPace)
        assertThat(effects).isEmpty()
    }
}
