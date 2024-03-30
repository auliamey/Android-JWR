package com.example.pbd_jwr.data.model

class LoginModel {
    data class LoginRequest (
        val email: String,
        val password: String
    )

    data class LoginResponse (
        val token: String
    )
}