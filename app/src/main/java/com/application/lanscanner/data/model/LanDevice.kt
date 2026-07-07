package com.application.lanscanner.data.model

data class LanDevice(
    val ipAddress: String,
    val name: String, // Ex: "Generic", "samsung SM-A156E"
    val macAddress: String? = null,
    val os: String? = null,
    val brand: String? = null, // "Samsung"
    val model: String? = null, // "Galaxy A15 5G"
    val deviceType: DeviceType = DeviceType.GENERIC,
    val isOnline: Boolean = true
)

enum class DeviceType { ROUTER, PHONE, GENERIC }