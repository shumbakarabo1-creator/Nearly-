package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MockData
import com.example.data.local.NearlyDatabase
import com.example.data.local.entity.SavedPlaceEntity
import com.example.data.local.entity.SearchHistoryEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.Place
import com.example.data.model.PlaceCategory
import com.example.data.repository.NearlyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
    HOME,
    SEARCH,
    SAVED,
    PROFILE
}

enum class SortOption {
    RELEVANCE,
    NEAREST,
    RATING
}

private data class FilterState(
    val query: String,
    val category: PlaceCategory?,
    val maxDistanceKm: Double?,
    val openNowOnly: Boolean,
    val area: String
)

class NearlyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NearlyRepository

    init {
        val database = NearlyDatabase.getInstance(application)
        repository = NearlyRepository(database.nearlyDao())
    }

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    // Active Selected Area - Defaults to Mpumalanga Capital
    private val _selectedArea = MutableStateFlow("Mbombela (Nelspruit), Mpumalanga")
    val selectedArea: StateFlow<String> = _selectedArea.asStateFlow()

    val availableAreas = MockData.AVAILABLE_AREAS
    val allPlaces: List<Place> = repository.allMockPlaces

    // Area Selector Bottom Sheet
    private val _isAreaSelectorOpen = MutableStateFlow(false)
    val isAreaSelectorOpen: StateFlow<Boolean> = _isAreaSelectorOpen.asStateFlow()

    // Place Details Modal Sheet
    private val _selectedPlaceDetails = MutableStateFlow<Place?>(null)
    val selectedPlaceDetails: StateFlow<Place?> = _selectedPlaceDetails.asStateFlow()

    // Google Maps Directions Modal Sheet
    private val _directionsPlace = MutableStateFlow<Place?>(null)
    val directionsPlace: StateFlow<Place?> = _directionsPlace.asStateFlow()

    // Map View Toggle in Search Screen
    private val _isMapViewActive = MutableStateFlow(false)
    val isMapViewActive: StateFlow<Boolean> = _isMapViewActive.asStateFlow()

    // Search & Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<PlaceCategory?>(null)
    val selectedCategory: StateFlow<PlaceCategory?> = _selectedCategory.asStateFlow()

    private val _maxDistanceKm = MutableStateFlow<Double?>(null)
    val maxDistanceKm: StateFlow<Double?> = _maxDistanceKm.asStateFlow()

    private val _openNowOnly = MutableStateFlow(false)
    val openNowOnly: StateFlow<Boolean> = _openNowOnly.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.RELEVANCE)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // Saved Places from Room Database
    val savedPlaces: StateFlow<List<SavedPlaceEntity>> = repository.getSavedPlaces()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savedPlaceIds: StateFlow<Set<String>> = repository.getSavedPlaceIds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    // Search History from Room Database
    val searchHistory: StateFlow<List<SearchHistoryEntity>> = repository.getRecentSearches(10)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // User Profile from Room Database
    val userProfile: StateFlow<UserProfileEntity> = repository.getUserProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfileEntity()
        )

    // Search Results combined state
    val searchResults: StateFlow<List<Place>> = combine(
        _searchQuery,
        _selectedCategory,
        _maxDistanceKm,
        _openNowOnly,
        _selectedArea
    ) { query, category, maxDist, openNow, area ->
        FilterState(query, category, maxDist, openNow, area)
    }.combine(_sortOption) { filterState, sort ->
        val results = repository.searchPlaces(
            query = filterState.query,
            category = filterState.category,
            maxDistanceKm = filterState.maxDistanceKm,
            openNowOnly = filterState.openNowOnly,
            userArea = filterState.area
        )
        val coords = MockData.getAreaCoords(filterState.area)
        when (sort) {
            SortOption.RELEVANCE -> results
            SortOption.NEAREST -> results.sortedBy { it.calculateDistanceForArea(filterState.area, coords.first, coords.second) }
            SortOption.RATING -> results.sortedByDescending { it.rating }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            val profile = repository.getUserProfile().firstOrNull()
            if (profile != null && profile.selectedArea.isNotBlank()) {
                _selectedArea.value = profile.selectedArea
            }
        }
    }

    // Navigation Actions
    fun navigateTo(tab: ScreenTab) {
        _currentTab.value = tab
    }

    // Area Actions
    fun selectArea(area: String) {
        _selectedArea.value = area
        _isAreaSelectorOpen.value = false
        viewModelScope.launch {
            val current = userProfile.value
            repository.updateProfile(current.name, area, current.themeMode)
        }
    }

    fun openAreaSelector() {
        _isAreaSelectorOpen.value = true
    }

    fun closeAreaSelector() {
        _isAreaSelectorOpen.value = false
    }

    // Detail Sheet Actions
    fun openPlaceDetails(place: Place) {
        _selectedPlaceDetails.value = place
    }

    fun openPlaceDetailsById(placeId: String) {
        val place = repository.allMockPlaces.find { it.id == placeId }
        if (place != null) {
            _selectedPlaceDetails.value = place
        }
    }

    fun closePlaceDetails() {
        _selectedPlaceDetails.value = null
    }

    // Google Maps Directions Actions
    fun openDirections(place: Place) {
        _directionsPlace.value = place
    }

    fun closeDirections() {
        _directionsPlace.value = null
    }

    // Map View Toggle Action
    fun toggleMapView() {
        _isMapViewActive.value = !_isMapViewActive.value
    }

    fun setMapViewActive(active: Boolean) {
        _isMapViewActive.value = active
    }

    // Search & Filter Actions
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun performSearch(query: String, category: PlaceCategory? = null) {
        _searchQuery.value = query
        if (category != null) {
            _selectedCategory.value = category
        }
        _currentTab.value = ScreenTab.SEARCH
        viewModelScope.launch {
            if (query.isNotBlank()) {
                repository.recordSearch(query, category?.name)
            }
        }
    }

    fun selectCategory(category: PlaceCategory?) {
        if (_selectedCategory.value == category) {
            _selectedCategory.value = null
        } else {
            _selectedCategory.value = category
        }
    }

    fun selectMaxDistance(distanceKm: Double?) {
        if (_maxDistanceKm.value == distanceKm) {
            _maxDistanceKm.value = null
        } else {
            _maxDistanceKm.value = distanceKm
        }
    }

    fun toggleOpenNowOnly() {
        _openNowOnly.value = !_openNowOnly.value
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _maxDistanceKm.value = null
        _openNowOnly.value = false
        _sortOption.value = SortOption.RELEVANCE
    }

    // Bookmark / Save Actions
    fun toggleSavePlace(place: Place) {
        viewModelScope.launch {
            repository.toggleSavePlace(place, _selectedArea.value)
        }
    }

    fun removeSavedPlace(placeId: String) {
        viewModelScope.launch {
            repository.removeSavedPlace(placeId)
        }
    }

    // Search History Actions
    fun deleteSearchHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteSearch(id)
        }
    }

    fun clearAllSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }

    // Profile Actions
    fun updateProfile(name: String, area: String, themeMode: String) {
        viewModelScope.launch {
            repository.updateProfile(name, area, themeMode)
            _selectedArea.value = area
        }
    }

    fun getNearbyRecommendations(): List<Place> {
        return repository.getNearbyRecommendations(_selectedArea.value, limit = 5)
    }
}
