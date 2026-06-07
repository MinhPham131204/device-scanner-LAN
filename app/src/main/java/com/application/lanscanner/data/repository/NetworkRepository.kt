package com.application.lanscanner.data.repository

import com.application.lanscanner.data.dataSource.coreScanner.PingScanner
import com.application.lanscanner.data.model.LanDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class NetworkRepository(
    private val pingScanner: PingScanner
) {
    fun scanLanDevices(networkAddress: String, numOfHosts: Int): Flow<LanDevice> {
        return pingScanner.scanSubnetRealtime(networkAddress, numOfHosts)
            .map { activeIp ->
                // Biến đổi (Map) dữ liệu thô thành Model của ứng dụng
                LanDevice(
                    ipAddress = activeIp,
                    name = "Generic"
                )
            }
            // Đảm bảo toàn bộ quá trình xử lý data diễn ra trên luồng nền (IO Thread)
            // giúp giao diện UI không bị đơ giật.
            .flowOn(Dispatchers.IO)
    }
}