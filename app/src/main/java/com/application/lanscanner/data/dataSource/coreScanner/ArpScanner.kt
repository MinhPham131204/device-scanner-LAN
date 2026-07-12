package com.application.lanscanner.data.dataSource.coreScanner

import com.application.lanscanner.utils.NetworkUtils.pingIpAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader

class ArpScanner {
    /**
     * get mac address function
     */
    suspend fun getMacAddress(targetIp: String): String? = withContext(Dispatchers.IO) {
        // lookup target ip in arp table first
        var macAddress = readArpTableForIp(targetIp)

        // if MAC address is not exist => ping ip
        if (macAddress == null) {
            pingIpAddress(targetIp)

            // lookup ARP table again to get MAC address
            macAddress = readArpTableForIp(targetIp)
        }

        return@withContext macAddress
    }

    /**
     * read file /proc/net/arp (arp table) to find MAC address.
     * File structure: IP address | HW type | Flags | HW address | Mask | Device
     */
    private fun readArpTableForIp(targetIp: String): String? {
        try {
            BufferedReader(FileReader("/proc/net/arp")).use { reader ->
                var line: String?

                // ignore first header line
                reader.readLine()

                while (reader.readLine().also { line = it } != null) {
                    val tokens = line!!.trim().split(Regex("\\s+"))

                    if (tokens.size >= 4) {
                        val ip = tokens[0]
                        val flag = tokens[2]
                        val mac = tokens[3]

                        if (ip == targetIp) {
                            if (flag.contains("2") && mac != "00:00:00:00:00:00") { // flags = 0x2
                                return mac
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}