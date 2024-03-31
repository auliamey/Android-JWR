package com.example.pbd_jwr

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.net.ConnectivityManager
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.pbd_jwr.backgroundService.JWTValidationService
import com.example.pbd_jwr.databinding.ActivityMainBinding
import com.example.pbd_jwr.encryptedSharedPref.EncryptedSharedPref
import com.example.pbd_jwr.network.NetworkCallbackImplementation
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: Editor

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: NetworkCallbackImplementation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, JWTValidationService::class.java)
        startService(serviceIntent)

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = NetworkCallbackImplementation(this)
        registerNetworkCallback()

        sharedPreferences = EncryptedSharedPref.create(applicationContext,"login")
        sharedPreferencesEditor = sharedPreferences.edit()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        navView.background=null;

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_transaction, R.id.navigation_dashboard,R.id.navigation_scan, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        val fab: FloatingActionButton = binding.fabScan
        fab.setOnClickListener {
            navController.navigate(R.id.navigation_scan)
        }
        navView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_transaction -> {
                    // Handle transaction navigation
                    navController.navigate(R.id.navigation_transaction)
                    true
                }
                R.id.navigation_dashboard -> {
                    // Handle dashboard navigation
                    navController.navigate(R.id.navigation_dashboard)
                    true
                }
                R.id.navigation_settings -> {
                    // Handle notifications navigation
                    navController.navigate(R.id.navigation_settings)

                    true
                }
                R.id.logout -> {
                    // Handle your button click here
                    sharedPreferencesEditor.clear().putString("isRemember","false").apply()

                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        val serviceIntent = Intent(this, JWTValidationService::class.java)
        stopService(serviceIntent)
        unregisterNetworkCallback()

    }

    private fun registerNetworkCallback() {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    private fun unregisterNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}