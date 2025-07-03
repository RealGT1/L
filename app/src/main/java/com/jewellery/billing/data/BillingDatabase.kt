package com.jewellery.billing.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [BillingItem::class],
    version = 1,
    exportSchema = false
)
abstract class BillingDatabase : RoomDatabase() {
    abstract fun billingItemDao(): BillingItemDao
    
    companion object {
        @Volatile
        private var INSTANCE: BillingDatabase? = null
        
        fun getDatabase(context: Context): BillingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BillingDatabase::class.java,
                    "billing_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}