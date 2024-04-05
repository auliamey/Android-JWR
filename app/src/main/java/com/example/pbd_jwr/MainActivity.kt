package com.example.pbd_jwr

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.net.ConnectivityManager
import android.os.Bundle
import android.content.pm.PackageManager
import android.widget.Toast
import android.Manifest
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts

import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pbd_jwr.backgroundService.JWTValidationService
import com.example.pbd_jwr.data.entity.Transaction
import com.example.pbd_jwr.data.model.Category
import com.example.pbd_jwr.databinding.ActivityMainBinding
import com.example.pbd_jwr.encryptedSharedPref.EncryptedSharedPref
import com.example.pbd_jwr.network.NetworkCallbackImplementation
import com.example.pbd_jwr.ui.transaction.TransactionViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: Editor

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: NetworkCallbackImplementation

    private lateinit var mTransactionViewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = EncryptedSharedPref.create(applicationContext,"login")
        sharedPreferencesEditor = sharedPreferences.edit()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isLocationPermissionGranted()) {
            requestLocationPermission()
        }

        val navView: BottomNavigationView = binding.navView
        navView.background=null

        mTransactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_transaction, R.id.navigation_dashboard, R.id.navigation_settings, R.id.navigation_transaction_add, R.id.navigation_transaction_detail, R.id.navigation_twibbon, R.id.navigation_scan
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val fab: FloatingActionButton = binding.fabScan
        fab.setOnClickListener {
            // Navigate ScanFragment
            navController.navigate(R.id.navigation_scan)
            val menuItem = navView.menu.findItem(R.id.navigation_scan)
            menuItem.isChecked = true

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
                R.id.navigation_scan->{
                    navController.navigate(R.id.navigation_scan)
                    true
                }
                else -> false
            }
        }

    }

    override fun onStart() {
        super.onStart()

    }

    override fun onResume() {
        super.onResume()
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = NetworkCallbackImplementation(this)
        registerNetworkCallback()
        val serviceIntent = Intent(this, JWTValidationService::class.java)
        startService(serviceIntent)
    }
    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

//     Request location permission
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, save the status
                saveLocationPermissionGranted()
            } else {
                // Permission denied, inform the user
                Toast.makeText(
                    this,
                    "Location permission denied. Some functionality may be limited.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveLocationPermissionGranted() {
        val sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("location_permission_granted", true)
        editor.apply()
    }

    override fun onPause() {
        super.onPause()
        unregisterNetworkCallback()
        val serviceIntent = Intent(this, JWTValidationService::class.java)
        stopService(serviceIntent)
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    private fun registerNetworkCallback() {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    private fun unregisterNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}