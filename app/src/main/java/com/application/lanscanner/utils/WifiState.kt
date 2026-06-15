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

    // Trạng thái mặc định ban đầu
    val isConnected = remember { mutableStateOf(false) }

    DisposableEffect(connectivityManager) {
        // 1. Kiểm tra trạng thái ngay lúc vừa mở app
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        isConnected.value = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        // 2. Lắng nghe sự thay đổi (Bật/Tắt Wifi) trong lúc app đang chạy
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnected.value = true
            }
            override fun onLost(network: Network) {
                isConnected.value = false
            }
        }

        // Chỉ đăng ký nhận thông báo từ mạng Wi-Fi
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Hủy đăng ký khi màn hình này bị đóng để tránh rò rỉ bộ nhớ
        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    return isConnected
}