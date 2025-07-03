package com.jewellerycalculator.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [JewelleryItem::class],
    version = 1,
    exportSchema = false
)
abstract class JewelleryDatabase : RoomDatabase() {
    abstract fun jewelleryItemDao(): JewelleryItemDao

    companion object {
        @Volatile
        private var INSTANCE: JewelleryDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): JewelleryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JewelleryDatabase::class.java,
                    "jewellery_database"
                ).addCallback(JewelleryDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class JewelleryDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateDatabase(database.jewelleryItemDao())
                    }
                }
            }
        }

        private suspend fun populateDatabase(jewelleryItemDao: JewelleryItemDao) {
            // Default Gold items
            val goldItems = listOf(
                JewelleryItem(name = "Ring", material = "Gold", isDefault = true),
                JewelleryItem(name = "Chain", material = "Gold", isDefault = true),
                JewelleryItem(name = "Necklace", material = "Gold", isDefault = true),
                JewelleryItem(name = "Earrings", material = "Gold", isDefault = true),
                JewelleryItem(name = "Bangle", material = "Gold", isDefault = true)
            )

            // Default Silver items
            val silverItems = listOf(
                JewelleryItem(name = "Anklet", material = "Silver", isDefault = true),
                JewelleryItem(name = "Toe Ring", material = "Silver", isDefault = true),
                JewelleryItem(name = "Bracelet", material = "Silver", isDefault = true),
                JewelleryItem(name = "Idol", material = "Silver", isDefault = true)
            )

            jewelleryItemDao.insertItems(goldItems + silverItems)
        }
    }
}