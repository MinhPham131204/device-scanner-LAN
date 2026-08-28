package com.application.lanscanner.ui.deviceList

import com.application.lanscanner.data.model.LanDevice

sealed class DeviceListState {
    object Idle : DeviceListState()
    data class Loading(
        val devices: List<LanDevice> = emptyList(),
        val subnetName: String = "Unknown"
    ) : DeviceListState()

    data class Success(
        val devices: List<LanDevice>,
        val subnetName: String
    ) : DeviceListState()
    data class Error(val message: String) : DeviceListState()
}
