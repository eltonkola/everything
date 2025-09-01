package com.eltonkola.everything.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// Existing models for reverse geocoding
@Serializable
data class NominatimResponse(
    @SerialName("address")
    val address: Address? = null,
    @SerialName("display_name")
    val displayName: String? = null
)

@Serializable
data class Address(
    @SerialName("city")
    val city: String? = null,
    @SerialName("town")
    val town: String? = null,
    @SerialName("village")
    val village: String? = null,
    @SerialName("country")
    val country: String? = null
)

// New models for search functionality
@Serializable
data class SearchResult(
    @SerialName("display_name")
    val displayName: String,
    @SerialName("lat")
    val lat: String,
    @SerialName("lon")
    val lon: String,
    @SerialName("importance")
    val importance: Double? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("class")
    val placeClass: String? = null
) {
    // Convenience properties for easier use
    val latitude: Double get() = lat.toDoubleOrNull() ?: 0.0
    val longitude: Double get() = lon.toDoubleOrNull() ?: 0.0
}

class CustomHttpLogger(): Logger {
    override fun log(message: String) {
        println("GeocodingService $message" ) // Or whatever logging system you want here
    }
}


class GeocodingService {

    // This sets up the HTTP client for making network requests.
    // It's configured to automatically parse JSON responses.
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true // Safely ignore fields in the JSON we don't need
                isLenient = true
            })
        }
        install(Logging) {
            logger = CustomHttpLogger()
            level = LogLevel.ALL
        }
    }

    /**
     * Fetches a human-readable place name for a given latitude and longitude.
     * This is a suspend function, so it must be called from a coroutine.
     */
    suspend fun getPlaceName(lat: Double, lon: Double): String? {
        val url = "https://nominatim.openstreetmap.org/reverse"
        return try {
            // Make the GET request to the Nominatim API
            val response: NominatimResponse = httpClient.get(url) {
                parameter("format", "jsonv2")
                parameter("lat", lat)
                parameter("lon", lon)
                parameter("zoom", 10) // Use zoom=10 to prioritize city/town names
            }.body()

            // Intelligently pick the most relevant name from the response
            response.address?.let {
                it.city ?: it.town ?: it.village ?: it.country
            }
        } catch (e: Exception) {
            // If the network fails or there's an error, print it and return null
            println("Geocoding failed: ${e.message}")
            null
        }
    }

    /**
     * Searches for locations matching the given query string.
     * Returns a list of search results sorted by relevance/importance.
     */
    suspend fun searchLocations(
        query: String,
        limit: Int = 10,
        countryCode: String? = null
    ): List<SearchResult> {
        if (query.isBlank()) return emptyList()

        val url = "https://nominatim.openstreetmap.org/search"
        return try {
            val jsonString: String = httpClient.get(url) {
                parameter("format", "json")
                parameter("q", query.trim())
                parameter("limit", limit)
                parameter("addressdetails", 1)
                parameter("extratags", 1)

                // Optional country filtering
                countryCode?.let { parameter("countrycodes", it) }
            }.bodyAsText()

            // Manually parse the JSON array
            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
            val results = json.decodeFromString<List<SearchResult>>(jsonString)

            // Sort by importance (higher is better) and return
            results.sortedByDescending { it.importance ?: 0.0 }
        } catch (e: Exception) {
            println("Location search failed: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Searches for locations near a specific coordinate.
     * Useful for "search around me" functionality.
     */
    suspend fun searchNearby(
        query: String,
        lat: Double,
        lon: Double,
        radiusKm: Int = 50,
        limit: Int = 10
    ): List<SearchResult> {
        if (query.isBlank()) return emptyList()

        val url = "https://nominatim.openstreetmap.org/search"
        return try {
            val jsonString: String = httpClient.get(url) {
                parameter("format", "json")
                parameter("q", query.trim())
                parameter("limit", limit)
                parameter("bounded", 1)

                // Create a bounding box around the coordinates
                val latOffset = radiusKm * 0.009 // Rough conversion: 1 degree ≈ 111km
                val lonOffset = radiusKm * 0.009

                parameter("viewbox", "${lon - lonOffset},${lat + latOffset},${lon + lonOffset},${lat - latOffset}")
            }.bodyAsText()

            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
            val results = json.decodeFromString<List<SearchResult>>(jsonString)
            println("results: $results")
            results.sortedByDescending { it.importance ?: 0.0 }
        } catch (e: Exception) {
            println("Nearby search failed: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }


    /**
     * Searches for locations within the current map viewport.
     * This is perfect for "search what I'm looking at" functionality.
     */
    suspend fun searchInViewport(
        query: String,
        west: Double,
        north: Double,
        east: Double,
        south: Double,
        limit: Int = 10
    ): List<SearchResult> {
        if (query.isBlank()) return emptyList()

        val url = "https://nominatim.openstreetmap.org/search"
        return try {
            val jsonString: String = httpClient.get(url) {
                parameter("format", "json")
                parameter("q", query.trim())
                parameter("limit", limit)
                parameter("bounded", 1) // Only search within the bounds
                parameter("viewbox", "$west,$north,$east,$south")
            }.bodyAsText()

            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
            val results = json.decodeFromString<List<SearchResult>>(jsonString)
            results.sortedByDescending { it.importance ?: 0.0 }
        } catch (e: Exception) {
            println("Viewport search failed: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Clean up resources when done
     */
    fun close() {
        httpClient.close()
    }
}