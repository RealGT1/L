package com.jewellerycalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jewellerycalculator.data.Material
import com.jewellerycalculator.ui.MaterialSelectionScreen
import com.jewellerycalculator.ui.PriceCalculationScreen
import com.jewellerycalculator.ui.theme.JewelleryCalculatorTheme
import com.jewellerycalculator.viewmodel.JewelleryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JewelleryCalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    JewelleryCalculatorApp()
                }
            }
        }
    }
}

@Composable
fun JewelleryCalculatorApp() {
    val viewModel: JewelleryViewModel = viewModel()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.MaterialSelection) }

    when (currentScreen) {
        Screen.MaterialSelection -> {
            MaterialSelectionScreen(
                onMaterialSelected = { material ->
                    viewModel.selectMaterial(material)
                    viewModel.resetCalculation()
                    currentScreen = Screen.PriceCalculation(material)
                }
            )
        }
        is Screen.PriceCalculation -> {
            PriceCalculationScreen(
                viewModel = viewModel,
                material = currentScreen.material,
                onBackClick = {
                    currentScreen = Screen.MaterialSelection
                }
            )
        }
    }
}

sealed class Screen {
    object MaterialSelection : Screen()
    data class PriceCalculation(val material: Material) : Screen()
}