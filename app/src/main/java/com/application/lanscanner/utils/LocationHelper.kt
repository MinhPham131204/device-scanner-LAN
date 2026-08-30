package com.application.lanscanner.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class LocationResult(
    val address: String,     // "Can Tho, VN"
    val isp_org: String,
    val coordinates: String  // "10.0371,105.7883"
)

object LocationHelper {

    /**
     * Return string "City, Country, Location"
     * If network error, return "Unknown Location"
     */
    suspend fun getPublicLocation(): LocationResult = withContext(Dispatchers.IO) {
        try {
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

                val isp_org = jsonObject.optString("org", "Unknown org.").replaceFirst(
                    Regex("^AS\\d+\\s+"), // ignore "(AS...)" string
                    ""
                )

                val city = jsonObject.optString("region", "Unknown City").replace(
                    Regex("\\s*\\([^()]*\\)$"), // ignore (...) string
                    ""
                )
                val country = jsonObject.optString("country", "Unknown Country")
                val loc = jsonObject.optString("loc", "")

                return@withContext LocationResult(
                    address = "$city, $country",
                    isp_org,
                    coordinates = loc
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext LocationResult("Unknown Location", "", "")
    }
}