package com.application.lanscanner.ui.deviceList

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.lanscanner.data.dataSource.coreScanner.PingScanner
import com.application.lanscanner.data.model.LanDevice
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

    // Khởi tạo Repository (Sau này nếu dùng Hilt/Dagger, bạn sẽ Inject nó vào constructor)
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

                // Tính toán tên mạng CHỈ 1 LẦN tại đây
                val calculatedSubnet = NetworkUtils.getSubnetName(networkInfo)
                val numOfHosts = 1 shl (32 - networkInfo.prefixLength)

                // Gán giá trị khởi tạo
                _uiState.value = DeviceListState.Loading(
                    devices = emptyList(),
                    subnetName = calculatedSubnet
                )

                networkRepository.scanLanDevices(networkInfo.baseIp, numOfHosts)
                    .collect { newDevice ->
                        // SỬ DỤNG HÀM UPDATE ĐỂ ĐẢM BẢO AN TOÀN ĐA LUỒNG
                        _uiState.update { currentState ->
                            if (currentState is DeviceListState.Loading) {
                                // Tạo list mới dựa trên list cũ + thiết bị mới, ép Compose phải vẽ lại
                                currentState.copy(devices = currentState.devices + newDevice)
                            } else {
                                currentState
                            }
                        }
                    }

                // Chuyển sang Success khi quét xong
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