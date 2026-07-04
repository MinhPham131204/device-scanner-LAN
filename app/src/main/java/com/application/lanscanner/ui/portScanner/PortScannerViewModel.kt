package com.application.lanscanner.ui.portScanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.lanscanner.data.repository.PortRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.coroutines.cancellation.CancellationException

class PortScannerViewModel(
    private val portRepository: PortRepository
) : ViewModel() {

    private var scanJob: Job? = null
    private val _uiState = MutableStateFlow(PortScannerState())
    val uiState: StateFlow<PortScannerState> = _uiState.asStateFlow()

    // Hàm gọi khi vừa điều hướng sang màn hình này
    fun initTarget(ip: String) {
        _uiState.update { it.copy(targetIp = ip) }
    }

    fun toggleScan() {
        if (_uiState.value.isScanning) {
            stopScan()
        } else {
            startScan()
        }
    }

    private fun startScan() {
        val targetIp = _uiState.value.targetIp
        if (targetIp.isBlank()) return

        _uiState.update {
            it.copy(isScanning = true, openPorts = emptyList(), scannedCount = 0)
        }

        scanJob = viewModelScope.launch(Dispatchers.IO) {
            val ianaDb = portRepository.getIanaPorts()
            val semaphore = Semaphore(50)
            val portsToScan = 1..65535
            _uiState.update { it.copy(totalPortsToScan = portsToScan.last) }

            // Giới hạn tổng thời gian quét (Ví dụ: 30 giây)
            val scanTimeoutMillis = 30_000L

            withTimeoutOrNull(scanTimeoutMillis) {

                coroutineScope {
                    portsToScan.forEach { port ->

                        // Nếu người dùng chủ động bấm nút "DỪNG", thoát khỏi vòng lặp nạp việc
                        if (!_uiState.value.isScanning) return@coroutineScope

                        launch {
                            semaphore.withPermit {

                                val isOpen = checkPortOpen(targetIp, port)

                                _uiState.update { it.copy(scannedCount = it.scannedCount + 1) }

                                if (isOpen) {
                                    val dbInfo = ianaDb.find { it.portNumber == port.toString() }
                                    val newPort = PortUiModel(
                                        portNumber = port,
                                        serviceName = dbInfo?.serviceName ?: "unknown",
                                        description = dbInfo?.description ?: "Unknown Service"
                                    )

                                    _uiState.update { currentState ->
                                        currentState.copy(
                                            openPorts = (currentState.openPorts + newPort).sortedBy { it.portNumber }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            _uiState.update { it.copy(isScanning = false) }
        }
    }

    private fun stopScan() {
        _uiState.update { it.copy(isScanning = false) }

        scanJob?.cancel()
    }

    // Hàm mở Socket cực nhanh (Timeout 300ms)
    private suspend fun checkPortOpen(ip: String, port: Int): Boolean {
        return try {
            runInterruptible {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(ip, port), 1000)
                    true
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            false
        }
    }
}