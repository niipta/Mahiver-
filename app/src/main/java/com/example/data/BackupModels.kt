package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupSettings(
    val userName: String = "MAHIR",
    val soundEnabled: Boolean = true
)

/**
 * Backup data model. Version 2 adds mockTests + mockQuestions for full backup.
 *
 * IMPORTANT: Backups are stored as plain JSON (NOT encrypted with AndroidKeystore).
 * The old v1 approach used device-locked encryption which made backups useless
 * after uninstall — the key was deleted with the app. Plain JSON is portable
 * across devices and survives reinstalls.
 */
@JsonClass(generateAdapter = true)
data class BackupData(
    val version: Int = 2,
    val timestamp: Long = System.currentTimeMillis(),
    val settings: BackupSettings? = null,
    val subjects: List<SubjectEntity>,
    val topics: List<TopicEntity>,
    val subtopics: List<SubtopicEntity>,
    val revisions: List<RevisionEntity>,
    val focusSessions: List<FocusSessionEntity>,
    val dailyPlans: List<DailyPlanEntity>,
    val exams: List<ExamEntity> = emptyList(),
    val syncTasks: List<SyncQueueEntity> = emptyList(),
    val mockTests: List<MockTestEntity> = emptyList(),
    val mockQuestions: List<MockQuestionLogEntity> = emptyList()
)
