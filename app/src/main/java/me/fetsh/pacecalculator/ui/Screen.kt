package me.fetsh.pacecalculator.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun Screen() {
    Text(text = "Hello, Pace Calculator!")
}

@Preview(showBackground = true)
@Composable
fun ScreenPreview() {
    Screen()
}
