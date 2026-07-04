package com.application.lanscanner.ui.networkInfo

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import com.application.lanscanner.utils.LocationHelper
import com.application.lanscanner.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NetworkInfoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NetworkInfoState())
    val uiState: StateFlow<NetworkInfoState> = _uiState.asStateFlow()

    suspend fun fetchNetworkDetails(context: Context, deviceCount: Int) {

        val networkInfo = NetworkUtils.getLocalNetworkDetails(context)

        if (networkInfo == null) {
            _uiState.update { currentState ->
                currentState.copy(
                    subnetName = "Không tìm thấy mạng",
                    location = "Offline",
                    coordinates = "",
                    bssid = "--:--:--:--:--:--",
                    netmask = "---",
                    gatewayIp = "---",
                    dnsServers = "---",
                    onlineDevices = 0,
                    totalDevices = 0
                )
            }
            return // Cực kỳ quan trọng: Lệnh này ngăn code chạy tiếp xuống bên dưới
        }

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcpInfo = wifiManager.dhcpInfo
        val wifiInfo = wifiManager.connectionInfo

        // Chuyển đổi IP từ Int sang chuỗi String (Ví dụ: 192.168.1.1)
        val gatewayIpStr = intToIp(dhcpInfo.gateway)
        val netmaskStr = intToIp(dhcpInfo.netmask)
        val dnsStr = intToIp(dhcpInfo.dns1)

        val subnetName = NetworkUtils.getSubnetName(networkInfo)

        val locationData = LocationHelper.getPublicLocation()

        // Cập nhật State
        _uiState.update { currentState ->
            currentState.copy(
                subnetName = subnetName,
                location = locationData.address,
                coordinates = locationData.coordinates,
                bssid = wifiInfo.bssid ?: "02:00:00:00:00:00",
                netmask = netmaskStr,
                gatewayIp = "$gatewayIpStr (02:00:00:00:00:00)",
                dnsServers = dnsStr,
                onlineDevices = deviceCount,
                totalDevices = deviceCount
            )
        }
    }

    // Hàm phụ trợ dịch ngược IP tĩnh của Android
    private fun intToIp(ipInt: Int): String {
        return "${ipInt and 0xFF}.${ipInt shr 8 and 0xFF}.${ipInt shr 16 and 0xFF}.${ipInt shr 24 and 0xFF}"
    }
}