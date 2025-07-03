package com.jewellery.billing.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewellery.billing.R
import com.jewellery.billing.data.MetalRates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateInputScreen(
    onRatesSubmitted: (MetalRates) -> Unit
) {
    var goldRate by remember { mutableStateOf("") }
    var silverRate by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.rate_input_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = goldRate,
            onValueChange = { 
                goldRate = it
                showError = false
            },
            label = { Text(stringResource(R.string.gold_rate)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            isError = showError && goldRate.isEmpty()
        )

        OutlinedTextField(
            value = silverRate,
            onValueChange = { 
                silverRate = it
                showError = false
            },
            label = { Text(stringResource(R.string.silver_rate)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            isError = showError && silverRate.isEmpty()
        )

        if (showError) {
            Text(
                text = stringResource(R.string.invalid_rate),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                val goldRateValue = goldRate.toDoubleOrNull()
                val silverRateValue = silverRate.toDoubleOrNull()
                
                if (goldRateValue != null && silverRateValue != null && 
                    goldRateValue > 0 && silverRateValue > 0) {
                    onRatesSubmitted(MetalRates(goldRateValue, silverRateValue))
                } else {
                    showError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = stringResource(R.string.continue_button),
                fontSize = 18.sp
            )
        }
    }
}