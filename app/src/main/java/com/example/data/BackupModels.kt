package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupSettings(
    val userName: String = "MAHIR",
    val soundEnabled: Boolean = true
)

@JsonClass(generateAdapter = true)
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val settings: BackupSettings? = null,
    val subjects: List<SubjectEntity>,
    val topics: List<TopicEntity>,
    val subtopics: List<SubtopicEntity>,
    val revisions: List<RevisionEntity>,
    val focusSessions: List<FocusSessionEntity>,
    val dailyPlans: List<DailyPlanEntity>,
    val exams: List<ExamEntity> = emptyList(),
    val syncTasks: List<SyncQueueEntity> = emptyList()
)
