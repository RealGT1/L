package com.jewellery.billing

import com.jewellery.billing.data.BillingItem
import com.jewellery.billing.data.MetalRates
import com.jewellery.billing.data.MetalType
import org.junit.Test
import org.junit.Assert.*

class BillingCalculationsTest {

    @Test
    fun testPriceCalculation() {
        val weight = 10.0
        val quantity = 4
        val makingCharge = 250.0
        val metalRate = 108.0
        
        val totalWeight = weight * quantity
        val wastage = totalWeight * 0.10 // 10% wastage
        val finalWeight = totalWeight + wastage
        val totalPrice = (finalWeight * metalRate) + makingCharge
        
        assertEquals(40.0, totalWeight, 0.01)
        assertEquals(4.0, wastage, 0.01)
        assertEquals(44.0, finalWeight, 0.01)
        assertEquals(5002.0, totalPrice, 0.01)
    }
    
    @Test
    fun testBillSummaryCalculation() {
        val silverItems = listOf(
            BillingItem(
                id = 1,
                name = "s1",
                weight = 10.0,
                quantity = 4,
                makingCharge = 250.0,
                metalType = MetalType.SILVER,
                totalWeight = 40.0,
                wastage = 4.0,
                finalWeight = 44.0,
                totalPrice = 5002.0
            ),
            BillingItem(
                id = 2,
                name = "s2",
                weight = 25.0,
                quantity = 3,
                makingCharge = 350.0,
                metalType = MetalType.SILVER,
                totalWeight = 75.0,
                wastage = 7.5,
                finalWeight = 82.5,
                totalPrice = 9260.0
            )
        )
        
        val goldItems = listOf(
            BillingItem(
                id = 3,
                name = "g1",
                weight = 15.0,
                quantity = 1,
                makingCharge = 1500.0,
                metalType = MetalType.GOLD,
                totalWeight = 15.0,
                wastage = 1.5,
                finalWeight = 16.5,
                totalPrice = 151650.0
            )
        )
        
        val silverTotal = silverItems.sumOf { it.totalPrice }
        val goldTotal = goldItems.sumOf { it.totalPrice }
        val grandTotal = silverTotal + goldTotal
        
        assertEquals(14262.0, silverTotal, 0.01)
        assertEquals(151650.0, goldTotal, 0.01)
        assertEquals(165912.0, grandTotal, 0.01)
    }
}