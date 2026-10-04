package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.SavedPlaceEntity
import com.example.data.model.Place
import com.example.ui.ScreenTab
import com.example.ui.components.PlaceCard

@Composable
fun SavedScreen(
    savedPlaces: List<SavedPlaceEntity>,
    allPlaces: List<Place>,
    userArea: String,
    onRemoveSaved: (String) -> Unit,
    onToggleSave: (Place) -> Unit,
    onPlaceClick: (Place) -> Unit,
    onDirectionsClick: (Place) -> Unit = {},
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    // Map saved entities to actual Place objects to maintain complete functionality
    val savedPlaceModels = savedPlaces.mapNotNull { entity ->
        allPlaces.find { it.id == entity.placeId }
    }

    val filteredPlaces = if (selectedCategoryFilter != null) {
        savedPlaceModels.filter { it.category.name.equals(selectedCategoryFilter, ignoreCase = true) }
    } else {
        savedPlaceModels
    }

    val uniqueCategories = savedPlaceModels.map { it.category.name }.distinct()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saved Places",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${savedPlaces.size} places bookmarked for quick offline access",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(
                            text = "${savedPlaces.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Category Filter row if multiple categories saved
        if (uniqueCategories.size > 1) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All (${savedPlaceModels.size})") }
                    )
                    uniqueCategories.forEach { categoryName ->
                        val count = savedPlaceModels.count { it.category.name == categoryName }
                        FilterChip(
                            selected = selectedCategoryFilter == categoryName,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == categoryName) null else categoryName
                            },
                            label = { Text("$categoryName ($count)") }
                        )
                    }
                }
            }
        }

        // Empty State
        if (savedPlaces.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                        .testTag("saved_empty_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.BookmarkBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No saved places yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "As you discover services, shops, or repair shops, tap the bookmark icon on any card to save them here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onExploreClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("saved_explore_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Explore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(text = "Discover Places")
                        }
                    }
                }
            }
        }

        // Saved List
        items(filteredPlaces) { place ->
            PlaceCard(
                place = place,
                userArea = userArea,
                isSaved = true,
                onToggleSave = { onToggleSave(place) },
                onClick = { onPlaceClick(place) },
                onDirectionsClick = { onDirectionsClick(place) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
