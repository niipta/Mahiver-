package com.example.data.sync

import androidx.room.*
import com.example.data.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncDao {
    @Query("SELECT * FROM sync_queue WHERE syncStatus = 'PENDING' ORDER BY queuedAt ASC")
    fun getPendingSyncQueue(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE syncStatus = 'PENDING' ORDER BY queuedAt ASC")
    suspend fun getPendingSyncQueueSync(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncTask(task: SyncQueueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncTasks(tasks: List<SyncQueueEntity>)

    @Update
    suspend fun updateSyncTask(task: SyncQueueEntity)

    @Delete
    suspend fun deleteSyncTask(task: SyncQueueEntity)
    
    @Query("DELETE FROM sync_queue WHERE syncStatus = 'COMPLETED'")
    suspend fun clearCompletedTasks()
}
