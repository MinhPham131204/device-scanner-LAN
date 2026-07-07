package com.application.lanscanner.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberWifiConnectivityState(): State<Boolean> {
    val context = LocalContext.current
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // default state
    val isConnected = remember { mutableStateOf(false) }

    DisposableEffect(connectivityManager) {
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        isConnected.value = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        // listen state change when app is running
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnected.value = true
            }
            override fun onLost(network: Network) {
                isConnected.value = false
            }
        }

        // only sign up for notifications from Wifi
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Unsubscribe when this screen is closed.
        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    return isConnected
}