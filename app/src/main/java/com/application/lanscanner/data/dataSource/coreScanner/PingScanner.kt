package com.application.lanscanner.data.dataSource.coreScanner

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.application.lanscanner.utils.MDnsParser
import com.application.lanscanner.utils.NetworkUtils.ipToLong
import com.application.lanscanner.utils.NetworkUtils.longToIp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

data class PingResult(
    val ipAddress: String,
    val hostname: String
)

class PingScanner {
    fun scanSubnetRealtime(networkAddress: String, numOfHosts: Int, context: Context): Flow<PingResult> = channelFlow {
        val startIpLong = ipToLong(networkAddress)

        // create a Thread-safe Set that store IP of found devices
        val discoveredIps = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

        // Lưu ý: Cần truyền 'context' vào hàm hoặc class của bạn
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val multicastLock = wifiManager.createMulticastLock("LANScanner_mDNS")
        multicastLock.setReferenceCounted(true)

        // Khóa sóng Wifi để bắt đầu hứng gói tin Multicast
        multicastLock.acquire()

        try {
            withTimeoutOrNull(20_000L) {
                try {
                    // Initialize the socket and subscribe device in IPv4 mDNS group.
                    MulticastSocket(5353).use { socket ->
                        val multicastGroup = InetAddress.getByName("224.0.0.251")
                        socket.joinGroup(multicastGroup)
                        socket.soTimeout = 20000

                        val buffer = ByteArray(4096)

                        // sniff packets in 20 seconds
                        while (isActive) {
                            val packet = DatagramPacket(buffer, buffer.size)
                            socket.receive(packet) // stop thread to sniff packet

                            val senderIp = packet.address.hostAddress ?: continue

                            if (isIpInSubnet(senderIp, startIpLong, numOfHosts) && !discoveredIps.contains(senderIp)) {
                                discoveredIps.add(senderIp)

                                val extractedName = MDnsParser.extractHostname(packet.data, packet.length)

                                val resolvedName = extractedName ?: getHostName(senderIp)

                                // send to Kotlin Flow
                                send(PingResult(ipAddress = senderIp, hostname = resolvedName))
                            }
                        }
                        socket.leaveGroup(multicastGroup)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } finally {
            // CỰC KỲ QUAN TRỌNG: Luôn nhả khóa khi kết thúc để tránh gây hao pin
            if (multicastLock.isHeld) {
                multicastLock.release()
            }
        }

        // Ping the remaining IPs (not in the set).
        (1 until numOfHosts - 1).forEach { i ->
            val currentIpLong = startIpLong + i
            val targetIp = longToIp(currentIpLong)

            if (!discoveredIps.contains(targetIp)) {
                launch(Dispatchers.IO) {
                    val isAlive = pingIpAddress(targetIp)

                    if (isAlive) {
                        val resolvedName = getHostName(targetIp)

                        send(PingResult(ipAddress = targetIp, hostname = resolvedName))
                    }
                }
            }
        }
    }

    /**
     * check IP is in active IP range of LAN or not.
     */
    private fun isIpInSubnet(ip: String, startLong: Long, numOfHosts: Int): Boolean {
        return try {
            val ipLong = ipToLong(ip)
            ipLong in (startLong + 1) until (startLong + numOfHosts - 1)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Run Ping command
     */
    private suspend fun pingIpAddress(ipAddress: String): Boolean = withContext(Dispatchers.IO) {
        try {
            runInterruptible {
                val command = "/system/bin/ping -c 1 -W 1 $ipAddress"
                val process = Runtime.getRuntime().exec(command)
                val exitValue = process.waitFor()
                exitValue == 0
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            false
        }
    }

    /**
     * using Reverse DNS to get hostname of IP address
     */
    private suspend fun getHostName(ipAddress: String): String = withContext(Dispatchers.IO) {
        try {
            runInterruptible {
                val inetAddress = InetAddress.getByName(ipAddress)
                val hostname = inetAddress.hostName

                if (hostname == ipAddress) {
                    "Generic"
                } else {
                    hostname
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            "Generic"
        }
    }
}