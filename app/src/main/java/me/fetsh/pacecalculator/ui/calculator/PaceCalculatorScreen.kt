package me.fetsh.pacecalculator.ui.calculator

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PaceCalculatorScreen() {
    Text(text = "Hello, Pace Calculator!")
}

@Preview(showBackground = true)
@Composable
fun PaceCalculatorScreenPreview() {
    PaceCalculatorScreen()
}