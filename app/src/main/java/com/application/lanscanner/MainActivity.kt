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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.application.lanscanner.ui.deviceList.DeviceListState
import com.application.lanscanner.ui.deviceList.DeviceListViewModel
import androidx.compose.material3.CircularProgressIndicator
import com.application.lanscanner.utils.NetworkUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    NetworkScannerApp();
                }
            }
        }
    }
}

@Composable
fun NetworkScannerApp() {
    // 1. Khởi tạo bộ điều khiển điều hướng
    val navController = rememberNavController()

    // 2. Thiết lập NavHost với startDestination chỉ định màn hình đầu tiên
    NavHost(
        navController = navController,
        startDestination = "device_list_route" // Màn hình này sẽ hiện ra đầu tiên
    ) {

        // --- CÁC MÀN HÌNH TRONG APP ---

        // Màn hình 1: Danh sách thiết bị
        composable("device_list_route") {
            DeviceListApp(
                onNavigateToPortScanner = { targetIp ->
                    // Cách chuyển sang màn hình khác kèm theo dữ liệu (IP)
                    navController.navigate("port_scanner_route/$targetIp")
                }
            )
        }

        // Màn hình 2: Quét Port (Dự trù cho tính năng tiếp theo)
        composable("port_scanner_route/{ip}") { backStackEntry ->
            val ipToScan = backStackEntry.arguments?.getString("ip") ?: ""
            // Tạm thời hiển thị chữ để test chuyển màn hình
            // PortScannerScreen(ip = ipToScan)
        }

    }
}

@Composable
fun DeviceListApp(onNavigateToPortScanner: (String) -> Unit) {
    val viewModel: DeviceListViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // 1. Lấy Context trực tiếp từ Jetpack Compose
    val context = LocalContext.current

    val networkInfo = NetworkUtils.getLocalNetworkDetails(context)

    // 2. Tự động kích hoạt quét mạng ngay khi UI vừa được nạp lên
    LaunchedEffect(Unit) {
        if (viewModel.uiState.value is DeviceListState.Idle) {
            viewModel.startScan(context)
        }
    }

    // 3. Phân nhánh UI
    when (val state = uiState) {

        // Gộp chung Idle và Loading để luôn hiển thị khung giao diện thay vì màn hình đen
        is DeviceListState.Idle, is DeviceListState.Loading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                // Vẫn vẽ giao diện Fing nhưng với danh sách rỗng
                FingDeviceListScreen(
                    devices = emptyList(),
                    subnetName = NetworkUtils.getSubnetName(networkInfo!!),
                    onUpdateClick = { viewModel.startScan(context) },
                    onDeviceClick = { }
                )

                // Vẽ vòng xoay đè lên giữa màn hình
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color(0xFF2196F3))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Đang lấy thông tin mạng...", color = Color.White)
                }
            }
        }

        is DeviceListState.Success -> {
            FingDeviceListScreen(
                devices = state.devices,
                subnetName = NetworkUtils.getSubnetName(networkInfo!!), // Hiển thị tên Subnet thực tế
                onUpdateClick = { viewModel.startScan(context) },
                onDeviceClick = { clickedDevice ->
                    onNavigateToPortScanner(clickedDevice.ipAddress)
                }
            )
        }

        is DeviceListState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Lỗi: ${state.message}", color = Color.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.startScan(context) }) {
                        Text("Thử lại")
                    }
                }
            }
        }
    }
}