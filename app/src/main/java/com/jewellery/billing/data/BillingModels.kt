package com.jewellery.billing.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "billing_items")
data class BillingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val weight: Double,
    val quantity: Int,
    val makingCharge: Double,
    val metalType: MetalType,
    val totalWeight: Double,
    val wastage: Double,
    val finalWeight: Double,
    val totalPrice: Double,
    val billId: Long? = null
)

enum class MetalType {
    GOLD, SILVER
}

data class MetalRates(
    val goldRate: Double,
    val silverRate: Double
)

data class BillSummary(
    val silverItems: List<BillingItem>,
    val goldItems: List<BillingItem>,
    val silverTotal: Double,
    val goldTotal: Double,
    val grandTotal: Double,
    val metalRates: MetalRates
)