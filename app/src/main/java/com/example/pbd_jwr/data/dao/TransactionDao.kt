package com.example.pbd_jwr.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pbd_jwr.data.entity.Transaction

@Dao
interface TransactionDao {
    @Query("SELECT * FROM `transaction`")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addTransaction(transaction: Transaction)
}