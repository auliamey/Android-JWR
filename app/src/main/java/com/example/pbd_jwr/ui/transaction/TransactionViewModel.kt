package com.example.pbd_jwr.ui.transaction

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pbd_jwr.data.dao.TransactionDao
import com.example.pbd_jwr.data.database.AppDatabase
import com.example.pbd_jwr.data.entity.Transaction
import com.example.pbd_jwr.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale.Category

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val readAllTransactions: LiveData<List<Transaction>>
    private val repository: TransactionRepository

    private val _transactionSubmitted = MutableLiveData<Boolean>()
    val transactionSubmitted: LiveData<Boolean> = _transactionSubmitted
    init {
        println("masuk init")
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)
        readAllTransactions = repository.getAllTransactions()

        readAllTransactions.observeForever { transactions ->
            // Log the list of transactions whenever it changes
            println("All Transactions: $transactions")
        }
    }
    fun getAllTransactions(): LiveData<List<Transaction>> {
        return repository.getAllTransactions()
    }

    fun getTransactionsByEmail(email: String): LiveData<List<Transaction>> {
        return repository.getTransactionsByEmail(email)
    }

    fun addTransaction(transaction: Transaction) {
//        val transactionWithEmail = transaction.copy(email = email)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.addTransaction(transaction)
                println("berhasil")
                _transactionSubmitted.postValue(true)
            } catch (e: Exception) {
                println("ga berhasil")
                _transactionSubmitted.postValue(false)
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateTransaction(transaction)
                _transactionSubmitted.postValue(true)
            } catch (e: Exception) {
                _transactionSubmitted.postValue(false)
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transaction)
        }
    }

}