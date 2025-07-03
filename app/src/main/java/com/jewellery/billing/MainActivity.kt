package com.jewellery.billing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jewellery.billing.data.BillingDatabase
import com.jewellery.billing.data.BillingRepository
import com.jewellery.billing.navigation.Screen
import com.jewellery.billing.ui.screens.BillingScreen
import com.jewellery.billing.ui.screens.BillSummaryScreen
import com.jewellery.billing.ui.screens.RateInputScreen
import com.jewellery.billing.ui.theme.JewelleryBillingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = BillingDatabase.getDatabase(this)
        val repository = BillingRepository(database.billingItemDao())
        
        setContent {
            JewelleryBillingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BillingApp(repository)
                }
            }
        }
    }
}

@Composable
fun BillingApp(repository: BillingRepository) {
    val navController = rememberNavController()
    val viewModel: BillingViewModel = viewModel { BillingViewModel(repository) }
    
    val metalRates by viewModel.metalRates.collectAsState()
    val billSummary by viewModel.billSummary.collectAsState()
    val currentBillItems by viewModel.currentBillItems.collectAsState(emptyList())
    
    NavHost(
        navController = navController,
        startDestination = Screen.RateInput.route
    ) {
        composable(Screen.RateInput.route) {
            RateInputScreen(
                onRatesSubmitted = { rates ->
                    viewModel.setMetalRates(rates)
                    navController.navigate(Screen.Billing.route)
                }
            )
        }
        
        composable(Screen.Billing.route) {
            metalRates?.let { rates ->
                BillingScreen(
                    items = currentBillItems,
                    metalRates = rates,
                    onAddItem = { name, weight, quantity, makingCharge, metalType ->
                        viewModel.addItem(name, weight, quantity, makingCharge, metalType)
                    },
                    onDeleteItem = { item ->
                        viewModel.deleteItem(item)
                    },
                    onGenerateBill = {
                        viewModel.generateBill(currentBillItems)
                        navController.navigate(Screen.BillSummary.route)
                    }
                )
            }
        }
        
        composable(Screen.BillSummary.route) {
            billSummary?.let { summary ->
                BillSummaryScreen(
                    billSummary = summary,
                    onNewBill = {
                        viewModel.startNewBill()
                        navController.navigate(Screen.RateInput.route) {
                            popUpTo(Screen.RateInput.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}