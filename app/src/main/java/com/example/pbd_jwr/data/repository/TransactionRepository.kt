package com.example.pbd_jwr.data.repository

import androidx.lifecycle.LiveData
import com.example.pbd_jwr.data.dao.TransactionDao
import com.example.pbd_jwr.data.entity.Transaction
import java.util.Locale.Category

class TransactionRepository(private val transactionDao: TransactionDao) {
    fun getAllTransactions(): LiveData<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }

    fun getTransactionsByEmail(email: String): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByEmail(email)
    }

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.addTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }
}