package com.jewellery.billing.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import com.jewellery.billing.data.MetalType
import com.jewellery.billing.ui.theme.Gold
import com.jewellery.billing.ui.theme.Silver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCard(
    item: BillingItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (item.metalType) {
                MetalType.GOLD -> Gold.copy(alpha = 0.1f)
                MetalType.SILVER -> Silver.copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${item.weight}g × ${item.quantity} = ${String.format("%.2f", item.totalWeight)}g",
                    fontSize = 14.sp
                )
                Text(
                    text = "Making: ₹${String.format("%.2f", item.makingCharge)}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Total: ₹${String.format("%.2f", item.totalPrice)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete item",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}