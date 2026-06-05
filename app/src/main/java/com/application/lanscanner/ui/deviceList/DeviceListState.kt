package com.application.lanscanner.ui.deviceList

import com.application.lanscanner.data.model.LanDevice

sealed class DeviceListState {
    object Idle : DeviceListState()
    object Loading : DeviceListState()
    data class Success(val devices: List<LanDevice>) : DeviceListState()
    data class Error(val message: String) : DeviceListState()
}
