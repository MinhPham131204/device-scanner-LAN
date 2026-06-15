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

class DeviceListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DeviceListState>(DeviceListState.Idle)
    val uiState: StateFlow<DeviceListState> = _uiState.asStateFlow()

    // Khởi tạo Repository (Sau này nếu dùng Hilt/Dagger, bạn sẽ Inject nó vào constructor)
    private val pingScanner = PingScanner()
    private val networkRepository = NetworkRepository(pingScanner)

    fun startScan(context: Context) {
        if (_uiState.value is DeviceListState.Loading) return
        _uiState.value = DeviceListState.Loading

        viewModelScope.launch {
            try {
                // 1. Lấy thông tin mạng (Subnet, Base IP, v.v.)
                val networkInfo = NetworkUtils.getLocalNetworkDetails(context)

                if (networkInfo == null) {
                    _uiState.value = DeviceListState.Error("Không tìm thấy kết nối mạng LAN (Wi-Fi/Ethernet).")
                    return@launch
                }

                val numOfHosts = 1 shl (32 - networkInfo.prefixLength)

                val currentDevices = mutableListOf<LanDevice>()

                // 2. Gọi Repository để bắt đầu hứng dữ liệu quét được
                networkRepository.scanLanDevices(networkInfo.baseIp, numOfHosts)
                    .collect { newDevice ->
                        // Thêm thiết bị mới vào danh sách
                        currentDevices.add(newDevice)

                        // Cập nhật lại trạng thái UI với danh sách mới nhất
                        _uiState.value = DeviceListState.Success(
                            devices = currentDevices.toList(),
                        )
                    }

            } catch (e: Exception) {
                _uiState.value = DeviceListState.Error(e.message ?: "Lỗi không xác định khi quét mạng.")
            }
        }
    }
}