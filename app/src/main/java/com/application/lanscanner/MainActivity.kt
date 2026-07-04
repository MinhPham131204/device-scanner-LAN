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
import androidx.lifecycle.ViewModel
import com.application.lanscanner.data.repository.AppDatabase
import com.application.lanscanner.data.repository.PortRepository
import com.application.lanscanner.ui.portScanner.PortScannerScreen
import com.application.lanscanner.ui.portScanner.PortScannerViewModel
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

        // --- Screen 1: Check Wifi state ---
        composable("wifi_check_route") {
            WifiCheckScreen(
                onScanClick = {
                    // navigate to devices list screen
                    navController.navigate("device_list_route") {
                        popUpTo("wifi_check_route") { inclusive = true }
                    }
                }
            )
        }

        // --- Screen 2: Devices list ---
        composable("device_list_route") {
            DeviceListApp(
                onNavigateToPortScanner = { targetIp ->
                    navController.navigate("port_scanner_route/$targetIp")
                },
                onBackClick = {
                    // Back to screen 1
                    navController.navigate("wifi_check_route") {
                        popUpTo("device_list_route") { inclusive = true }
                    }
                }
            )
        }

        // --- Screen 3: Port scanning ---
        composable("port_scanner_route/{ip}") { backStackEntry ->
            val ipToScan = backStackEntry.arguments?.getString("ip") ?: ""
            val context = LocalContext.current

            val database = AppDatabase.getDatabase(context)
            val repository = PortRepository(database.ianaPortDao())

            val portViewModel: PortScannerViewModel = viewModel {
                PortScannerViewModel(repository)
            }

            LaunchedEffect(ipToScan) {
                portViewModel.initTarget(ipToScan)
            }

            PortScannerScreen(
                viewModel = portViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun DeviceListApp(onNavigateToPortScanner: (String) -> Unit, onBackClick: () -> Unit) {
    val viewModel: DeviceListViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    val networkInfo = NetworkUtils.getLocalNetworkDetails(context)

    LaunchedEffect(Unit) {
        if (viewModel.uiState.value is DeviceListState.Idle) {
            viewModel.startScan(context)
        }
    }

    when (val state = uiState) {

        is DeviceListState.Idle -> {
            Box(modifier = Modifier.fillMaxSize()) {
                FingDeviceListScreen(
                    devices = emptyList(),
                    subnetName = NetworkUtils.getSubnetName(networkInfo!!),
                    isScanning = true,
                    onUpdateClick = { viewModel.startScan(context) },
                    onDeviceClick = { },
                    onBackClick = onBackClick
                )

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color(0xFF2196F3)) // loading spinner
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Retrieving info...", color = Color.White)
                }
            }
        }

        is DeviceListState.Loading -> {
            FingDeviceListScreen(
                devices = state.devices,
                subnetName = state.subnetName,
                isScanning = true,
                onUpdateClick = { viewModel.startScan(context) },
                onDeviceClick = {},
                onBackClick = onBackClick
            )
        }

        is DeviceListState.Success -> {
            FingDeviceListScreen(
                devices = state.devices,
                subnetName = state.subnetName,
                isScanning = false,
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
                    Text(text = "Error: ${state.message}", color = Color.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.startScan(context) }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
fun WifiCheckScreen(onScanClick: () -> Unit) {
    val isWifiConnected by rememberWifiConnectivityState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            if (!isWifiConnected) {
                Text(
                    text = "The device is not connected to any WiFi network",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
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
                        containerColor = Color(0xFF2196F3),
                        contentColor = Color.White
                    )
                ) {
                    Text("Scan devices")
                }
            }

        }
    }
}