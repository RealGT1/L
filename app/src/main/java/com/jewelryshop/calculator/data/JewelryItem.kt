package com.jewelryshop.calculator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jewelry_items")
data class JewelryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val material: String, // "Gold" or "Silver"
    val isDefault: Boolean = false // true for default items, false for user-added items
)

enum class MaterialType(val displayName: String) {
    GOLD("Gold"),
    SILVER("Silver")
}

data class PriceCalculation(
    val originalWeight: Double,
    val weightWithAddition: Double,
    val makingCharge: Double,
    val finalPrice: Double
)