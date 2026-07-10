package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY timestamp ASC")
    suspend fun getAllSessionsSync(): List<FocusSessionEntity>

    @Query("SELECT * FROM focus_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): FocusSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity)
    
    @androidx.room.Update
    suspend fun updateSession(session: FocusSessionEntity)
    
    @androidx.room.Delete
    suspend fun deleteSession(session: FocusSessionEntity)
}


data class DailyFocusTotal(val day: String, val totalMinutes: Int)
