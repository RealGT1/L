package com.jewellerycalculator.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JewelleryItemDao {
    @Query("SELECT * FROM jewellery_items WHERE material = :material ORDER BY isDefault DESC, name ASC")
    fun getItemsByMaterial(material: String): Flow<List<JewelleryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: JewelleryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<JewelleryItem>)

    @Query("SELECT COUNT(*) FROM jewellery_items WHERE material = :material AND isDefault = 1")
    suspend fun getDefaultItemsCount(material: String): Int
}