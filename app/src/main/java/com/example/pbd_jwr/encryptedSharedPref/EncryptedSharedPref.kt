package com.example.pbd_jwr.encryptedSharedPref

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object EncryptedSharedPref {

    fun create(context: Context, sharedPrefName: String): SharedPreferences {
        // Create or retrieve the master key for encryption
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            sharedPrefName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        val masterKeyAlias = MasterKey.Builder(context, "login")
            .setKeyGenParameterSpec(keyGenParameterSpec)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            sharedPrefName,
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )


        // Create EncryptedSharedPreferences instance
        return EncryptedSharedPreferences.create(
            context,
            sharedPrefName,  // Name of the shared preferences file
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

}