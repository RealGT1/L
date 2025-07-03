package com.jewellerycalculator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jewellery_items")
data class JewelleryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val material: String, // "Gold" or "Silver"
    val isDefault: Boolean = false
)

enum class Material(val displayName: String) {
    GOLD("Gold"),
    SILVER("Silver")
}

data class PriceCalculation(
    val weight: Double,
    val rate: Double,
    val makingCharge: Double,
    val wastagePercentage: Double = 10.0
) {
    val wastageAmount: Double get() = weight * (wastagePercentage / 100.0)
    val totalWeight: Double get() = weight + wastageAmount
    val materialCost: Double get() = totalWeight * rate
    val finalPrice: Double get() = materialCost + makingCharge
}