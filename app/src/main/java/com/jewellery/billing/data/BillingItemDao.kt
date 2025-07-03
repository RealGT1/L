package com.jewellery.billing.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BillingItemDao {
    @Query("SELECT * FROM billing_items WHERE billId IS NULL")
    fun getCurrentBillItems(): Flow<List<BillingItem>>
    
    @Query("SELECT * FROM billing_items WHERE billId = :billId")
    fun getBillItems(billId: Long): Flow<List<BillingItem>>
    
    @Insert
    suspend fun insert(item: BillingItem): Long
    
    @Update
    suspend fun update(item: BillingItem)
    
    @Delete
    suspend fun delete(item: BillingItem)
    
    @Query("DELETE FROM billing_items WHERE billId IS NULL")
    suspend fun clearCurrentBill()
    
    @Query("SELECT * FROM billing_items WHERE id = :id")
    suspend fun getItemById(id: Long): BillingItem?
}