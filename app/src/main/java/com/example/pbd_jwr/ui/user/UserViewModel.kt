package com.example.pbd_jwr.ui.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.pbd_jwr.data.database.AppDatabase
import com.example.pbd_jwr.data.entity.User
import com.example.pbd_jwr.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<User>> // Change visibility to public
    private val repository: UserRepository

    init {
        val userDao  = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        readAllData = repository.getAllUsers()

        readAllData.observeForever { userList ->
            userList?.let {
                for (user in userList) {
                    println("User: ${user.fullName}, ${user.email}")
                }
            }
        }

        // Inside your UserViewModel or wherever readAllData is accessible
        val userList: List<User>? = readAllData.value


        if (userList != null) {
            for (user in userList) {
                println("User: ${user.fullName}, ${user.email}")
            }
        } else {
            // userList is null, LiveData has not emitted any data yet
            println("No data available yet")
        }

    }

    fun createUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createUser(user)
        }
    }
}
