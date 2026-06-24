package com.application.lanscanner.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class LocationResult(
    val address: String,     // Chứa "Can Tho, VN"
    val coordinates: String  // Chứa "10.0371,105.7883"
)

object LocationHelper {

    /**
     * Trả về chuỗi dạng "City, Country, Location"
     * Nếu lỗi mạng, trả về "Unknown Location"
     */
    suspend fun getPublicLocation(): LocationResult = withContext(Dispatchers.IO) {
        try {
            // Đổi sang endpoint của ipinfo.io
            val url = URL("https://ipinfo.io/json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonObject = JSONObject(response.toString())

                val city = jsonObject.optString("region", "Unknown City")
                val country = jsonObject.optString("country", "Unknown Country")
                val loc = jsonObject.optString("loc", "")

                return@withContext LocationResult(
                    address = "$city, $country",
                    coordinates = loc
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext LocationResult("Unknown Location", "")
    }
}