package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MockData
import com.example.data.model.Place
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleMapsDirectionsSheet(
    place: Place?,
    userArea: String,
    onDismiss: () -> Unit
) {
    if (place == null) return

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedMode by remember { mutableStateOf("driving") } // "driving", "walking", "transit"

    val coords = MockData.getAreaCoords(userArea)
    val distance = place.calculateDistanceForArea(userArea, coords.first, coords.second)
    val formattedDistance = String.format(Locale.getDefault(), "%.1f km", distance)

    // Calculate approximate travel times
    val driveMinutes = max(2, ceil((distance / 40.0) * 60).toInt())
    val walkMinutes = max(5, ceil((distance / 4.8) * 60).toInt())
    val transitMinutes = max(10, ceil((distance / 25.0) * 60 + 8).toInt())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Google Maps branding & dismiss
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF4285F4).copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Map,
                                contentDescription = "Google Maps",
                                tint = Color(0xFF4285F4),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Google Maps Directions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Navigation from ${userArea.substringBefore(",")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Destination Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = place.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = formattedDistance,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = place.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Travel Mode Selection
            Text(
                text = "Choose Travel Mode",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Driving
                FilterChip(
                    selected = selectedMode == "driving",
                    onClick = { selectedMode = "driving" },
                    label = { Text("Drive (~$driveMinutes min)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.DirectionsCar,
                            contentDescription = "Drive",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                // Walking
                FilterChip(
                    selected = selectedMode == "walking",
                    onClick = { selectedMode = "walking" },
                    label = { Text("Walk (~$walkMinutes min)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.DirectionsWalk,
                            contentDescription = "Walk",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                // Transit
                FilterChip(
                    selected = selectedMode == "transit",
                    onClick = { selectedMode = "transit" },
                    label = { Text("Transit (~$transitMinutes min)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.DirectionsBus,
                            contentDescription = "Transit",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Primary Button: Launch Turn-by-Turn Navigation
            Button(
                onClick = {
                    launchGoogleMapsNavigation(context, place, selectedMode)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_google_maps_nav_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A73E8) // Google Maps Blue
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Navigation,
                    contentDescription = "Navigation",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Google Maps Navigation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Secondary Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        launchGoogleMapsSearch(context, place)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("explore_on_google_maps_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Explore on Map", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = {
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(place.getGoogleMapsDirectionsUrl(selectedMode))
                        )
                        context.startActivity(browserIntent)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_in_browser_maps_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Web Directions", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private fun launchGoogleMapsNavigation(context: Context, place: Place, mode: String) {
    val navMode = when (mode) {
        "walking" -> "w"
        "transit" -> "r"
        else -> "d"
    }
    val navUri = Uri.parse(place.getGoogleNavigationUri(navMode))
    val navIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
        setPackage("com.google.android.apps.maps")
    }

    try {
        context.startActivity(navIntent)
    } catch (e: ActivityNotFoundException) {
        // Fallback to general maps intent or browser
        val fallbackUri = Uri.parse(place.getGoogleMapsDirectionsUrl(mode))
        val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
        context.startActivity(fallbackIntent)
    }
}

private fun launchGoogleMapsSearch(context: Context, place: Place) {
    val searchUri = Uri.parse("geo:${place.latitude},${place.longitude}?q=${Uri.encode("${place.name}, ${place.address}")}")
    val searchIntent = Intent(Intent.ACTION_VIEW, searchUri).apply {
        setPackage("com.google.android.apps.maps")
    }

    try {
        context.startActivity(searchIntent)
    } catch (e: ActivityNotFoundException) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(place.getGoogleMapsSearchUrl()))
        context.startActivity(browserIntent)
    }
}
