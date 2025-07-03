package com.jewelryshop.calculator.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JewelryItemDao {
    @Query("SELECT * FROM jewelry_items WHERE material = :material ORDER BY isDefault DESC, name ASC")
    fun getItemsByMaterial(material: String): Flow<List<JewelryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: JewelryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<JewelryItem>)

    @Query("SELECT COUNT(*) FROM jewelry_items WHERE isDefault = 1")
    suspend fun getDefaultItemsCount(): Int
}