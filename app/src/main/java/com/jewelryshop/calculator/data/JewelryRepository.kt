package com.jewelryshop.calculator.data

class JewelryRepository(private val jewelryItemDao: JewelryItemDao) {
    
    fun getItemsByMaterial(material: String) = jewelryItemDao.getItemsByMaterial(material)
    
    suspend fun insertItem(item: JewelryItem) = jewelryItemDao.insertItem(item)
    
    suspend fun initializeDefaultItems() {
        val count = jewelryItemDao.getDefaultItemsCount()
        if (count == 0) {
            val defaultItems = listOf(
                // Gold items
                JewelryItem(name = "Ring", material = MaterialType.GOLD.displayName, isDefault = true),
                JewelryItem(name = "Chain", material = MaterialType.GOLD.displayName, isDefault = true),
                JewelryItem(name = "Necklace", material = MaterialType.GOLD.displayName, isDefault = true),
                JewelryItem(name = "Earrings", material = MaterialType.GOLD.displayName, isDefault = true),
                JewelryItem(name = "Bangle", material = MaterialType.GOLD.displayName, isDefault = true),
                
                // Silver items
                JewelryItem(name = "Anklet", material = MaterialType.SILVER.displayName, isDefault = true),
                JewelryItem(name = "Toe Ring", material = MaterialType.SILVER.displayName, isDefault = true),
                JewelryItem(name = "Bracelet", material = MaterialType.SILVER.displayName, isDefault = true),
                JewelryItem(name = "Idol", material = MaterialType.SILVER.displayName, isDefault = true)
            )
            jewelryItemDao.insertItems(defaultItems)
        }
    }
    
    fun calculatePrice(weight: Double, makingCharge: Double): PriceCalculation {
        val weightWithAddition = weight + (weight * 0.1) // Add 10% to weight
        val finalPrice = weightWithAddition + makingCharge
        
        return PriceCalculation(
            originalWeight = weight,
            weightWithAddition = weightWithAddition,
            makingCharge = makingCharge,
            finalPrice = finalPrice
        )
    }
}