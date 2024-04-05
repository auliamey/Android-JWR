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
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts

import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
import org.json.JSONObject
import java.util.Date
import kotlin.math.roundToInt
import androidx.appcompat.widget.Toolbar
import com.example.pbd_jwr.ui.transaction.TransactionAddFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: Editor

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: NetworkCallbackImplementation

    private lateinit var mTransactionViewModel: TransactionViewModel

    private lateinit var receiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serviceIntent = Intent(this, JWTValidationService::class.java)
        startService(serviceIntent)

        // Inisialisasi receiver
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "com.example.pbd_jwr.RANDOMIZE_TRANSACTION") {
                    val sharedPreferences = context.getSharedPreferences("randomize_data", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("randomize_intent_received", true)
                    editor.apply()

                }
            }
        }

        val filter = IntentFilter().apply {
            addAction("com.example.pbd_jwr.RANDOMIZE_TRANSACTION")
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

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
                R.id.navigation_transaction, R.id.navigation_dashboard, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        val fab: FloatingActionButton = binding.fabScan
        fab.setOnClickListener {
            // Start ScanActivity
            val intent = Intent(this, ScanActivity::class.java)
            startScanActivityForResult.launch(intent)
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

    override fun onStart() {
        super.onStart()

    }

    override fun onResume() {
        super.onResume()
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = NetworkCallbackImplementation(this)
        registerNetworkCallback()
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

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
    }

    override fun onDestroy() {
        super.onDestroy()
        val serviceIntent = Intent(this, JWTValidationService::class.java)
        stopService(serviceIntent)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    private fun registerNetworkCallback() {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    private fun unregisterNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private val startScanActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            sharedPreferences = EncryptedSharedPref.create(applicationContext, "login")
            val currentUserEmail = sharedPreferences.getString("email", "") ?: ""

            val data = result.data
            val transactionDummyData = data?.getStringExtra("transactionDummyData")

            transactionDummyData?.let {

                val jsonObject = JSONObject(transactionDummyData)
                val itemsArray = jsonObject.getJSONObject("items").getJSONArray("items")

                for (i in 0 until itemsArray.length()) {
                    val itemObject = itemsArray.getJSONObject(i)
                    val name = itemObject.getString("name")
                    val category = Category.EXPENSE
                    val price = itemObject.getDouble("price")
                    val qty = itemObject.getInt("qty")
                    val amount = (qty * price * 1000).roundToInt() / 1000.0
                    val latitude = 6.8915
                    val longitude = 107.6107
                    val location = "Latitude: $latitude, Longitude: $longitude"
                    val date = Date().time

                    mTransactionViewModel.addTransaction(Transaction(email = currentUserEmail, title = name, category = category, amount = amount, latitude = latitude, longitude = longitude, date = date))
                }


            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}