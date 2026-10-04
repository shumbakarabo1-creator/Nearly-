package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.SavedPlaceEntity
import com.example.data.local.entity.SearchHistoryEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NearlyDao {

    // Saved Places
    @Query("SELECT * FROM saved_places ORDER BY savedAt DESC")
    fun getAllSavedPlaces(): Flow<List<SavedPlaceEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_places WHERE placeId = :placeId)")
    fun isPlaceSaved(placeId: String): Flow<Boolean>

    @Query("SELECT placeId FROM saved_places")
    fun getAllSavedPlaceIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPlace(place: SavedPlaceEntity)

    @Query("DELETE FROM saved_places WHERE placeId = :placeId")
    suspend fun deleteSavedPlaceById(placeId: String)

    // Search History
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSearches(limit: Int = 10): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchById(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearAllSearches()

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}
