package com.example.pbd_jwr.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pbd_jwr.data.dao.TransactionDao
import com.example.pbd_jwr.data.dao.UserDao
import com.example.pbd_jwr.data.entity.Transaction
import com.example.pbd_jwr.data.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class, Transaction::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null){
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addCallback(DatabaseCallback(context))
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
                    populateDatabase(database.userDao())
                }
            }
        }

        private suspend fun populateDatabase(userDao: UserDao) {
            // Contoh data pengguna yang akan disisipkan ke database saat pertama kali dibuat
            val user1 = User(fullName = "John Doe", email = "john@example.com")
            val user2 = User(fullName = "Jane Doe", email = "jane@example.com")
            userDao.createUser(user1)
            userDao.createUser(user2)
        }
    }
}
