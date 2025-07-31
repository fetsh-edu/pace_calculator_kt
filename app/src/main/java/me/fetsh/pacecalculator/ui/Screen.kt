package me.fetsh.pacecalculator.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import me.fetsh.pacecalculator.core.Msg
import me.fetsh.pacecalculator.core.ShowToastEffect
import me.fetsh.pacecalculator.domain.DistanceUnit
import me.fetsh.pacecalculator.domain.NamedDistance
import me.fetsh.pacecalculator.domain.Pace
import me.fetsh.pacecalculator.domain.PaceUnit
import me.fetsh.pacecalculator.domain.PlainDistance
import me.fetsh.pacecalculator.domain.Speed
import me.fetsh.pacecalculator.domain.Time
import me.fetsh.pacecalculator.domain.TimeParts
import me.fetsh.pacecalculator.domain.TimePrecision

/**
 * Playground screen wiring domain inputs (Distance, Pace, Time, Speed) to the ViewModel.
 */
@Composable
fun Screen(viewModel: PaceCalculatorViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel.sideEffects) {
        viewModel.sideEffects.collectLatest { effect ->
            when (effect) {
                is ShowToastEffect -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val kinematics = uiState.kinematics
    val paceParts = kinematics.pace.toTimeParts()
    val timeParts = kinematics.time.toParts()

    var paceMinutes by remember(paceParts) { mutableStateOf(paceParts.minutes.toString()) }
    var paceSeconds by remember(paceParts) { mutableStateOf(paceParts.seconds.toString()) }
    var paceMillis by remember(paceParts) { mutableStateOf(paceParts.milliseconds.toString()) }

    var timeHours by remember(timeParts) { mutableStateOf(timeParts.hours.toString()) }
    var timeMinutes by remember(timeParts) { mutableStateOf(timeParts.minutes.toString()) }
    var timeSeconds by remember(timeParts) { mutableStateOf(timeParts.seconds.toString()) }
    var timeMillis by remember(timeParts) { mutableStateOf(timeParts.milliseconds.toString()) }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Distance")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                viewModel.processMessage(Msg.DistanceChanged(NamedDistance.Marathon))
            }) {
                Text("Marathon")
            }
            Button(onClick = {
                viewModel.processMessage(Msg.DistanceChanged(NamedDistance.HalfMarathon))
            }) {
                Text("Half Marathon")
            }
        }
        OutlinedTextField(
            value =
                kinematics.distance.distance
                    .to(DistanceUnit.Kilometer)
                    .toString(),
            onValueChange = {
                it.toDoubleOrNull()?.let { value ->
                    viewModel.processMessage(Msg.DistanceChanged(PlainDistance(value, DistanceUnit.Kilometer)))
                }
            },
            label = { Text("Custom distance") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Pace")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = paceMinutes,
                onValueChange = {
                    paceMinutes = it
                    val min = it.toIntOrNull() ?: 0
                    val sec = paceSeconds.toIntOrNull() ?: 0
                    val ms = paceMillis.toIntOrNull() ?: 0
                    viewModel.processMessage(Msg.PaceChanged(Pace.of(min, sec, ms, TimePrecision.Milliseconds, PaceUnit.PerKilometre)))
                },
                label = { Text("Min") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = paceSeconds,
                onValueChange = {
                    paceSeconds = it
                    val min = paceMinutes.toIntOrNull() ?: 0
                    val sec = it.toIntOrNull() ?: 0
                    val ms = paceMillis.toIntOrNull() ?: 0
                    viewModel.processMessage(Msg.PaceChanged(Pace.of(min, sec, ms, TimePrecision.Milliseconds, PaceUnit.PerKilometre)))
                },
                label = { Text("Sec") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = paceMillis,
                onValueChange = {
                    paceMillis = it
                    val min = paceMinutes.toIntOrNull() ?: 0
                    val sec = paceSeconds.toIntOrNull() ?: 0
                    val ms = it.toIntOrNull() ?: 0
                    viewModel.processMessage(Msg.PaceChanged(Pace.of(min, sec, ms, TimePrecision.Milliseconds, PaceUnit.PerKilometre)))
                },
                label = { Text("Ms") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        Text("Time")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = timeHours,
                onValueChange = {
                    timeHours = it
                    val h = it.toIntOrNull() ?: 0
                    val m = timeMinutes.toIntOrNull() ?: 0
                    val s = timeSeconds.toIntOrNull() ?: 0
                    val ms = timeMillis.toIntOrNull() ?: 0
                    viewModel.processMessage(Msg.TimeChanged(Time.of(TimeParts.of(h, m, s, ms, TimePrecision.Milliseconds))))
                },
                label = { Text("Hr") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = timeMinutes,
                onValueChange = {
                    timeMinutes = it
                    val h = timeHours.toIntOrNull() ?: 0
                    val m = it.toIntOrNull() ?: 0
                    val s = timeSeconds.toIntOrNull() ?: 0
                    val ms = timeMillis.toIntOrNull() ?: 0
                    viewModel.processMessage(Msg.TimeChanged(Time.of(TimeParts.of(h, m, s, ms, TimePrecision.Milliseconds))))
                },
                label = { Text("Min") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = timeSeconds,
                onValueChange = {
                    timeSeconds = it
                    val h = timeHours.toIntOrNull() ?: 0
                    val m = timeMinutes.toIntOrNull() ?: 0
                    val s = it.toIntOrNull() ?: 0
                    val ms = timeMillis.toIntOrNull() ?: 0
                    viewModel.processMessage(Msg.TimeChanged(Time.of(TimeParts.of(h, m, s, ms, TimePrecision.Milliseconds))))
                },
                label = { Text("Sec") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = timeMillis,
                onValueChange = {
                    timeMillis = it
                    val h = timeHours.toIntOrNull() ?: 0
                    val m = timeMinutes.toIntOrNull() ?: 0
                    val s = timeSeconds.toIntOrNull() ?: 0
                    val ms = it.toIntOrNull() ?: 0
                    viewModel.processMessage(Msg.TimeChanged(Time.of(TimeParts.of(h, m, s, ms, TimePrecision.Milliseconds))))
                },
                label = { Text("Ms") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        Text("Speed (m/s)")
        OutlinedTextField(
            value =
                kinematics.pace
                    .toSpeed()
                    .metersPerSecond
                    .toString(),
            onValueChange = {
                it.toDoubleOrNull()?.let { spd ->
                    viewModel.processMessage(Msg.SpeedChanged(Speed(spd), uiState.speedUnit))
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                viewModel.processMessage(Msg.TestEffectRequested("Hello from UI"))
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Test Effect")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenPreview() {
    Screen()
}
