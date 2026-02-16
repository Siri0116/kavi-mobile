package com.kavi.mobile.services

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

/**
 * Weather Service - Fetches weather information
 * Uses OpenWeatherMap API as primary source, falls back to web search
 */
class WeatherService(private val context: Context) {

    companion object {
        private const val TAG = "WeatherService"
        // You can get a free API key from https://openweathermap.org/api
        private const val API_KEY = "YOUR_API_KEY_HERE" // TODO: Replace with actual API key
        private const val BASE_URL = "https://api.openweathermap.org/data/2.5/weather"
    }

    data class WeatherInfo(
        val temperature: Double,
        val feelsLike: Double,
        val description: String,
        val humidity: Int,
        val windSpeed: Double,
        val city: String,
        val country: String
    )

    /**
     * Get current weather for user's location
     */
    suspend fun getCurrentWeather(): WeatherInfo? = withContext(Dispatchers.IO) {
        try {
            val location = getLastKnownLocation()
            if (location != null) {
                getWeatherByCoordinates(location.latitude, location.longitude)
            } else {
                // Fallback to default city or return null
                Log.w(TAG, "Location not available")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current weather", e)
            null
        }
    }

    /**
     * Get weather for a specific city
     */
    suspend fun getWeatherByCity(cityName: String): WeatherInfo? = withContext(Dispatchers.IO) {
        try {
            if (API_KEY == "YOUR_API_KEY_HERE") {
                Log.w(TAG, "API key not configured, using fallback")
                return@withContext null
            }

            val encodedCity = URLEncoder.encode(cityName, "UTF-8")
            val urlString = "$BASE_URL?q=$encodedCity&appid=$API_KEY&units=metric"
            
            val response = URL(urlString).readText()
            parseWeatherResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting weather for city: $cityName", e)
            null
        }
    }

    /**
     * Get weather by coordinates
     */
    private suspend fun getWeatherByCoordinates(lat: Double, lon: Double): WeatherInfo? = withContext(Dispatchers.IO) {
        try {
            if (API_KEY == "YOUR_API_KEY_HERE") {
                Log.w(TAG, "API key not configured, using fallback")
                return@withContext null
            }

            val urlString = "$BASE_URL?lat=$lat&lon=$lon&appid=$API_KEY&units=metric"
            
            val response = URL(urlString).readText()
            parseWeatherResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting weather by coordinates", e)
            null
        }
    }

    /**
     * Parse OpenWeatherMap API response
     */
    private fun parseWeatherResponse(jsonResponse: String): WeatherInfo {
        val json = JSONObject(jsonResponse)
        
        val main = json.getJSONObject("main")
        val weather = json.getJSONArray("weather").getJSONObject(0)
        val wind = json.getJSONObject("wind")
        val sys = json.getJSONObject("sys")
        
        return WeatherInfo(
            temperature = main.getDouble("temp"),
            feelsLike = main.getDouble("feels_like"),
            description = weather.getString("description"),
            humidity = main.getInt("humidity"),
            windSpeed = wind.getDouble("speed"),
            city = json.getString("name"),
            country = sys.getString("country")
        )
    }

    /**
     * Get last known location
     */
    private fun getLastKnownLocation(): Location? {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            
            // Try GPS first
            val gpsLocation = try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } catch (e: SecurityException) {
                Log.w(TAG, "GPS location permission not granted")
                null
            }
            
            // Fallback to network location
            val networkLocation = try {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (e: SecurityException) {
                Log.w(TAG, "Network location permission not granted")
                null
            }
            
            // Return the most recent location
            when {
                gpsLocation != null && networkLocation != null -> {
                    if (gpsLocation.time > networkLocation.time) gpsLocation else networkLocation
                }
                gpsLocation != null -> gpsLocation
                networkLocation != null -> networkLocation
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location", e)
            null
        }
    }

    /**
     * Format weather info as a natural language response
     */
    fun formatWeatherResponse(weather: WeatherInfo): String {
        val temp = weather.temperature.toInt()
        val feelsLike = weather.feelsLike.toInt()
        
        return buildString {
            append("The weather in ${weather.city} is ${weather.description}. ")
            append("Temperature is $temp degrees Celsius")
            
            if (Math.abs(temp - feelsLike) > 3) {
                append(", but feels like $feelsLike degrees")
            }
            
            append(". Humidity is ${weather.humidity} percent")
            
            if (weather.windSpeed > 5) {
                append(" with wind speed of ${weather.windSpeed.toInt()} meters per second")
            }
            
            append(".")
        }
    }

    /**
     * Get simple weather description for voice response
     */
    fun getSimpleWeatherResponse(weather: WeatherInfo): String {
        val temp = weather.temperature.toInt()
        
        return when {
            temp < 10 -> "It's quite cold at $temp degrees. You should wear warm clothes."
            temp < 20 -> "It's cool at $temp degrees. A light jacket would be good."
            temp < 30 -> "It's pleasant at $temp degrees. Perfect weather!"
            else -> "It's hot at $temp degrees. Stay hydrated!"
        }
    }
}
