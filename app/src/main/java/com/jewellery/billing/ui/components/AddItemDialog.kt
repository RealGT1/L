package com.jewellery.billing.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jewellery.billing.R
import com.jewellery.billing.data.MetalType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    metalType: MetalType,
    metalRate: Double,
    onDismiss: () -> Unit,
    onAdd: (String, Double, Int, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var makingCharge by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Add ${metalType.name} Item",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        showError = false 
                    },
                    label = { Text(stringResource(R.string.item_name)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    isError = showError && name.isEmpty()
                )
                
                OutlinedTextField(
                    value = weight,
                    onValueChange = { 
                        weight = it
                        showError = false 
                    },
                    label = { Text(stringResource(R.string.weight)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    isError = showError && weight.isEmpty()
                )
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { 
                        quantity = it
                        showError = false 
                    },
                    label = { Text(stringResource(R.string.quantity)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    isError = showError && quantity.isEmpty()
                )
                
                OutlinedTextField(
                    value = makingCharge,
                    onValueChange = { 
                        makingCharge = it
                        showError = false 
                    },
                    label = { Text(stringResource(R.string.making_charge)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    isError = showError && makingCharge.isEmpty()
                )
                
                // Preview calculation
                val weightValue = weight.toDoubleOrNull() ?: 0.0
                val quantityValue = quantity.toIntOrNull() ?: 0
                val makingChargeValue = makingCharge.toDoubleOrNull() ?: 0.0
                
                if (weightValue > 0 && quantityValue > 0) {
                    val totalWeight = weightValue * quantityValue
                    val wastage = totalWeight * 0.10
                    val finalWeight = totalWeight + wastage
                    val totalPrice = (finalWeight * metalRate) + makingChargeValue
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.price_calculation),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text("${stringResource(R.string.total_weight)}: ${String.format("%.2f", totalWeight)}g")
                            Text("${stringResource(R.string.wastage)}: ${String.format("%.2f", wastage)}g")
                            Text("${stringResource(R.string.final_weight)}: ${String.format("%.2f", finalWeight)}g")
                            Text("Rate: ₹${String.format("%.2f", metalRate)}/g")
                            Text("Making Charge: ₹${String.format("%.2f", makingChargeValue)}")
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                text = "Total: ₹${String.format("%.2f", totalPrice)}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                if (showError) {
                    Text(
                        text = stringResource(R.string.invalid_input),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    
                    Button(
                        onClick = {
                            val nameValue = name.trim()
                            val weightValue = weight.toDoubleOrNull()
                            val quantityValue = quantity.toIntOrNull()
                            val makingChargeValue = makingCharge.toDoubleOrNull()
                            
                            if (nameValue.isNotEmpty() && 
                                weightValue != null && weightValue > 0 &&
                                quantityValue != null && quantityValue > 0 &&
                                makingChargeValue != null && makingChargeValue >= 0) {
                                onAdd(nameValue, weightValue, quantityValue, makingChargeValue)
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.add_item))
                    }
                }
            }
        }
    }
}