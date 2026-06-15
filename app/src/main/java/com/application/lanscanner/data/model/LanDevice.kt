package com.application.lanscanner.data.model

data class LanDevice(
    val ipAddress: String,
    val name: String, // VD: "Bộ định tuyến", "Chung", "samsung SM-A156E"
    val macAddress: String? = null,
    val os: String? = null,
    val brand: String? = null, // VD: "Samsung"
    val model: String? = null, // VD: "Galaxy A15 5G"
    val deviceType: DeviceType = DeviceType.GENERIC,
    val isOnline: Boolean = true
)

enum class DeviceType { ROUTER, PHONE, GENERIC }