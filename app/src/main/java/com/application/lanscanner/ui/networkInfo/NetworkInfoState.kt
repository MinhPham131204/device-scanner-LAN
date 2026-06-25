package com.application.lanscanner.ui.networkInfo

data class NetworkInfoState(
    val subnetName: String = "",      // VD: Net 192.168.1.0/24
    val location: String = "",        // VD: Can Tho, Việt Nam
    val coordinates: String = "",
    val isWifi: Boolean = true,
    val onlineDevices: Int = 0,                  // Số thiết bị đang online
    val totalDevices: Int = 0,                   // Tổng số thiết bị quét được
    val bssid: String = "02:00:00:00:00:00",     // Điểm truy cập
    val netmask: String = "255.255.255.0",       // Cài đặt mạng -> Netmask
    val gatewayIp: String = "192.168.1.1",       // Cổng (IP)
    val gatewayMac: String = "02:00:00:00:00:00",// Cổng (MAC)
    val dnsServers: String = "8.8.8.8"           // DNS
)