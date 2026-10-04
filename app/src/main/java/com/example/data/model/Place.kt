package com.example.data.model

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Place(
    val id: String,
    val name: String,
    val category: PlaceCategory,
    val description: String,
    val address: String,
    val area: String,
    val baseDistanceKm: Double,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val openingHours: String,
    val isOpen: Boolean,
    val tags: List<String> = emptyList(),
    val rating: Double = 4.5,
    val reviewCount: Int = 30,
    val services: List<String> = emptyList()
) {
    /**
     * Calculates distance using the Haversine formula if area coordinates are known,
     * otherwise falls back to area-based calculation.
     */
    fun calculateDistanceForArea(userArea: String, userLat: Double? = null, userLng: Double? = null): Double {
        if (userLat != null && userLng != null) {
            val r = 6371.0 // Earth radius in kilometers
            val dLat = Math.toRadians(latitude - userLat)
            val dLon = Math.toRadians(longitude - userLng)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(userLat)) * cos(Math.toRadians(latitude)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            val dist = r * c
            return Math.round(dist * 10.0) / 10.0
        }

        return if (area.equals(userArea, ignoreCase = true)) {
            baseDistanceKm
        } else {
            val isSameTown = area.substringBefore(",").trim().equals(userArea.substringBefore(",").trim(), ignoreCase = true)
            if (isSameTown) {
                baseDistanceKm + 1.8
            } else {
                // Approximate regional distances in Mpumalanga
                baseDistanceKm + 28.5
            }
        }
    }

    /**
     * Generates a Google Maps URL for directions / search.
     */
    fun getGoogleMapsSearchUrl(): String {
        val query = URLEncoder.encode("$name, $address", StandardCharsets.UTF_8.toString())
        return "https://www.google.com/maps/search/?api=1&query=$query"
    }

    /**
     * Generates a Google Maps turn-by-turn navigation or directions URL.
     */
    fun getGoogleMapsDirectionsUrl(travelMode: String = "driving"): String {
        val dest = URLEncoder.encode("$latitude,$longitude", StandardCharsets.UTF_8.toString())
        return "https://www.google.com/maps/dir/?api=1&destination=$dest&travelmode=$travelMode"
    }

    /**
     * Generates the Android Google Maps turn-by-turn navigation intent uri string.
     */
    fun getGoogleNavigationUri(mode: String = "d"): String {
        return "google.navigation:q=$latitude,$longitude&mode=$mode"
    }
}
