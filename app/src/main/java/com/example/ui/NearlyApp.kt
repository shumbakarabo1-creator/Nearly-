package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AreaSelectorSheet
import com.example.ui.components.GoogleMapsDirectionsSheet
import com.example.ui.components.PlaceDetailSheet
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SavedScreen
import com.example.ui.screens.SearchScreen

@Composable
fun NearlyApp(
    viewModel: NearlyViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedArea by viewModel.selectedArea.collectAsStateWithLifecycle()
    val isAreaSelectorOpen by viewModel.isAreaSelectorOpen.collectAsStateWithLifecycle()
    val selectedPlaceDetails by viewModel.selectedPlaceDetails.collectAsStateWithLifecycle()
    val directionsPlace by viewModel.directionsPlace.collectAsStateWithLifecycle()
    val isMapViewActive by viewModel.isMapViewActive.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val maxDistanceKm by viewModel.maxDistanceKm.collectAsStateWithLifecycle()
    val openNowOnly by viewModel.openNowOnly.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()

    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val savedPlaces by viewModel.savedPlaces.collectAsStateWithLifecycle()
    val savedPlaceIds by viewModel.savedPlaceIds.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    // Handle back button: if not on Home, navigate to Home
    BackHandler(enabled = currentTab != ScreenTab.HOME) {
        viewModel.navigateTo(ScreenTab.HOME)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = androidx.compose.ui.unit.Dp(8f)
            ) {
                // Home
                NavigationBarItem(
                    selected = currentTab == ScreenTab.HOME,
                    onClick = { viewModel.navigateTo(ScreenTab.HOME) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("nav_item_home")
                )

                // Search
                NavigationBarItem(
                    selected = currentTab == ScreenTab.SEARCH,
                    onClick = { viewModel.navigateTo(ScreenTab.SEARCH) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                            contentDescription = "Search"
                        )
                    },
                    label = { Text("Search") },
                    modifier = Modifier.testTag("nav_item_search")
                )

                // Saved with Badge
                NavigationBarItem(
                    selected = currentTab == ScreenTab.SAVED,
                    onClick = { viewModel.navigateTo(ScreenTab.SAVED) },
                    icon = {
                        if (savedPlaceIds.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text("${savedPlaceIds.size}")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == ScreenTab.SAVED) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Saved"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (currentTab == ScreenTab.SAVED) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Saved"
                            )
                        }
                    },
                    label = { Text("Saved") },
                    modifier = Modifier.testTag("nav_item_saved")
                )

                // Profile
                NavigationBarItem(
                    selected = currentTab == ScreenTab.PROFILE,
                    onClick = { viewModel.navigateTo(ScreenTab.PROFILE) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile") },
                    modifier = Modifier.testTag("nav_item_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.HOME -> {
                    HomeScreen(
                        selectedArea = selectedArea,
                        onOpenAreaSelector = { viewModel.openAreaSelector() },
                        onSearchTriggered = { query, category ->
                            viewModel.performSearch(query, category)
                        },
                        onNavigateToTab = { tab -> viewModel.navigateTo(tab) },
                        recommendations = viewModel.getNearbyRecommendations(),
                        savedPlaceIds = savedPlaceIds,
                        recentSearches = searchHistory,
                        onToggleSave = { place -> viewModel.toggleSavePlace(place) },
                        onPlaceClick = { place -> viewModel.openPlaceDetails(place) },
                        onDirectionsClick = { place -> viewModel.openDirections(place) }
                    )
                }

                ScreenTab.SEARCH -> {
                    SearchScreen(
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        maxDistanceKm = maxDistanceKm,
                        openNowOnly = openNowOnly,
                        sortOption = sortOption,
                        selectedArea = selectedArea,
                        results = searchResults,
                        savedPlaceIds = savedPlaceIds,
                        isMapView = isMapViewActive,
                        onToggleMapView = { viewModel.toggleMapView() },
                        onQueryChange = { q -> viewModel.updateSearchQuery(q) },
                        onSearchSubmit = { q -> viewModel.performSearch(q, selectedCategory) },
                        onCategorySelect = { cat -> viewModel.selectCategory(cat) },
                        onMaxDistanceSelect = { dist -> viewModel.selectMaxDistance(dist) },
                        onToggleOpenNow = { viewModel.toggleOpenNowOnly() },
                        onSortOptionSelect = { sort -> viewModel.setSortOption(sort) },
                        onClearFilters = { viewModel.clearFilters() },
                        onToggleSave = { place -> viewModel.toggleSavePlace(place) },
                        onPlaceClick = { place -> viewModel.openPlaceDetails(place) },
                        onDirectionsClick = { place -> viewModel.openDirections(place) },
                        onOpenAreaSelector = { viewModel.openAreaSelector() }
                    )
                }

                ScreenTab.SAVED -> {
                    SavedScreen(
                        savedPlaces = savedPlaces,
                        allPlaces = viewModel.allPlaces,
                        userArea = selectedArea,
                        onRemoveSaved = { id -> viewModel.removeSavedPlace(id) },
                        onToggleSave = { place -> viewModel.toggleSavePlace(place) },
                        onPlaceClick = { place -> viewModel.openPlaceDetails(place) },
                        onDirectionsClick = { place -> viewModel.openDirections(place) },
                        onExploreClick = { viewModel.navigateTo(ScreenTab.SEARCH) }
                    )
                }

                ScreenTab.PROFILE -> {
                    ProfileScreen(
                        userProfile = userProfile,
                        savedPlacesCount = savedPlaces.size,
                        searchHistory = searchHistory,
                        onUpdateProfileName = { newName ->
                            viewModel.updateProfile(newName, selectedArea, userProfile.themeMode)
                        },
                        onOpenAreaSelector = { viewModel.openAreaSelector() },
                        onUpdateThemeMode = { mode ->
                            viewModel.updateProfile(userProfile.name, selectedArea, mode)
                        },
                        onDeleteSearchItem = { id -> viewModel.deleteSearchHistoryItem(id) },
                        onClearSearchHistory = { viewModel.clearAllSearchHistory() }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheets
    AreaSelectorSheet(
        isOpen = isAreaSelectorOpen,
        selectedArea = selectedArea,
        availableAreas = viewModel.availableAreas,
        onSelectArea = { area -> viewModel.selectArea(area) },
        onDismiss = { viewModel.closeAreaSelector() }
    )

    PlaceDetailSheet(
        place = selectedPlaceDetails,
        userArea = selectedArea,
        isSaved = selectedPlaceDetails?.let { savedPlaceIds.contains(it.id) } ?: false,
        onToggleSave = { selectedPlaceDetails?.let { viewModel.toggleSavePlace(it) } },
        onDirectionsClick = { place -> viewModel.openDirections(place) },
        onDismiss = { viewModel.closePlaceDetails() }
    )

    // Google Maps Directions Modal Sheet
    GoogleMapsDirectionsSheet(
        place = directionsPlace,
        userArea = selectedArea,
        onDismiss = { viewModel.closeDirections() }
    )
}
