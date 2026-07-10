package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RevisionDao {
    @Query("SELECT * FROM revisions")
    fun getAllRevisionsSync(): List<RevisionEntity>
    @Query("SELECT * FROM revisions ORDER BY scheduledDateMillis ASC")
    fun getAllRevisions(): Flow<List<RevisionEntity>>

    @Query("SELECT * FROM revisions WHERE isActive = 1 AND isCompleted = 0 ORDER BY scheduledDateMillis ASC")
    suspend fun getPendingRevisionsSync(): List<RevisionEntity>

    @Query("SELECT * FROM revisions WHERE relatedId = :relatedId")
    suspend fun getRevisionsByRelatedId(relatedId: String): List<RevisionEntity>

    @Query("SELECT * FROM revisions WHERE id = :id")
    suspend fun getRevisionById(id: String): RevisionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevision(revision: RevisionEntity)

    @Update
    suspend fun updateRevision(revision: RevisionEntity)

    @Delete
    suspend fun deleteRevision(revision: RevisionEntity)

    @Query("SELECT * FROM revisions WHERE scheduledDateMillis >= :startMs AND scheduledDateMillis < :endMs")
    fun getRevisionsInRange(startMs: Long, endMs: Long): Flow<List<RevisionEntity>>

    @Query("SELECT COUNT(*) FROM revisions WHERE isCompleted = 1")
    fun getCompletedRevisionsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM revisions WHERE isActive = 1 AND isCompleted = 0")
    fun getPendingRevisionsCount(): Flow<Int>

    @Query("DELETE FROM revisions WHERE relatedId = :relatedId")
    suspend fun deleteRevisionsByRelatedId(relatedId: String)

}
