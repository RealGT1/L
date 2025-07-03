package com.jewelryshop.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jewelryshop.calculator.data.JewelryDatabase
import com.jewelryshop.calculator.data.JewelryRepository
import com.jewelryshop.calculator.ui.theme.JewelryShopCalculatorTheme
import com.jewelryshop.calculator.ui.JewelryCalculatorApp
import com.jewelryshop.calculator.viewmodel.JewelryViewModel
import com.jewelryshop.calculator.viewmodel.JewelryViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = JewelryDatabase.getDatabase(this)
        val repository = JewelryRepository(database.jewelryItemDao())
        val viewModelFactory = JewelryViewModelFactory(repository)
        
        setContent {
            JewelryShopCalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: JewelryViewModel = viewModel(factory = viewModelFactory)
                    JewelryCalculatorApp(viewModel = viewModel)
                }
            }
        }
    }
}