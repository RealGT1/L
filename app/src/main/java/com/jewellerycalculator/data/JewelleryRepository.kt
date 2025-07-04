package com.jewellerycalculator.data

import kotlinx.coroutines.flow.Flow

class JewelleryRepository(private val jewelleryItemDao: JewelleryItemDao) {
    
    fun getItemsByMaterial(material: String): Flow<List<JewelleryItem>> {
        return jewelleryItemDao.getItemsByMaterial(material)
    }

    suspend fun insertItem(item: JewelleryItem) {
        jewelleryItemDao.insertItem(item)
    }

    suspend fun getDefaultItemsCount(material: String): Int {
        return jewelleryItemDao.getDefaultItemsCount(material)
    }
}