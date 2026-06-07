package com.application.lanscanner.data.dataSource.coreScanner

import com.application.lanscanner.utils.NetworkUtils.ipToLong
import com.application.lanscanner.utils.NetworkUtils.longToIp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PingScanner {
    fun scanSubnetRealtime(networkAddress: String, numOfHosts: Int): Flow<String> = channelFlow {
        val startIpLong = ipToLong(networkAddress)

        (1 until numOfHosts - 1).forEach { i ->
            launch(Dispatchers.IO) {
                // Tính toán IP của thiết bị đích dưới dạng số
                val currentIpLong = startIpLong + i

                // Dịch ngược từ số về lại chuỗi IPv4 (VD: "192.168.1.5")
                val targetIp = longToIp(currentIpLong)

                val isAlive = pingIpAddress(targetIp)

                if (isAlive) {
                    send(targetIp)
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
}