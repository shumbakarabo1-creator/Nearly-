package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.data.model.Place
import com.example.data.model.PlaceCategory
import com.example.ui.SortOption
import com.example.ui.components.CategoryChip
import com.example.ui.components.MapExplorerView
import com.example.ui.components.PlaceCard

@Composable
fun SearchScreen(
    searchQuery: String,
    selectedCategory: PlaceCategory?,
    maxDistanceKm: Double?,
    openNowOnly: Boolean,
    sortOption: SortOption,
    selectedArea: String,
    results: List<Place>,
    savedPlaceIds: Set<String>,
    isMapView: Boolean = false,
    onToggleMapView: () -> Unit = {},
    onQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onCategorySelect: (PlaceCategory?) -> Unit,
    onMaxDistanceSelect: (Double?) -> Unit,
    onToggleOpenNow: () -> Unit,
    onSortOptionSelect: (SortOption) -> Unit,
    onClearFilters: () -> Unit,
    onToggleSave: (Place) -> Unit,
    onPlaceClick: (Place) -> Unit,
    onDirectionsClick: (Place) -> Unit = {},
    onOpenAreaSelector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isFilterDrawerOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_text_input"),
                placeholder = {
                    Text("Search in ${selectedArea.substringBefore(",")}... (e.g. charger, print, fix phone)")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        onSearchSubmit(searchQuery)
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        // Area Pill, Filters Toggle & Map View Switcher
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Area button
                Surface(
                    onClick = onOpenAreaSelector,
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedArea.substringBefore(","),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Map vs List Toggle Button
                    Surface(
                        onClick = onToggleMapView,
                        shape = RoundedCornerShape(20.dp),
                        color = if (isMapView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.testTag("toggle_map_view_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isMapView) Icons.Filled.List else Icons.Filled.Map,
                                contentDescription = if (isMapView) "Show List" else "Show Map",
                                tint = if (isMapView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isMapView) "List" else "Map",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isMapView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Filter Drawer Button
                    Surface(
                        onClick = { isFilterDrawerOpen = !isFilterDrawerOpen },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isFilterDrawerOpen || maxDistanceKm != null || openNowOnly || sortOption != SortOption.RELEVANCE) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.testTag("toggle_filters_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = "Filters",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Filters",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Categories Filter Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" chip
                Surface(
                    onClick = { onCategorySelect(null) },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = if (selectedCategory == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedCategory == null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                PlaceCategory.entries.forEach { category ->
                    CategoryChip(
                        category = category,
                        isSelected = selectedCategory == category,
                        onClick = { onCategorySelect(category) }
                    )
                }
            }
        }

        // Expandable Detailed Filters
        item {
            AnimatedVisibility(visible = isFilterDrawerOpen) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Max Distance Filter
                        Text(
                            text = "Maximum Distance from City Center",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                Pair("Any", null),
                                Pair("< 1 km", 1.0),
                                Pair("< 3 km", 3.0),
                                Pair("< 5 km", 5.0)
                            ).forEach { (label, value) ->
                                val isSelected = maxDistanceKm == value
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onMaxDistanceSelect(value) },
                                    label = { Text(label) }
                                )
                            }
                        }

                        // Open Now
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Open Now Only",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = openNowOnly,
                                onCheckedChange = { onToggleOpenNow() }
                            )
                        }

                        // Sort Options
                        Text(
                            text = "Sort By",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SortOption.entries.forEach { option ->
                                val isSelected = sortOption == option
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSortOptionSelect(option) },
                                    label = {
                                        Text(
                                            when (option) {
                                                SortOption.RELEVANCE -> "Best Match"
                                                SortOption.NEAREST -> "Nearest"
                                                SortOption.RATING -> "Highest Rated"
                                            }
                                        )
                                    }
                                )
                            }
                        }

                        // Reset
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onClearFilters) {
                                Text("Reset Filters")
                            }
                        }
                    }
                }
            }
        }

        // View Mode: Map View vs List View
        if (isMapView) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(520.dp)
                ) {
                    MapExplorerView(
                        places = results,
                        userArea = selectedArea,
                        searchQuery = searchQuery,
                        onPlaceClick = onPlaceClick,
                        onDirectionsClick = onDirectionsClick
                    )
                }
            }
        } else {
            // Results Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            "Found ${results.size} places for \"$searchQuery\""
                        } else if (selectedCategory != null) {
                            "${selectedCategory.displayName} places (${results.size})"
                        } else {
                            "Places in ${selectedArea.substringBefore(",")} (${results.size})"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (searchQuery.isNotBlank() || selectedCategory != null || maxDistanceKm != null || openNowOnly) {
                        TextButton(onClick = onClearFilters) {
                            Text("Clear all", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Empty State
            if (results.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No places found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try adjusting your filters, selecting a broader distance, or searching another need.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(onClick = onClearFilters) {
                                Text("Reset Search Filters")
                            }
                        }
                    }
                }
            }

            // Results List
            items(results) { place ->
                PlaceCard(
                    place = place,
                    userArea = selectedArea,
                    isSaved = savedPlaceIds.contains(place.id),
                    onToggleSave = { onToggleSave(place) },
                    onClick = { onPlaceClick(place) },
                    onDirectionsClick = { onDirectionsClick(place) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
