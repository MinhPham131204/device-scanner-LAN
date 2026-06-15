package com.application.lanscanner.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import java.net.Inet4Address
import kotlin.experimental.and

// Data class chứa kết quả trả về
data class NetworkDetails(
    val baseIp: String,         // VD: "192.168.1" (Dùng cho vòng lặp PingScanner)
    val prefixLength: Int,      // VD: 24
)

object NetworkUtils {

    /**
     * Lấy thông tin mạng LAN hiện tại của thiết bị
     */
    fun getLocalNetworkDetails(context: Context): NetworkDetails? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Lấy mạng đang active (thường là Wi-Fi)
        val network = connectivityManager.activeNetwork ?: return null
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return null

        // Đảm bảo thiết bị đang kết nối Wi-Fi hoặc Ethernet (bỏ qua 4G/5G vì không quét LAN được)
        if (!networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
            !networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return null
        }

        val linkProperties: LinkProperties = connectivityManager.getLinkProperties(network) ?: return null

        // Duyệt qua các địa chỉ IP của thiết bị (tìm IPv4)
        for (linkAddress in linkProperties.linkAddresses) {
            val inetAddress = linkAddress.address

            // Chỉ lấy địa chỉ IPv4 và bỏ qua Loopback (127.0.0.1)
            if (inetAddress is Inet4Address && !inetAddress.isLoopbackAddress) {

                val deviceIp = inetAddress.hostAddress ?: continue
                val prefixLength = linkAddress.prefixLength // VD: 24

                // Tính toán địa chỉ Subnet gốc (Network Address) bằng Bitwise AND
                val ipBytes = inetAddress.address
                val maskBytes = calculateMaskBytes(prefixLength)

                val networkBytes = ByteArray(4)
                for (i in 0..3) {
                    // AND từng byte của IP với Subnet Mask
                    networkBytes[i] = (ipBytes[i] and maskBytes[i])
                }

                // Chuyển mảng byte thành chuỗi IP (VD: 192.168.1.0)
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
     * Hàm hỗ trợ: Chuyển đổi Prefix (VD: 24) thành mảng byte Subnet Mask (VD: 255.255.255.0)
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