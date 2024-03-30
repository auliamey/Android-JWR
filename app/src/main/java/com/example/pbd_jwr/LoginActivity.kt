package com.example.pbd_jwr

//import com.google.android.material.bottomnavigation.BottomNavigationView
//import androidx.navigation.findNavController
//import androidx.navigation.ui.AppBarConfiguration
//import androidx.navigation.ui.setupWithNavController
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pbd_jwr.databinding.ActivityLoginBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import org.json.JSONObject

//import com.example.pbd_jwr.network.ApiServiceManager

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var email: String
    private lateinit var password:String
    private  var client = OkHttpClient()
    private val postURL = "https://pbd-backend-2024.vercel.app/api/auth/login"
    val mediaType = "application/json; charset=utf-8".toMediaType()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user is already logged in
        if (isLoggedIn()) {
            // If the user is already logged in, start the MainActivity
            startMainActivity()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val Email: TextView = findViewById(R.id.emailText)
        val Password: TextView = findViewById(R.id.passwordText)


        // Set up login UI and handle login process
        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            // Perform login authentication
            email = Email.text.toString()
            password = Password.text.toString()



            if(email == ""){
                Email.error = "Email is required"
            }else if(password == ""){
                Password.error = "Password is required"
            }else{

                post(email,password)
            }

        }
    }

    private fun isLoggedIn(): Boolean {
        // Check if the user is already logged in (e.g., check if credentials are stored)
        // Return true if logged in, false otherwise
        // Replace this with your actual implementation
        return false // Placeholder implementation
    }

    private fun authenticateUser(email: String, password: String) {
        // Perform authentication logic (e.g., check credentials against database)
        // Return true if authentication is successful, false otherwise
        post(email, password)
//        return true;
    }

    private fun post(email: String, password: String){


        val jsonObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("password", password)
        val requestBody :RequestBody = jsonObject.toString().toRequestBody(mediaType)
        println(requestBody.toString())

        println(email)
        println(password)

        val request :Request = Request.Builder().url(postURL).post(requestBody).build();
        val response = client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {

                runOnUiThread {
                    e.printStackTrace();
                }
            }

            override fun onResponse(call: Call, response: Response) {

                response.use {
                    if(response.isSuccessful){
                        Log.i("HTTP Succes",response.body.toString())
                        println(response.body?.string())
                        println(response.code)
                    }else{
                        Log.e("HTTP Error","Rusak")
                        println(response.code)
                    }

                }

            }
        })

    }
    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Finish LoginActivity to prevent user from returning by pressing back
    }


}
