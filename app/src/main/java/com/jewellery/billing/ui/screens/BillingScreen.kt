package com.jewellery.billing.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewellery.billing.R
import com.jewellery.billing.data.BillingItem
import com.jewellery.billing.data.MetalRates
import com.jewellery.billing.data.MetalType
import com.jewellery.billing.ui.components.AddItemDialog
import com.jewellery.billing.ui.components.ItemCard
import com.jewellery.billing.ui.theme.Gold
import com.jewellery.billing.ui.theme.Silver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    items: List<BillingItem>,
    metalRates: MetalRates,
    onAddItem: (String, Double, Int, Double, MetalType) -> Unit,
    onDeleteItem: (BillingItem) -> Unit,
    onGenerateBill: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMetalType by remember { mutableStateOf(MetalType.GOLD) }
    
    val silverItems = items.filter { it.metalType == MetalType.SILVER }
    val goldItems = items.filter { it.metalType == MetalType.GOLD }
    val silverTotal = silverItems.sumOf { it.totalPrice }
    val goldTotal = goldItems.sumOf { it.totalPrice }
    val grandTotal = silverTotal + goldTotal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Jewellery Billing",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Add buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { 
                    selectedMetalType = MetalType.GOLD
                    showAddDialog = true 
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_gold_item))
            }
            
            Button(
                onClick = { 
                    selectedMetalType = MetalType.SILVER
                    showAddDialog = true 
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Silver)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_silver_item))
            }
        }
        
        // Items list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                ItemCard(
                    item = item,
                    onDelete = { onDeleteItem(item) }
                )
            }
        }
        
        // Totals
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (silverItems.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.silver_total),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "₹${String.format("%.2f", silverTotal)}",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                if (goldItems.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.gold_total),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "₹${String.format("%.2f", goldTotal)}",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.grand_total),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "₹${String.format("%.2f", grandTotal)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
        
        // Generate bill button
        Button(
            onClick = onGenerateBill,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = items.isNotEmpty()
        ) {
            Text(
                text = stringResource(R.string.generate_bill),
                fontSize = 18.sp
            )
        }
    }
    
    // Add item dialog
    if (showAddDialog) {
        AddItemDialog(
            metalType = selectedMetalType,
            metalRate = when (selectedMetalType) {
                MetalType.GOLD -> metalRates.goldRate
                MetalType.SILVER -> metalRates.silverRate
            },
            onDismiss = { showAddDialog = false },
            onAdd = { name, weight, quantity, makingCharge ->
                onAddItem(name, weight, quantity, makingCharge, selectedMetalType)
                showAddDialog = false
            }
        )
    }
}