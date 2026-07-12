package com.application.lanscanner.data.dataSource.coreScanner

import com.application.lanscanner.utils.NetworkUtils.ipToLong
import com.application.lanscanner.utils.NetworkUtils.longToIp
import com.application.lanscanner.utils.NetworkUtils.pingIpAddress
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

                val currentIpLong = startIpLong + i

                val targetIp = longToIp(currentIpLong)

                val isAlive = pingIpAddress(targetIp)

                if (isAlive) {
                    val resolvedName = getHostName(targetIp)

                    // send IP address + Hostname to Kotlin Flow
                    send(PingResult(ipAddress = targetIp, hostname = resolvedName))
                }
            }
        }
    }

    /**
     * using Reverse DNS to get hostname of IP address
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