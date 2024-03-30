package com.example.pbd_jwr

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.pbd_jwr.databinding.ActivityLoginBinding
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

    private val keyGenParameterSpec = KeyGenParameterSpec.Builder(
        "login",
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(256)
        .build()

    private lateinit var masterKeyAlias: MasterKey
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor :Editor
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        masterKeyAlias = MasterKey.Builder(applicationContext, "login")
            .setKeyGenParameterSpec(keyGenParameterSpec)
            .build()
        sharedPreferences = EncryptedSharedPreferences.create(
            applicationContext,
            "login",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
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

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val checkBox: CheckBox = binding.rememberMe


        val Email: TextView = binding.emailText
        val Password: TextView = binding.passwordText


        // Set up login UI and handle login process
        val loginButton: Button = binding.loginButton
        loginButton.setOnClickListener {
            // Perform login authentication
            email = Email.text.toString()
            password = Password.text.toString()



            if(email == ""){
                Email.error = "Email is required"
            }else if(password == ""){
                Password.error = "Password is required"
            }else{
                val isRemember = checkBox.isChecked
                post(email,password,isRemember)
            }

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

        //Make Request Using Retrofit
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
                            if(isRemember){
                                sharedPreferencesEditor.putString("isRemember","true")
                                sharedPreferencesEditor.putString("email",email)
                                sharedPreferencesEditor.putString("password",password)
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


}
