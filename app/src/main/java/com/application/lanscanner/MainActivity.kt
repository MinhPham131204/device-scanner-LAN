package com.application.lanscanner

import FingDeviceListScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.application.lanscanner.ui.deviceList.DeviceListState
import com.application.lanscanner.ui.deviceList.DeviceListViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Thay MaterialTheme bằng Theme ứng dụng của bạn nếu có (VD: NetworkScannerTheme)
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    // Dùng màu đen làm nền mặc định để khớp với giao diện Fing
                    color = Color.Black
                ) {
                    DeviceListApp()
                }
            }
        }
    }
}

@Composable
fun DeviceListApp() {
    // 1. Khởi tạo ViewModel (được giữ nguyên qua các lần xoay màn hình)
    val viewModel: DeviceListViewModel = viewModel()

    // 2. Lắng nghe trạng thái StateFlow từ ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // 3. Phân nhánh UI dựa trên trạng thái hiện tại
    when (val state = uiState) {
        is DeviceListState.Idle -> {
            // Trạng thái chờ khởi tạo
        }

        is DeviceListState.Loading -> {
            // Vẫn hiển thị giao diện Fing nhưng với danh sách rỗng để tránh màn hình bị giật (nháy)
            Box(modifier = Modifier.fillMaxSize()) {
                FingDeviceListScreen(
                    devices = emptyList(),
                    onUpdateClick = { /* Không làm gì khi đang tải */ }
                )

                // Hiển thị vòng xoay tải đè lên trên
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color(0xFF2196F3)) // FingBlue
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Đang quét mạng LAN...", color = Color.White)
                }
            }
        }

        is DeviceListState.Success -> {
            // Hiển thị danh sách thiết bị khi có dữ liệu
            FingDeviceListScreen(
                devices = state.devices,
                onUpdateClick = {
                    viewModel.startScan() // Gọi lại hàm quét khi bấm CẬP NHẬT
                }
            )
        }

        is DeviceListState.Error -> {
            // Xử lý khi có lỗi mạng
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Lỗi: ${state.message}",
                    color = Color.Red
                )
            }
        }
    }
}