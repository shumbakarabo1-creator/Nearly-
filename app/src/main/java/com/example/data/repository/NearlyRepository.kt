package com.example.data.repository

import com.example.data.MockData
import com.example.data.local.dao.NearlyDao
import com.example.data.local.entity.SavedPlaceEntity
import com.example.data.local.entity.SearchHistoryEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.Place
import com.example.data.model.PlaceCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class NearlyRepository(
    private val dao: NearlyDao
) {
    val allMockPlaces: List<Place> = MockData.PLACES
    val availableAreas: List<String> = MockData.AVAILABLE_AREAS

    // Smart Natural Language Search & Filtering
    fun searchPlaces(
        query: String,
        category: PlaceCategory?,
        maxDistanceKm: Double?,
        openNowOnly: Boolean,
        userArea: String
    ): List<Place> {
        val trimmedQuery = query.trim().lowercase()

        // Extract key search terms and common stop words filter
        val stopWords = setOf(
            "i", "need", "where", "can", "to", "a", "an", "the", "for", "my", "me",
            "nearby", "closest", "around", "in", "is", "there", "someone", "buy", "find"
        )
        val queryTokens = trimmedQuery
            .split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.isNotBlank() && !stopWords.contains(it) }

        val coords = MockData.getAreaCoords(userArea)
        return allMockPlaces.mapNotNull { place ->
            val distance = place.calculateDistanceForArea(userArea, coords.first, coords.second)

            // Category filter check
            if (category != null && place.category != category) {
                return@mapNotNull null
            }

            // Open now check
            if (openNowOnly && !place.isOpen) {
                return@mapNotNull null
            }

            // Max distance check
            if (maxDistanceKm != null && distance > maxDistanceKm) {
                return@mapNotNull null
            }

            // If no search query, match with default relevance
            if (trimmedQuery.isEmpty()) {
                return@mapNotNull Pair(place, 1.0 / (distance + 0.1))
            }

            // Calculate relevance score based on query & tokens
            var score = 0.0

            // Direct full phrase matching
            val nameLower = place.name.lowercase()
            val descLower = place.description.lowercase()
            val catLower = place.category.displayName.lowercase()

            if (nameLower.contains(trimmedQuery)) score += 50.0
            if (descLower.contains(trimmedQuery)) score += 20.0
            if (catLower.contains(trimmedQuery)) score += 30.0

            // Check specific intent keywords
            if (trimmedQuery.contains("phone charger") || trimmedQuery.contains("charger") || trimmedQuery.contains("cable")) {
                if (place.category == PlaceCategory.ELECTRONICS) score += 40.0
            }
            if (trimmedQuery.contains("print") || trimmedQuery.contains("documents") || trimmedQuery.contains("copy") || trimmedQuery.contains("scan")) {
                if (place.category == PlaceCategory.PRINTING) score += 40.0
            }
            if (trimmedQuery.contains("fix") || trimmedQuery.contains("repair") || trimmedQuery.contains("screen")) {
                if (place.category == PlaceCategory.REPAIRS) score += 40.0
            }
            if (trimmedQuery.contains("haircut") || trimmedQuery.contains("barber") || trimmedQuery.contains("beard") || trimmedQuery.contains("fade")) {
                if (place.category == PlaceCategory.BEAUTY) score += 40.0
            }
            if (trimmedQuery.contains("school") || trimmedQuery.contains("supplies") || trimmedQuery.contains("textbook") || trimmedQuery.contains("stationery")) {
                if (place.category == PlaceCategory.EDUCATION || place.category == PlaceCategory.SHOPPING) score += 40.0
            }
            if (trimmedQuery.contains("groceries") || trimmedQuery.contains("food") || trimmedQuery.contains("lunch") || trimmedQuery.contains("coffee")) {
                if (place.category == PlaceCategory.FOOD) score += 40.0
            }
            if (trimmedQuery.contains("doctor") || trimmedQuery.contains("pharmacy") || trimmedQuery.contains("medicine") || trimmedQuery.contains("flu")) {
                if (place.category == PlaceCategory.HEALTH) score += 40.0
            }

            // Check tags and services
            for (tag in place.tags) {
                val tagLower = tag.lowercase()
                if (tagLower == trimmedQuery) score += 35.0
                else if (tagLower.contains(trimmedQuery) || trimmedQuery.contains(tagLower)) score += 20.0
            }

            for (service in place.services) {
                val serviceLower = service.lowercase()
                if (serviceLower.contains(trimmedQuery) || trimmedQuery.contains(serviceLower)) score += 15.0
            }

            // Token-level matching
            for (token in queryTokens) {
                if (token.length >= 3) {
                    if (nameLower.contains(token)) score += 15.0
                    if (descLower.contains(token)) score += 8.0
                    if (catLower.contains(token)) score += 12.0
                    if (place.tags.any { it.contains(token, ignoreCase = true) }) score += 10.0
                    if (place.services.any { it.contains(token, ignoreCase = true) }) score += 8.0
                }
            }

            if (score > 0) {
                // Incorporate distance decay into final score so closer places rank higher
                val finalScore = score + (10.0 / (distance + 0.2))
                Pair(place, finalScore)
            } else {
                null
            }
        }
        .sortedByDescending { it.second }
        .map { it.first }
    }

    // Nearby Recommendations for Home Screen
    fun getNearbyRecommendations(userArea: String, limit: Int = 6): List<Place> {
        val coords = MockData.getAreaCoords(userArea)
        return allMockPlaces
            .filter { it.isOpen }
            .sortedBy { it.calculateDistanceForArea(userArea, coords.first, coords.second) }
            .take(limit)
    }

    // Saved Places
    fun getSavedPlaces(): Flow<List<SavedPlaceEntity>> = dao.getAllSavedPlaces()

    fun getSavedPlaceIds(): Flow<Set<String>> =
        dao.getAllSavedPlaceIds().map { it.toSet() }

    suspend fun toggleSavePlace(place: Place, userArea: String) {
        val coords = MockData.getAreaCoords(userArea)
        val existing = dao.isPlaceSaved(place.id).firstOrNull() ?: false
        if (existing) {
            dao.deleteSavedPlaceById(place.id)
        } else {
            val entity = SavedPlaceEntity(
                placeId = place.id,
                name = place.name,
                category = place.category.name,
                description = place.description,
                address = place.address,
                area = place.area,
                phone = place.phone,
                distanceKm = place.calculateDistanceForArea(userArea, coords.first, coords.second),
                isOpen = place.isOpen,
                rating = place.rating
            )
            dao.insertSavedPlace(entity)
        }
    }

    suspend fun removeSavedPlace(placeId: String) {
        dao.deleteSavedPlaceById(placeId)
    }

    // Search History
    fun getRecentSearches(limit: Int = 8): Flow<List<SearchHistoryEntity>> =
        dao.getRecentSearches(limit)

    suspend fun recordSearch(query: String, categoryFilter: String? = null) {
        if (query.isNotBlank()) {
            dao.insertSearch(
                SearchHistoryEntity(
                    query = query.trim(),
                    categoryFilter = categoryFilter
                )
            )
        }
    }

    suspend fun deleteSearch(id: Long) {
        dao.deleteSearchById(id)
    }

    suspend fun clearSearchHistory() {
        dao.clearAllSearches()
    }

    // User Profile
    fun getUserProfile(): Flow<UserProfileEntity> = dao.getUserProfile().map {
        it ?: UserProfileEntity()
    }

    suspend fun updateProfile(name: String, area: String, themeMode: String) {
        dao.insertOrUpdateProfile(
            UserProfileEntity(
                id = 1,
                name = name,
                selectedArea = area,
                themeMode = themeMode
            )
        )
    }
}
