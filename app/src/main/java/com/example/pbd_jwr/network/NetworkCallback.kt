package com.example.pbd_jwr.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi

class NetworkCallbackImplementation(private val context: Context) : ConnectivityManager.NetworkCallback() {
    private var firstLoad = true
    override fun onAvailable(network: Network) {
        super.onAvailable(network)

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        if(!firstLoad){
            capabilities?.let {
                if (it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                    Toast.makeText(context, "Currently using wi-fi", Toast.LENGTH_LONG).show()

                    // Handle WiFi network availability here
                } else if (it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Toast.makeText(context, "Currently using mobile-data", Toast.LENGTH_LONG).show()
                    // Handle cellular network availability here
                } else {
                    Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show()

                }
            }
        }else{
            firstLoad = false
        }
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        Toast.makeText(context, "No internet", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "NetworkCallbackImpl"
    }
}



