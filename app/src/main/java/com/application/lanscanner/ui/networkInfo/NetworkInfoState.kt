package com.application.lanscanner.ui.networkInfo

data class NetworkInfoState(
    val subnetName: String = "",      // Ex: Net 192.168.1.0/24
    val location: String = "",        // Ex: Can Tho, Việt Nam
    val coordinates: String = "",
    val isWifi: Boolean = true,
    val onlineDevices: Int = 0,                  // Number of online devices
    val totalDevices: Int = 0,                   // Amount of scanned devices
    val bssid: String = "02:00:00:00:00:00",     // access point
    val netmask: String = "255.255.255.0",       // Netmask
    val gatewayIp: String = "192.168.1.1",       // Gateway (IP)
    val gatewayMac: String = "02:00:00:00:00:00",// Gateway (MAC)
    val dnsServers: String = "8.8.8.8"           // DNS
)