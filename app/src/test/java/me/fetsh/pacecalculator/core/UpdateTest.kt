package me.fetsh.pacecalculator.core

import com.google.common.truth.Truth.assertThat
import me.fetsh.pacecalculator.domain.DistanceSolveFor
import me.fetsh.pacecalculator.domain.DistanceUnit
import me.fetsh.pacecalculator.domain.Kinematics
import me.fetsh.pacecalculator.domain.Pace
import me.fetsh.pacecalculator.domain.PaceSolveFor
import me.fetsh.pacecalculator.domain.PaceUnit
import me.fetsh.pacecalculator.domain.PlainDistance
import me.fetsh.pacecalculator.domain.Speed
import me.fetsh.pacecalculator.domain.SpeedUnit
import me.fetsh.pacecalculator.domain.Time
import me.fetsh.pacecalculator.domain.TimeSolveFor
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class UpdateTest {
    @Test
    fun `when distance is changed, update the distance in the model`() {
        val initialKinematics = Kinematics.INIT
        val initialModel = Model(kinematics = initialKinematics, speedUnit = SpeedUnit.KmpH)

        val newDistance = PlainDistance(20.0, DistanceUnit.Kilometer)
        val msg = Msg.DistanceChanged(value = newDistance)

        val newKinematics = initialKinematics.withDistance(newDistance, DistanceSolveFor.Time)

        val (newModel, effects) = update(msg, initialModel)

        assertThat(newModel.kinematics.distance).isEqualTo(newKinematics.distance)
        assertThat(effects).isEmpty()
    }

    @Test
    fun `when time is changed, update the time in the model`() {
        val initialKinematics = Kinematics.INIT
        val initialModel = Model(kinematics = initialKinematics, speedUnit = SpeedUnit.KmpH)

        val newTime = Time.fromMinutes(BigDecimal(120))
        val msg = Msg.TimeChanged(value = newTime)

        val newKinematics = initialKinematics.withTime(newTime, TimeSolveFor.Pace)

        val (newModel, effects) = update(msg, initialModel)

        assertThat(newModel.kinematics.time).isEqualTo(newKinematics.time)
        assertThat(effects).isEmpty()
    }

    @Test
    fun `when pace is changed, update the pace in the model`() {
        val initialKinematics = Kinematics.INIT
        val initialModel = Model(kinematics = initialKinematics, speedUnit = SpeedUnit.KmpH)

        val newPace = Pace.of(5, 0, 0, PaceUnit.PerKilometre)
        val msg = Msg.PaceChanged(value = newPace)

        val newKinematics = initialKinematics.withPace(newPace, PaceSolveFor.Time)

        val (newModel, effects) = update(msg, initialModel)

        assertThat(newModel.kinematics.pace).isEqualTo(newKinematics.pace)
        assertThat(effects).isEmpty()
    }

    @Test
    fun `when speed is changed, update the pace in the model`() {
        val initialKinematics = Kinematics.INIT
        val initialModel = Model(kinematics = initialKinematics, speedUnit = SpeedUnit.KmpH)

        val speed = Speed(BigDecimal(3.5))
        val msg = Msg.SpeedChanged(value = speed, speedUnit = SpeedUnit.KmpH)

        val newKinematics = initialKinematics.withSpeed(speed, PaceSolveFor.Time)

        val (newModel, effects) = update(msg, initialModel)

        assertThat(newModel.kinematics.speed.metersPerSecond).isEqualTo(newKinematics.speed.metersPerSecond)
//        isWithin(0.0001).of(newKinematics.speed.metersPerSecond)
        assertThat(effects).isEmpty()
    }

    @Test
    fun `when test effect is triggered, DelayEffect is emitted`() {
        val model =
            Model(
                kinematics = Kinematics.INIT,
                speedUnit = SpeedUnit.KmpH,
            )

        val msg = Msg.TestEffectRequested("Toast this")
        val (newModel, effects) = update(msg, model)

        assertThat(newModel).isEqualTo(model)
        assertThat(effects).hasSize(1)
        assertThat(effects[0]).isInstanceOf(DelayEffect::class.java)
        assertThat((effects[0] as DelayEffect).message).isEqualTo("Toast this")
    }
}
