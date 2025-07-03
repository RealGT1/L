package com.jewellery.billing.data

import kotlinx.coroutines.flow.Flow

class BillingRepository(private val billingItemDao: BillingItemDao) {
    
    fun getCurrentBillItems(): Flow<List<BillingItem>> {
        return billingItemDao.getCurrentBillItems()
    }
    
    suspend fun addItem(
        name: String,
        weight: Double,
        quantity: Int,
        makingCharge: Double,
        metalType: MetalType,
        metalRate: Double
    ): Long {
        val totalWeight = weight * quantity
        val wastage = totalWeight * 0.10 // 10% wastage
        val finalWeight = totalWeight + wastage
        val totalPrice = (finalWeight * metalRate) + makingCharge
        
        val item = BillingItem(
            name = name,
            weight = weight,
            quantity = quantity,
            makingCharge = makingCharge,
            metalType = metalType,
            totalWeight = totalWeight,
            wastage = wastage,
            finalWeight = finalWeight,
            totalPrice = totalPrice
        )
        
        return billingItemDao.insert(item)
    }
    
    suspend fun deleteItem(item: BillingItem) {
        billingItemDao.delete(item)
    }
    
    suspend fun clearCurrentBill() {
        billingItemDao.clearCurrentBill()
    }
    
    fun calculateBillSummary(items: List<BillingItem>, metalRates: MetalRates): BillSummary {
        val silverItems = items.filter { it.metalType == MetalType.SILVER }
        val goldItems = items.filter { it.metalType == MetalType.GOLD }
        
        val silverTotal = silverItems.sumOf { it.totalPrice }
        val goldTotal = goldItems.sumOf { it.totalPrice }
        val grandTotal = silverTotal + goldTotal
        
        return BillSummary(
            silverItems = silverItems,
            goldItems = goldItems,
            silverTotal = silverTotal,
            goldTotal = goldTotal,
            grandTotal = grandTotal,
            metalRates = metalRates
        )
    }
}