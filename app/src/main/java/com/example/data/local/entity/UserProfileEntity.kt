package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Karabo Shumba",
    val selectedArea: String = "Mbombela (Nelspruit), Mpumalanga",
    val themeMode: String = "system" // "system", "light", "dark"
)
