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
    private val csvRegex = Regex(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")

    /**
     * API calling & parse CSV function, run on background thread (IO)
     */
    suspend fun fetchAndParse(): List<IanaPort> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<IanaPort>()
        val seenPorts = mutableSetOf<String>()

        try {
            val url = URL("https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.csv")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 15000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->

                    reader.readLine()

                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        if (line!!.isNotBlank()) {

                            val columns = line!!.split(csvRegex).map {
                                it.trim().removeSurrounding("\"")
                            }

                            if (columns.size >= 4) {
                                val serviceName = columns[0]
                                val portNumber = columns[1]
                                val description = columns[3]

                                if (portNumber.isNotBlank() && !description.equals("Unassigned", ignoreCase = true)) {

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

        }

        return@withContext resultList
    }
}