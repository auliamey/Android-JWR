package com.example.pbd_jwr.data.repository

import androidx.lifecycle.LiveData
import com.example.pbd_jwr.data.dao.UserDao
import com.example.pbd_jwr.data.entity.User

class UserRepository(private val userDao: UserDao)  {

    fun getAllUsers(): LiveData<List<User>> {
        return userDao.getAllUsers()
    }

    suspend fun createUser(user: User) {
        userDao.createUser(user)
    }

}