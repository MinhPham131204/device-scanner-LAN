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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.application.lanscanner.utils.NetworkUtils
import com.application.lanscanner.utils.rememberWifiConnectivityState

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
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "wifi_check_route" // Đổi đích đến mặc định
    ) {

        // --- Màn hình 1: Kiểm tra Wi-Fi ---
        composable("wifi_check_route") {
            WifiCheckScreen(
                onScanClick = {
                    // Chuyển sang màn hình danh sách thiết bị
                    navController.navigate("device_list_route") {
                        // Tùy chọn UX: Xóa màn hình check Wifi khỏi lịch sử (Backstack).
                        // Nhờ vậy khi user bấm nút "Back" trên điện thoại, app sẽ thoát luôn
                        // chứ không quay ngược lại màn hình chữ "Detect WiFi" nữa.
                        popUpTo("wifi_check_route") { inclusive = true }
                    }
                }
            )
        }

        // --- Màn hình 2: Danh sách thiết bị (Giữ nguyên như cũ) ---
        composable("device_list_route") {
            DeviceListApp(
                onNavigateToPortScanner = { targetIp ->
                    navController.navigate("port_scanner_route/$targetIp")
                },
                onBackClick = {
                    // Điều hướng quay về màn hình Wi-Fi, đồng thời xóa màn hình hiện tại khỏi bộ nhớ để giải phóng tài nguyên.
                    navController.navigate("wifi_check_route") {
                        popUpTo("device_list_route") { inclusive = true }
                    }
                }
            )
        }

        // --- Màn hình 3: Quét Port (Giữ nguyên) ---
        composable("port_scanner_route/{ip}") { backStackEntry ->
            val ipToScan = backStackEntry.arguments?.getString("ip") ?: ""
            // PortScannerScreen(ip = ipToScan)
        }
    }
}

@Composable
fun DeviceListApp(onNavigateToPortScanner: (String) -> Unit, onBackClick: () -> Unit) {
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
                    onDeviceClick = { },
                    onBackClick = onBackClick
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
                },
                onBackClick = onBackClick
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

@Composable
fun WifiCheckScreen(onScanClick: () -> Unit) {
    // Gọi hàm lắng nghe Wifi ở trên
    val isWifiConnected by rememberWifiConnectivityState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // Giữ tông nền tối của app
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            if (!isWifiConnected) {
                Text(
                    text = "The device is not connected to any WiFi network",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, // Đã sửa cú pháp đúng
                    modifier = Modifier.padding(horizontal = 24.dp) // Thêm chút padding cho đẹp
                )
            } else {
                Text(
                    text = "Detect WiFi",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onScanClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3), // Màu nền của nút
                        contentColor = Color.White          // Màu chữ và icon bên trong nút
                    )
                ) {
                    Text("Scan devices")
                }
            }

        }
    }
}