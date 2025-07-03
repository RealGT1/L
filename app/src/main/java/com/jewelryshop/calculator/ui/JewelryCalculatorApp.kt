package com.jewelryshop.calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jewelryshop.calculator.R
import com.jewelryshop.calculator.data.JewelryItem
import com.jewelryshop.calculator.data.MaterialType
import com.jewelryshop.calculator.viewmodel.JewelryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JewelryCalculatorApp(viewModel: JewelryViewModel) {
    val selectedMaterial by viewModel.selectedMaterial.collectAsStateWithLifecycle()
    val jewelryItems by viewModel.jewelryItems.collectAsStateWithLifecycle()
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
    val weight by viewModel.weight.collectAsStateWithLifecycle()
    val makingCharge by viewModel.makingCharge.collectAsStateWithLifecycle()
    val priceCalculation by viewModel.priceCalculation.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            selectedMaterial == null -> {
                MaterialSelectionScreen(
                    onMaterialSelected = { material ->
                        viewModel.selectMaterial(material)
                    }
                )
            }
            selectedItem == null -> {
                ItemSelectionScreen(
                    material = selectedMaterial,
                    items = jewelryItems,
                    onItemSelected = { item ->
                        viewModel.selectItem(item)
                    },
                    onAddNewItem = { itemName ->
                        viewModel.addNewItem(itemName)
                    },
                    onBack = {
                        viewModel.resetToMaterialSelection()
                    }
                )
            }
            else -> {
                PriceCalculationScreen(
                    material = selectedMaterial,
                    item = selectedItem,
                    weight = weight,
                    makingCharge = makingCharge,
                    priceCalculation = priceCalculation,
                    onWeightChange = { newWeight ->
                        viewModel.updateWeight(newWeight)
                    },
                    onMakingChargeChange = { newCharge ->
                        viewModel.updateMakingCharge(newCharge)
                    },
                    onCalculatePrice = {
                        viewModel.calculatePrice()
                    },
                    onBack = {
                        viewModel.selectItem(null)
                    }
                )
            }
        }
    }
}

@Composable
fun MaterialSelectionScreen(
    onMaterialSelected: (MaterialType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.select_material),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        MaterialType.values().forEach { material ->
            ElevatedButton(
                onClick = { onMaterialSelected(material) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = material.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSelectionScreen(
    material: MaterialType,
    items: List<JewelryItem>,
    onItemSelected: (JewelryItem) -> Unit,
    onAddNewItem: (String) -> Unit,
    onBack: () -> Unit
) {
    var showAddItemDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.back))
            }
            
            Text(
                text = "${material.displayName} ${stringResource(R.string.select_item)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                ElevatedCard(
                    onClick = { onItemSelected(item) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.name,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        ElevatedButton(
            onClick = { showAddItemDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_new_item))
        }
    }
    
    if (showAddItemDialog) {
        AddItemDialog(
            onAddItem = { itemName ->
                onAddNewItem(itemName)
                showAddItemDialog = false
            },
            onDismiss = { showAddItemDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    onAddItem: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_item)) },
        text = {
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text(stringResource(R.string.item_name)) },
                placeholder = { Text(stringResource(R.string.enter_item_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (itemName.isNotBlank()) {
                        onAddItem(itemName)
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceCalculationScreen(
    material: MaterialType,
    item: JewelryItem,
    weight: String,
    makingCharge: String,
    priceCalculation: com.jewelryshop.calculator.data.PriceCalculation?,
    onWeightChange: (String) -> Unit,
    onMakingChargeChange: (String) -> Unit,
    onCalculatePrice: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text(stringResource(R.string.back))
                }
                
                Text(
                    text = "${material.displayName} ${item.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
        
        item {
            OutlinedTextField(
                value = weight,
                onValueChange = onWeightChange,
                label = { Text(stringResource(R.string.enter_weight)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            OutlinedTextField(
                value = makingCharge,
                onValueChange = onMakingChargeChange,
                label = { Text(stringResource(R.string.enter_making_charge)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            ElevatedButton(
                onClick = onCalculatePrice,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.calculate_price))
            }
        }
        
        priceCalculation?.let { calculation ->
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                PriceBreakdownCard(calculation = calculation)
            }
        }
    }
}

@Composable
fun PriceBreakdownCard(calculation: com.jewelryshop.calculator.data.PriceCalculation) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.price_breakdown),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Divider()
            
            PriceRow(
                label = "Original Weight:",
                value = "${calculation.originalWeight} grams"
            )
            
            PriceRow(
                label = stringResource(R.string.weight_with_addition),
                value = "${calculation.weightWithAddition} grams"
            )
            
            PriceRow(
                label = stringResource(R.string.making_charge),
                value = "₹${calculation.makingCharge}"
            )
            
            Divider()
            
            PriceRow(
                label = stringResource(R.string.final_price),
                value = "₹${calculation.finalPrice}",
                isTotal = true
            )
        }
    }
}

@Composable
fun PriceRow(
    label: String,
    value: String,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
    }
}