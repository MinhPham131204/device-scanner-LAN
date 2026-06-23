package com.application.lanscanner.data.dataSource.database.iana_ports

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

// Data class đại diện cho 1 cổng
data class IanaPort(
    val serviceName: String,
    val portNumber: String, // Dùng String vì có những dải port như "1024-1025"
    val description: String
)

object IanaPortDb {

    // Regex bóc tách CSV chuẩn: Chỉ cắt dấu phẩy nằm ngoài màng bọc ngoặc kép
    private val csvRegex = Regex(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")

    /**
     * Hàm gọi API và bóc tách CSV trên luồng nền (IO)
     */
    suspend fun fetchAndParse(): List<IanaPort> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<IanaPort>()
        val seenPorts = mutableSetOf<String>()

        try {
            val url = URL("https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.csv")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000 // Timeout 10 giây
            connection.readTimeout = 15000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->

                    // 1. Đọc và bỏ qua dòng đầu tiên (Dòng tiêu đề Header)
                    reader.readLine()

                    var line: String?
                    // 2. Đọc từng dòng cho đến hết file (Streaming)
                    while (reader.readLine().also { line = it } != null) {
                        if (line!!.isNotBlank()) {

                            // 3. Cắt chuỗi và loại bỏ dấu ngoặc kép bọc ngoài
                            val columns = line!!.split(csvRegex).map {
                                it.trim().removeSurrounding("\"")
                            }

                            // IANA CSV có cấu trúc: [0]Service Name, [1]Port Number, [2]Protocol, [3]Description...
                            if (columns.size >= 4) {
                                val serviceName = columns[0]
                                val portNumber = columns[1]
                                val description = columns[3]

                                // 4. Lọc dữ liệu rác
                                // Bỏ qua các dòng không có port cụ thể hoặc port chưa được gán (Unassigned)
                                if (portNumber.isNotBlank() && !description.equals("Unassigned", ignoreCase = true)) {
                                    // Hàm add() của Set sẽ trả về TRUE nếu số port này CHƯA TỪNG tồn tại trong Set
                                    if (seenPorts.add(portNumber)) {
                                        resultList.add(
                                            IanaPort(
                                                serviceName = serviceName.ifBlank { "Unknown Service" },
                                                portNumber = portNumber,
                                                description = description
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Có thể quăng exception ra ngoài hoặc xử lý lỗi mạng tại đây
        }

        // Loại bỏ các dòng bị trùng lặp (vì IANA thường ghi 2 dòng cho cùng 1 port, 1 cho TCP và 1 cho UDP)
        return@withContext resultList
    }
}