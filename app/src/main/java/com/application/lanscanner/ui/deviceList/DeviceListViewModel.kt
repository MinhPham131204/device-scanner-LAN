package com.application.lanscanner.ui.deviceList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.lanscanner.data.model.DeviceType
import com.application.lanscanner.data.model.LanDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceListViewModel : ViewModel() {

    // 1. Trạng thái nội bộ (_uiState): Có thể thay đổi, chỉ ViewModel mới được phép ghi
    private val _uiState = MutableStateFlow<DeviceListState>(DeviceListState.Idle)

    // 2. Trạng thái công khai (uiState): View (Compose) chỉ được phép đọc (Observe)
    val uiState: StateFlow<DeviceListState> = _uiState.asStateFlow()

    init {
        // Tự động bắt đầu quét khi ViewModel được khởi tạo (khi mở màn hình)
        startScan()
    }

    fun startScan() {
        // Ngăn chặn việc bấm quét nhiều lần cùng lúc
        if (_uiState.value is DeviceListState.Loading) return

        _uiState.value = DeviceListState.Loading

        // Khởi chạy Coroutine gắn với vòng đời của ViewModel
        viewModelScope.launch {
            try {
                // TODO: Sau này bạn sẽ gọi NetworkRepository ở đây
                // val devices = repository.scanLanNetwork("192.168.1.0/24")

                // --- BẮT ĐẦU DỮ LIỆU GIẢ LẬP (Mock Data) ---
                delay(2000) // Giả lập độ trễ quét mạng mất 2 giây

                val mockDevices = listOf(
                    LanDevice("192.168.1.1", "Bộ định tuyến", deviceType = DeviceType.ROUTER),
                    LanDevice("192.168.1.5", "Chung"),
                    LanDevice("192.168.1.85", "Chung"),
                    LanDevice("192.168.1.115", "Chung"),
                    LanDevice("192.168.1.223", "samsung SM-A156E", brand = "Samsung", model = "Galaxy A15 5G", deviceType = DeviceType.PHONE),
                )
                // --- KẾT THÚC DỮ LIỆU GIẢ LẬP ---

                // Trả kết quả thành công về cho UI
                _uiState.value = DeviceListState.Success(mockDevices)

            } catch (e: Exception) {
                // Xử lý nếu có lỗi mạng
                _uiState.value = DeviceListState.Error(e.message ?: "Đã xảy ra lỗi khi quét LAN")
            }
        }
    }
}