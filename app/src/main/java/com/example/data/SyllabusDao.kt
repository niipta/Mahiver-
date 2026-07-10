package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SyllabusDao {
    @Query("SELECT * FROM subjects")
    fun getAllSubjectsSync(): List<SubjectEntity>

    @Query("SELECT * FROM topics")
    fun getAllTopicsSync(): List<TopicEntity>

    @Query("SELECT * FROM subtopics")
    fun getAllSubtopicsSync(): List<SubtopicEntity>
    @Transaction
    @Query("SELECT * FROM subjects")
    fun getAllSubjectsWithTopics(): Flow<List<SubjectWithTopics>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: String): SubjectEntity?

    @Query("SELECT * FROM topics WHERE id = :id")
    suspend fun getTopicById(id: String): TopicEntity?
    @Query("SELECT * FROM topics WHERE id IN (:ids)")
    suspend fun getTopicsByIds(ids: List<String>): List<TopicEntity>


    @Query("SELECT * FROM subtopics WHERE id = :id")
    suspend fun getSubtopicById(id: String): SubtopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity)

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Delete
    suspend fun deleteTopic(topic: TopicEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtopic(subtopic: SubtopicEntity)

    @Update
    suspend fun updateSubtopic(subtopic: SubtopicEntity)

    @Delete
    suspend fun deleteSubtopic(subtopic: SubtopicEntity)
}
