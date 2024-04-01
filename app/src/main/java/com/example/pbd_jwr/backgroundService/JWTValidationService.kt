package com.example.pbd_jwr.backgroundService

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences
import android.os.Handler
import android.os.IBinder;
import android.os.Looper
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Toast
import androidx.security.crypto.MasterKey
import com.example.pbd_jwr.encryptedSharedPref.EncryptedSharedPref
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import java.time.Instant
import kotlin.math.log


class JWTValidationService : Service() {
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private val INTERVAL: Long =  5 * 60 * 1000 // 5 minutes in milliseconds

    private  var client = OkHttpClient()
    private val validateTokenURL = "https://pbd-backend-2024.vercel.app/api/auth/token"
    val mediaType = "application/json; charset=utf-8".toMediaType()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor : SharedPreferences.Editor
    override fun onCreate() {
        super.onCreate()

        sharedPreferences = EncryptedSharedPref.create(applicationContext,"login")
//        handler = Handler(Looper.myLooper()!!) Use this to prevent UI Block since we use it in Service use getMainLooper Instead.
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            // Call your function here
            callCertainFunction()
            handler.postDelayed(runnable, INTERVAL)
        }
        handler.postDelayed(runnable, INTERVAL)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    private fun callCertainFunction() {
        // Your function logic here
        //Initiate JSON Obj
        val jsonObject = JSONObject()
        val requestBody :RequestBody = jsonObject.toString().toRequestBody(mediaType)


        //Make Request Using Retrofit
        val request : Request = Request.Builder().url(validateTokenURL).post(requestBody).addHeader("Authorization", "Bearer ${sharedPreferences.getString("token","")}").build();
        val response = client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                handler.post{
                    Toast.makeText(applicationContext, "Failed to re-login", Toast.LENGTH_SHORT).show()
                }


            }

            override fun onResponse(call: Call, response: Response) {

                response.use {
                    if(response.isSuccessful){
                        response.body?.let {
                            val responseBodyString = it.string()
                            val jsonObject = JSONObject(responseBodyString)

                            val exp = jsonObject.getString("exp")

                            if (isTokenExpired(exp.toLong())){
                                val email = sharedPreferences.getString("email","")
                                val password = sharedPreferences.getString("password","")
                                var isRememberTemp = sharedPreferences.getString("isRemember","")
                                var isRemember = false
                                if (isRememberTemp == "true"){
                                    isRemember = true
                                }else{
                                    isRemember = false
                                }
                                it.close()
                                if (email != null && password != null) {
                                    post(email,password,isRemember)
                                }
                            }else{
                                handler.post{
                                    Toast.makeText(applicationContext,"Token is not exp", Toast.LENGTH_SHORT).show()
                                }

                                return
                            }
                        } ?: run {

                        }

                    }else{
                        handler.post{
                            Toast.makeText(applicationContext,"Failed to check token, server error", Toast.LENGTH_SHORT).show()
                        }
                    }

                }

            }
        })


    }
    fun isTokenExpired(expirationTime: Long): Boolean {
        val currentTimeSeconds = Instant.now().epochSecond
        return currentTimeSeconds >= expirationTime
    }

    private fun post(email: String, password: String, isRemember:Boolean){

        //Initiate JSON Obj
        val jsonObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("password", password)
        val requestBody :RequestBody = jsonObject.toString().toRequestBody(mediaType)

        //Make Request Using Okhttp
        val request :Request = Request.Builder().url(validateTokenURL).post(requestBody).build();
        val response = client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {

                handler.post{
                    Toast.makeText(applicationContext, "Failed to re-login", Toast.LENGTH_SHORT).show()
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

                            handler.post{
                                Toast.makeText(applicationContext, "Re-login success", Toast.LENGTH_SHORT).show()
                            }

                        } ?: run {
                            handler.post{
                                Toast.makeText(applicationContext, "Server error", Toast.LENGTH_SHORT).show()
                            }

                        }

                    }else{
                        handler.post{
                            Toast.makeText(applicationContext, "Wrong credentials", Toast.LENGTH_SHORT).show()
                        }

                    }

                }

            }
        })


    }

}

