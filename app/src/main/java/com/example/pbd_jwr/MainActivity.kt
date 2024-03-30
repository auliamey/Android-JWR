package com.example.pbd_jwr

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Button
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.pbd_jwr.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val keyGenParameterSpec = KeyGenParameterSpec.Builder(
        "login",
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(256)
        .build()

    private lateinit var masterKeyAlias: MasterKey
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: Editor
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
}