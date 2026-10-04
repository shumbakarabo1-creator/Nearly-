package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

enum class PlaceCategory(
    val displayName: String,
    val icon: ImageVector,
    val quickKeyword: String,
    val badgeColorHex: Long
) {
    FOOD("Food", Icons.Default.Restaurant, "food", 0xFFE11D48),
    SHOPPING("Shopping", Icons.Default.ShoppingBag, "shopping", 0xFFD97706),
    SERVICES("Services", Icons.Default.Handyman, "services", 0xFF0284C7),
    REPAIRS("Repairs", Icons.Default.Build, "repairs", 0xFF7C3AED),
    TRANSPORT("Transport", Icons.Default.DirectionsBus, "transport", 0xFF059669),
    HEALTH("Health", Icons.Default.LocalHospital, "health", 0xFFDC2626),
    EDUCATION("Education", Icons.Default.School, "education", 0xFF4338CA),
    PRINTING("Printing", Icons.Default.Print, "printing", 0xFF0D9488),
    BEAUTY("Beauty", Icons.Default.ContentCut, "beauty", 0xFFDB2777),
    ELECTRONICS("Electronics", Icons.Default.Devices, "electronics", 0xFF2563EB),
    OTHER("Other", Icons.Default.Category, "other", 0xFF64748B);

    companion object {
        fun fromString(value: String): PlaceCategory {
            return entries.find {
                it.name.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true)
            } ?: OTHER
        }
    }
}
