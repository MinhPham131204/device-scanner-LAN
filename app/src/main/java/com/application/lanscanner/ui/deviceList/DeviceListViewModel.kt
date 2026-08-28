package com.application.lanscanner.ui.deviceList

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.lanscanner.data.dataSource.coreScanner.PingScanner
import com.application.lanscanner.data.repository.NetworkRepository
import com.application.lanscanner.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

class DeviceListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DeviceListState>(DeviceListState.Idle)
    val uiState: StateFlow<DeviceListState> = _uiState.asStateFlow()

    private val pingScanner = PingScanner()
    private val networkRepository = NetworkRepository(pingScanner)

    fun startScan(context: Context) {
        if (_uiState.value is DeviceListState.Loading) return

        viewModelScope.launch {
            try {
                val networkInfo = NetworkUtils.getLocalNetworkDetails(context)
                if (networkInfo == null) {
                    _uiState.value = DeviceListState.Error("Không tìm thấy kết nối mạng.")
                    return@launch
                }

                val calculatedSubnet = NetworkUtils.getSubnetName(networkInfo)
                val numOfHosts = 1 shl (32 - networkInfo.prefixLength)

                _uiState.value = DeviceListState.Loading(
                    devices = emptyList(),
                    subnetName = calculatedSubnet
                )

                networkRepository.scanLanDevices(networkInfo.baseIp, numOfHosts, context)
                    .collect { newDevice ->
                        _uiState.update { currentState ->
                            if (currentState is DeviceListState.Loading) {
                                currentState.copy(devices = currentState.devices + newDevice)
                            } else {
                                currentState
                            }
                        }
                    }

                // scan completed => convert state to success
                _uiState.update { currentState ->
                    if (currentState is DeviceListState.Loading) {
                        DeviceListState.Success(
                            devices = currentState.devices,
                            subnetName = currentState.subnetName
                        )
                    } else currentState
                }

            } catch (e: Exception) {
                _uiState.value = DeviceListState.Error(e.message ?: "Lỗi khi quét mạng.")
            }
        }
    }
}