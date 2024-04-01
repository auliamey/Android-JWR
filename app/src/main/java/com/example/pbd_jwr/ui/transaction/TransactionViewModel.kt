package com.example.pbd_jwr.ui.transaction

import android.app.Application
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

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val readAllTransactions: LiveData<List<Transaction>>
    private val repository: TransactionRepository

    private val _transactionSubmitted = MutableLiveData<Boolean>()
    val transactionSubmitted: LiveData<Boolean> = _transactionSubmitted
    init {
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)
        readAllTransactions = repository.getAllTransactions()
    }
    fun getAllTransactions(): LiveData<List<Transaction>> {
        return repository.getAllTransactions()
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTransaction(transaction)
            _transactionSubmitted.value = true
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transaction)
        }
    }

}