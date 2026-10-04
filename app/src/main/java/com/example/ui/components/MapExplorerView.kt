package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MockData
import com.example.data.model.Place
import com.example.ui.theme.StarGold
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import java.util.Locale

@Composable
fun MapExplorerView(
    places: List<Place>,
    userArea: String,
    searchQuery: String,
    onPlaceClick: (Place) -> Unit,
    onDirectionsClick: (Place) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPlaceOnMap by remember { mutableStateOf<Place?>(places.firstOrNull()) }
    var zoomLevel by remember { mutableStateOf(1.0f) }

    val userCoords = MockData.getAreaCoords(userArea)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .testTag("map_explorer_view")
    ) {
        val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        val centerPulseColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        val centerPinColor = MaterialTheme.colorScheme.primary

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(places) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f

                        var closestPlace: Place? = null
                        var minDistanceSq = 50f * 50f

                        places.forEach { place ->
                            val dLat = ((place.latitude - userCoords.first) * 1200.0 * zoomLevel).toFloat()
                            val dLng = ((place.longitude - userCoords.second) * 1200.0 * zoomLevel).toFloat()
                            val pinX = centerX + dLng
                            val pinY = centerY - dLat

                            val dx = tapOffset.x - pinX
                            val dy = tapOffset.y - pinY
                            val distSq = dx * dx + dy * dy
                            if (distSq < minDistanceSq) {
                                minDistanceSq = distSq
                                closestPlace = place
                            }
                        }

                        if (closestPlace != null) {
                            selectedPlaceOnMap = closestPlace
                        }
                    }
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // Concentric distance rings
            val ringDistances = listOf(70f, 150f, 240f, 340f)
            ringDistances.forEach { r ->
                drawCircle(
                    color = gridColor,
                    radius = r * zoomLevel,
                    center = Offset(centerX, centerY),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
            }

            // Radar background pulse
            drawCircle(
                color = centerPulseColor,
                radius = 35f,
                center = Offset(centerX, centerY)
            )

            // User City Center marker
            drawCircle(
                color = centerPinColor,
                radius = 8f,
                center = Offset(centerX, centerY)
            )

            // Plot place markers
            places.forEach { place ->
                val isSelected = selectedPlaceOnMap?.id == place.id
                val dLat = ((place.latitude - userCoords.first) * 1200.0 * zoomLevel).toFloat()
                val dLng = ((place.longitude - userCoords.second) * 1200.0 * zoomLevel).toFloat()
                val pinX = (centerX + dLng).coerceIn(30f, size.width - 30f)
                val pinY = (centerY - dLat).coerceIn(30f, size.height - 30f)

                val pinColor = Color(place.category.badgeColorHex)

                if (isSelected) {
                    drawCircle(
                        color = pinColor.copy(alpha = 0.35f),
                        radius = 24f,
                        center = Offset(pinX, pinY)
                    )
                }

                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 14f else 10f,
                    center = Offset(pinX, pinY)
                )

                drawCircle(
                    color = pinColor,
                    radius = if (isSelected) 11f else 8f,
                    center = Offset(pinX, pinY)
                )
            }
        }

        // Top Toolbar inside Map: Area title, Google Maps Search button, and Map Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = userArea.substringBefore(","),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${places.size} places",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Button to open entire current search directly inside Google Maps app
            Button(
                onClick = {
                    val gMapsQuery = if (searchQuery.isNotBlank()) {
                        "${searchQuery.trim()} in ${userArea.substringBefore(",")}, Mpumalanga"
                    } else {
                        "Services and shops in ${userArea.substringBefore(",")}, Mpumalanga"
                    }
                    val gMapsUri = Uri.parse("geo:0,0?q=${Uri.encode(gMapsQuery)}")
                    val intent = Intent(Intent.ACTION_VIEW, gMapsUri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(gMapsQuery)}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                modifier = Modifier.testTag("open_in_google_maps_app_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Map,
                    contentDescription = "Google Maps",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Google Maps",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Map Zoom Controls & Recenter Button
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = { zoomLevel = (zoomLevel * 1.3f).coerceAtMost(2.5f) },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                onClick = { zoomLevel = (zoomLevel / 1.3f).coerceAtLeast(0.6f) },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                onClick = {
                    zoomLevel = 1.0f
                    selectedPlaceOnMap = places.firstOrNull()
                },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "Recenter",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Floating Bottom Place Card for the selected map pin
        selectedPlaceOnMap?.let { place ->
            val distance = place.calculateDistanceForArea(userArea, userCoords.first, userCoords.second)
            val formattedDistance = String.format(Locale.getDefault(), "%.1f km away", distance)

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clickable { onPlaceClick(place) }
                    .testTag("map_selected_place_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(place.category.badgeColorHex).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = place.category.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(place.category.badgeColorHex),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = StarGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${place.rating}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { selectedPlaceOnMap = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close preview",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formattedDistance,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "•", color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (place.isOpen) StatusGreen else StatusRed, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (place.isOpen) "Open Now" else "Closed",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (place.isOpen) StatusGreen else StatusRed,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onDirectionsClick(place) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("map_card_directions_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Navigation,
                                contentDescription = "Navigate",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Directions", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }

                        FilledTonalButton(
                            onClick = {
                                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${place.phone.replace(" ", "")}")
                                }
                                context.startActivity(dialIntent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("map_card_call_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Phone,
                                contentDescription = "Call",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Call", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
