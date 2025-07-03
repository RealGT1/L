package com.jewellerycalculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jewellerycalculator.R
import com.jewellerycalculator.data.JewelleryItem
import com.jewellerycalculator.data.Material
import com.jewellerycalculator.viewmodel.JewelleryViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceCalculationScreen(
    viewModel: JewelleryViewModel,
    material: Material,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items by viewModel.getItemsByMaterial(material.displayName).collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
    val currentRate by viewModel.currentRate.collectAsStateWithLifecycle()
    val weight by viewModel.weight.collectAsStateWithLifecycle()
    val makingCharge by viewModel.makingCharge.collectAsStateWithLifecycle()
    val calculatedPrice by viewModel.calculatedPrice.collectAsStateWithLifecycle()
    val showNewItemDialog by viewModel.showNewItemDialog.collectAsStateWithLifecycle()

    var expanded by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }

    val materialColor = if (material == Material.GOLD) Color(0xFFFFD700) else Color(0xFFC0C0C0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "${material.displayName} Calculator",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = materialColor,
                titleContentColor = Color.Black,
                navigationIconContentColor = Color.Black
            )
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Current Rate Input
                OutlinedTextField(
                    value = currentRate,
                    onValueChange = viewModel::updateCurrentRate,
                    label = { Text(stringResource(R.string.rate_input_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Item Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedItem?.name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(stringResource(R.string.item_selection_label)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        items.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.name) },
                                onClick = {
                                    viewModel.selectItem(item)
                                    expanded = false
                                }
                            )
                        }
                        
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.add_new_item))
                                }
                            },
                            onClick = {
                                expanded = false
                                viewModel.showNewItemDialog()
                            }
                        )
                    }
                }

                // Weight Input
                OutlinedTextField(
                    value = weight,
                    onValueChange = viewModel::updateWeight,
                    label = { Text(stringResource(R.string.weight_input_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Making Charge Input
                OutlinedTextField(
                    value = makingCharge,
                    onValueChange = viewModel::updateMakingCharge,
                    label = { Text(stringResource(R.string.making_charge_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Wastage Info
                Text(
                    text = stringResource(R.string.wastage_info),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Calculate Button
                Button(
                    onClick = viewModel::calculatePrice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = materialColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = currentRate.isNotBlank() && weight.isNotBlank() && makingCharge.isNotBlank()
                ) {
                    Text(
                        text = stringResource(R.string.calculate_price),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Price Result
                calculatedPrice?.let { price ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.calculation_formula),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Weight: ${DecimalFormat("#.##").format(price.weight)}g",
                                fontSize = 14.sp
                            )
                            
                            Text(
                                text = "Wastage: ${DecimalFormat("#.##").format(price.wastageAmount)}g",
                                fontSize = 14.sp
                            )
                            
                            Text(
                                text = "Total Weight: ${DecimalFormat("#.##").format(price.totalWeight)}g",
                                fontSize = 14.sp
                            )
                            
                            Text(
                                text = "Material Cost: ₹${DecimalFormat("#.##").format(price.materialCost)}",
                                fontSize = 14.sp
                            )
                            
                            Text(
                                text = "Making Charge: ₹${DecimalFormat("#.##").format(price.makingCharge)}",
                                fontSize = 14.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = stringResource(R.string.final_price, DecimalFormat("#.##").format(price.finalPrice)),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // New Item Dialog
    if (showNewItemDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideNewItemDialog() },
            title = { Text(stringResource(R.string.new_item_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    label = { Text(stringResource(R.string.item_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newItemName.isNotBlank()) {
                            viewModel.addNewItem(newItemName)
                            viewModel.hideNewItemDialog()
                            newItemName = ""
                        }
                    },
                    enabled = newItemName.isNotBlank()
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.hideNewItemDialog()
                        newItemName = ""
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}