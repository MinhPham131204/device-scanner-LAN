package com.application.lanscanner.ui.portScanner

data class PortUiModel(
    val portNumber: Int,
    val serviceName: String,
    val description: String
)

data class PortScannerState(
    val targetIp: String = "",
    val isScanning: Boolean = false,
    val scannedCount: Int = 0,
    val totalPortsToScan: Int = 65535,
    val openPorts: List<PortUiModel> = emptyList()
)