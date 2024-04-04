package com.example.pbd_jwr.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pbd_jwr.data.entity.Transaction
import java.util.Locale.Category

@Dao
interface TransactionDao {
    @Query("SELECT * FROM `transaction`")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM `transaction` WHERE email = :email")
    fun getTransactionsByEmail(email: String): LiveData<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}