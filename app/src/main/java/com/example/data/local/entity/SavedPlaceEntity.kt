package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_places")
data class SavedPlaceEntity(
    @PrimaryKey
    val placeId: String,
    val name: String,
    val category: String,
    val description: String,
    val address: String,
    val area: String,
    val phone: String,
    val distanceKm: Double,
    val isOpen: Boolean,
    val rating: Double,
    val savedAt: Long = System.currentTimeMillis()
)
