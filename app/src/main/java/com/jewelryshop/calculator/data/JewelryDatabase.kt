package com.jewelryshop.calculator.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [JewelryItem::class],
    version = 1,
    exportSchema = false
)
abstract class JewelryDatabase : RoomDatabase() {
    abstract fun jewelryItemDao(): JewelryItemDao

    companion object {
        @Volatile
        private var INSTANCE: JewelryDatabase? = null

        fun getDatabase(context: Context): JewelryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JewelryDatabase::class.java,
                    "jewelry_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}