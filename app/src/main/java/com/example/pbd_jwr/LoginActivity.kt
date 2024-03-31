package com.example.pbd_jwr

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pbd_jwr.databinding.ActivityLoginBinding
import com.example.pbd_jwr.encryptedSharedPref.EncryptedSharedPref
import com.example.pbd_jwr.network.NetworkCallbackImplementation
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var email: String
    private lateinit var password:String
    private  var client = OkHttpClient()
    private val postURL = "https://pbd-backend-2024.vercel.app/api/auth/login"
    val mediaType = "application/json; charset=utf-8".toMediaType()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor :Editor

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: NetworkCallbackImplementation
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        sharedPreferences = EncryptedSharedPref.create(applicationContext,"login")
        // Check if the user credential is already stored


        if (isLoggedIn()) {
            // If the user credential is stored, login and start the MainActivity

            val emailRemembered = sharedPreferences.getString("email","")
            val passwordRemembered = sharedPreferences.getString("password","")
            if (passwordRemembered != null && emailRemembered != null) {
                post(emailRemembered,passwordRemembered,true)
            }
            return
        }

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = NetworkCallbackImplementation(this)
        registerNetworkCallback()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val checkBox: CheckBox = binding.rememberMe


        val emailTextView: TextView = binding.emailText
        val passwordTextView: TextView = binding.passwordText


        // Set up login UI and handle login process
        val loginButton: Button = binding.loginButton
        loginButton.setOnClickListener {
            // Perform login authentication
            email = emailTextView.text.toString()
            password = passwordTextView.text.toString()



            if(email == ""){
                emailTextView.error = "Email is required"
            }else if(password == ""){
                passwordTextView.error = "Password is required"
            }else{
                if(isNetworkAvailable()){
                    val isRemember = checkBox.isChecked
                    post(email,password,isRemember)
                }else{
                    Toast.makeText(this,"No network available",Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!isLoggedIn()){
            unregisterNetworkCallback()
        }
    }

    private fun isLoggedIn(): Boolean {
        // Check if the user is already logged in (e.g., check if credentials are stored)
        // Return true if logged in, false otherwise
        return sharedPreferences.getString("isRemember","") == "true" // Placeholder implementation
    }


    private fun post(email: String, password: String, isRemember:Boolean){

        //Initiate JSON Obj
        val jsonObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("password", password)
        val requestBody :RequestBody = jsonObject.toString().toRequestBody(mediaType)

        //Make Request Using Okhttp
        val request :Request = Request.Builder().url(postURL).post(requestBody).build();
        val response = client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to login", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {

                response.use {
                    if(response.isSuccessful){
                        response.body?.let {
                            val responseBodyString = it.string()
                            val jsonObject = JSONObject(responseBodyString)

                            val token = jsonObject.getString("token")



                            sharedPreferencesEditor = sharedPreferences.edit()
                            sharedPreferencesEditor.putString("token",token)
                            sharedPreferencesEditor.putString("email",email)
                            sharedPreferencesEditor.putString("password",password)
                            if(isRemember){
                                sharedPreferencesEditor.putString("isRemember","true")
                            }
                            sharedPreferencesEditor.commit()
                        } ?: run {
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Server error", Toast.LENGTH_SHORT).show()

                            }
                        }

                        startMainActivity()
                    }else{
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Wrong credentials", Toast.LENGTH_SHORT).show()

                        }
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

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null && (
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                )
    }

    private fun registerNetworkCallback() {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    private fun unregisterNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

}
