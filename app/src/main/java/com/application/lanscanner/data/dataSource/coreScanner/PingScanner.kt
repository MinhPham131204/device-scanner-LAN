package com.application.lanscanner.data.dataSource.coreScanner

import com.application.lanscanner.utils.NetworkUtils.ipToLong
import com.application.lanscanner.utils.NetworkUtils.longToIp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

data class PingResult(
    val ipAddress: String,
    val hostname: String
)

class PingScanner {
    fun scanSubnetRealtime(networkAddress: String, numOfHosts: Int): Flow<PingResult> = channelFlow {
        val startIpLong = ipToLong(networkAddress)

        (1 until numOfHosts - 1).forEach { i ->
            launch(Dispatchers.IO) {
                // Tính toán IP của thiết bị đích dưới dạng số
                val currentIpLong = startIpLong + i

                // Dịch ngược từ số về lại chuỗi IPv4 (VD: "192.168.1.5")
                val targetIp = longToIp(currentIpLong)

                val isAlive = pingIpAddress(targetIp)

                if (isAlive) {
                    // Kích hoạt lấy hostname NGAY SAU KHI biết thiết bị đang online
                    val resolvedName = getHostName(targetIp)

                    // Gửi cả IP và Hostname lên kênh
                    send(PingResult(ipAddress = targetIp, hostname = resolvedName))
                }
            }
        }
    }

    /**
     * Thực thi lệnh Ping cấp thấp của hệ điều hành.
     */
    private suspend fun pingIpAddress(ipAddress: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val command = "/system/bin/ping -c 1 -W 1 $ipAddress"
            val process = Runtime.getRuntime().exec(command)

            val exitValue = process.waitFor()

            return@withContext exitValue == 0
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    /**
     * Dùng Reverse DNS để hỏi Router xem IP này tên là gì.
     */
    private suspend fun getHostName(ipAddress: String): String = withContext(Dispatchers.IO) {
        try {
            val inetAddress = InetAddress.getByName(ipAddress)
            val hostname = inetAddress.hostName

            if (hostname == ipAddress) {
                return@withContext "Generic"
            }
            return@withContext hostname
        } catch (e: Exception) {
            return@withContext "Generic"
        }
    }
}