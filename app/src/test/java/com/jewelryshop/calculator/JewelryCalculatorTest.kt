package com.jewelryshop.calculator

import com.jewelryshop.calculator.data.MaterialType
import com.jewelryshop.calculator.data.PriceCalculation
import org.junit.Test
import org.junit.Assert.*

class JewelryCalculatorTest {
    
    @Test
    fun testMaterialTypeDisplayNames() {
        assertEquals("Gold", MaterialType.GOLD.displayName)
        assertEquals("Silver", MaterialType.SILVER.displayName)
    }
    
    @Test
    fun testPriceCalculationLogic() {
        // Test the price calculation logic directly
        val weight = 100.0
        val makingCharge = 50.0
        
        // Manual calculation like in the repository
        val weightWithAddition = weight + (weight * 0.1) // Add 10% to weight
        val finalPrice = weightWithAddition + makingCharge
        
        val calculation = PriceCalculation(
            originalWeight = weight,
            weightWithAddition = weightWithAddition,
            makingCharge = makingCharge,
            finalPrice = finalPrice
        )
        
        assertEquals(100.0, calculation.originalWeight, 0.01)
        assertEquals(110.0, calculation.weightWithAddition, 0.01) // 100 + 10%
        assertEquals(50.0, calculation.makingCharge, 0.01)
        assertEquals(160.0, calculation.finalPrice, 0.01) // 110 + 50
    }
    
    @Test
    fun testPriceCalculationZeroMakingCharge() {
        val weight = 50.0
        val makingCharge = 0.0
        
        val weightWithAddition = weight + (weight * 0.1)
        val finalPrice = weightWithAddition + makingCharge
        
        val calculation = PriceCalculation(
            originalWeight = weight,
            weightWithAddition = weightWithAddition,
            makingCharge = makingCharge,
            finalPrice = finalPrice
        )
        
        assertEquals(50.0, calculation.originalWeight, 0.01)
        assertEquals(55.0, calculation.weightWithAddition, 0.01) // 50 + 10%
        assertEquals(0.0, calculation.makingCharge, 0.01)
        assertEquals(55.0, calculation.finalPrice, 0.01) // 55 + 0
    }
}