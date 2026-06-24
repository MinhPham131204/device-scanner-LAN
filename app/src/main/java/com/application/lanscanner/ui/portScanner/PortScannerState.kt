package com.application.lanscanner.ui.portScanner

data class PortUiModel(
    val portNumber: Int,
    val serviceName: String,
    val description: String
)

// Class đại diện cho toàn bộ trạng thái của màn hình
data class PortScannerState(
    val targetIp: String = "",
    val isScanning: Boolean = false,
    val scannedCount: Int = 0,
    val totalPortsToScan: Int = 1000, // Quét 1000 port phổ biến hoặc 65535 tùy cấu hình
    val openPorts: List<PortUiModel> = emptyList()
)