package com.example.pbd_jwr.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pbd_jwr.converters.CategoryConverter
import com.example.pbd_jwr.data.dao.TransactionDao
import com.example.pbd_jwr.data.entity.Transaction
import com.example.pbd_jwr.data.model.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Transaction::class], version = 3)
@TypeConverters(CategoryConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.transactionDao())
                }
            }
        }

        private suspend fun populateDatabase(transactionDao: TransactionDao) {
            // Seed transactions
            val transaction1 = Transaction(
                email = "13521103@std.stei.itb.ac.id",
                title = "Transaction 1",
                category = Category.INCOME,
                amount = 100.0,
                latitude = -6.2088,
                longitude = 106.8456,
                date = System.currentTimeMillis()
            )
            val transaction2 = Transaction(
                email = "13521103@std.stei.itb.ac.id",
                title = "Transaction 2",
                category = Category.EXPENSE,
                amount = 200.0,
                latitude = -6.2188,
                longitude = 106.8399,
                date = System.currentTimeMillis()
            )
            val transaction3 = Transaction(
                email = "13521103@std.stei.itb.ac.id",
                title = "Transaction 3",
                category = Category.INCOME,
                amount = 300.0,
                latitude = -6.2024,
                longitude = 106.8247,
                date = System.currentTimeMillis()
            )
            val transaction4 = Transaction(
                email = "13521103@std.stei.itb.ac.id",
                title = "Transaction 4",
                category = Category.EXPENSE,
                amount = 400.0,
                latitude = -6.2426,
                longitude = 106.8000,
                date = System.currentTimeMillis()
            )
            val transaction5 = Transaction(
                email = "13521103@std.stei.itb.ac.id",
                title = "Transaction 5",
                category = Category.EXPENSE,
                amount = 500.0,
                latitude = -6.2787,
                longitude = 106.8467,
                date = System.currentTimeMillis()
            )
            val transaction6 = Transaction(
                email = "13521103@std.stei.itb.ac.id",
                title = "Transaction 6",
                category = Category.INCOME,
                amount = 600.0,
                latitude = -6.2475,
                longitude = 107.1485,
                date = System.currentTimeMillis()
            )

            transactionDao.addTransaction(transaction1)
            transactionDao.addTransaction(transaction2)
            transactionDao.addTransaction(transaction3)
            transactionDao.addTransaction(transaction4)
            transactionDao.addTransaction(transaction5)
            transactionDao.addTransaction(transaction6)

        }
    }
}
