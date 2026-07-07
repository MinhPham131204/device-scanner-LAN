package com.application.lanscanner.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import java.net.Inet4Address
import kotlin.experimental.and

data class NetworkDetails(
    val baseIp: String,         // Ex: "192.168.1"
    val prefixLength: Int,      // Ex: 24
)

object NetworkUtils {

    /**
     * Get LAN info
     */
    fun getLocalNetworkDetails(context: Context): NetworkDetails? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return null
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return null

        // Ignore 4G/5G
        if (!networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
            !networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return null
        }

        val linkProperties: LinkProperties = connectivityManager.getLinkProperties(network) ?: return null

        // Traverse all active IP addresses of devices
        for (linkAddress in linkProperties.linkAddresses) {
            val inetAddress = linkAddress.address

            // only get IPv4 addresses and ignore Loopback (127.0.0.1)
            if (inetAddress is Inet4Address && !inetAddress.isLoopbackAddress) {

                val prefixLength = linkAddress.prefixLength // VD: 24

                // Compute Subnet (Network Address) by Bitwise AND
                val ipBytes = inetAddress.address
                val maskBytes = calculateMaskBytes(prefixLength)

                val networkBytes = ByteArray(4)
                for (i in 0..3) {
                    // AND every byte of IP address with Subnet Mask
                    networkBytes[i] = (ipBytes[i] and maskBytes[i])
                }

                // Convert byte array to IP string (Ex: 192.168.1.0)
                val networkAddress = "${networkBytes[0].toUByte()}.${networkBytes[1].toUByte()}.${networkBytes[2].toUByte()}.${networkBytes[3].toUByte()}"

                return NetworkDetails(
                    baseIp = networkAddress,
                    prefixLength = prefixLength,
                )
            }
        }
        return null
    }

    /**
     * Helper function: Convert Prefix (Ex: 24) to byte array Subnet Mask (Ex: 255.255.255.0)
     */
    private fun calculateMaskBytes(prefixLength: Int): ByteArray {
        val mask = -1 shl (32 - prefixLength) // Dịch bit để tạo mask int
        return byteArrayOf(
            (mask ushr 24 and 0xFF).toByte(),
            (mask ushr 16 and 0xFF).toByte(),
            (mask ushr 8 and 0xFF).toByte(),
            (mask and 0xFF).toByte()
        )
    }

    fun getSubnetName(networkDetails: NetworkDetails): String {
        return "Net ${networkDetails.baseIp}/${networkDetails.prefixLength}"
    }

    fun ipToLong(ipAddress: String): Long {
        val octets = ipAddress.split(".")
        if (octets.size != 4) return 0L

        var result = 0L
        result = result or (octets[0].toLong() shl 24)
        result = result or (octets[1].toLong() shl 16)
        result = result or (octets[2].toLong() shl 8)
        result = result or octets[3].toLong()

        return result
    }

    fun longToIp(ip: Long): String {
        return "${(ip ushr 24) and 0xFF}.${(ip ushr 16) and 0xFF}.${(ip ushr 8) and 0xFF}.${ip and 0xFF}"
    }
}