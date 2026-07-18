package com.example.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.example.`data`.sync.SyncDao
import com.example.`data`.sync.SyncDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _syllabusDao: Lazy<SyllabusDao> = lazy {
    SyllabusDao_Impl(this)
  }

  private val _revisionDao: Lazy<RevisionDao> = lazy {
    RevisionDao_Impl(this)
  }

  private val _focusDao: Lazy<FocusDao> = lazy {
    FocusDao_Impl(this)
  }

  private val _examDao: Lazy<ExamDao> = lazy {
    ExamDao_Impl(this)
  }

  private val _syncDao: Lazy<SyncDao> = lazy {
    SyncDao_Impl(this)
  }

  private val _plannerDao: Lazy<PlannerDao> = lazy {
    PlannerDao_Impl(this)
  }

  private val _mockDao: Lazy<MockDao> = lazy {
    MockDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(11,
        "779845937980675be354045000c8088a", "be487174eb5bb5574cff488ebb4b5c18") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `subjects` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `icon` TEXT NOT NULL, `color` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `topics` (`id` TEXT NOT NULL, `subjectId` TEXT NOT NULL, `name` TEXT NOT NULL, `isPriority` INTEGER NOT NULL, `isWeak` INTEGER NOT NULL, `estimatedMinutes` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`subjectId`) REFERENCES `subjects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_topics_subjectId` ON `topics` (`subjectId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `subtopics` (`id` TEXT NOT NULL, `topicId` TEXT NOT NULL, `name` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`topicId`) REFERENCES `topics`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_subtopics_topicId` ON `subtopics` (`topicId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `revisions` (`id` TEXT NOT NULL, `relatedId` TEXT NOT NULL, `subjectName` TEXT NOT NULL, `title` TEXT NOT NULL, `type` TEXT NOT NULL, `priority` TEXT NOT NULL, `estimatedMinutes` INTEGER NOT NULL, `scheduledDateMillis` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `confidence` INTEGER NOT NULL, `repetitionLevel` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_revisions_relatedId` ON `revisions` (`relatedId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_revisions_scheduledDateMillis` ON `revisions` (`scheduledDateMillis`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `focus_sessions` (`id` TEXT NOT NULL, `subjectId` TEXT, `topicId` TEXT, `subtopicId` TEXT, `subjectName` TEXT NOT NULL, `topicName` TEXT, `durationMinutes` INTEGER NOT NULL, `actualDurationSeconds` INTEGER NOT NULL, `interruptions` INTEGER NOT NULL, `sessionType` TEXT NOT NULL, `isDeepFocus` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_focus_sessions_timestamp` ON `focus_sessions` (`timestamp`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `exams` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `dateMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sync_queue` (`id` TEXT NOT NULL, `operationType` TEXT NOT NULL, `entityType` TEXT NOT NULL, `entityId` TEXT NOT NULL, `syncStatus` TEXT NOT NULL, `retryCount` INTEGER NOT NULL, `queuedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_queue_syncStatus_queuedAt` ON `sync_queue` (`syncStatus`, `queuedAt`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `daily_plans` (`dateString` TEXT NOT NULL, `dateMillis` INTEGER NOT NULL, `plannedTopicIds` TEXT NOT NULL, `plannedSubtopicIds` TEXT NOT NULL, `plannedRevisionIds` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, PRIMARY KEY(`dateString`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `mock_tests` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `category` TEXT NOT NULL, `subjectId` TEXT, `subjectName` TEXT NOT NULL, `totalQuestions` INTEGER NOT NULL, `durationMinutes` INTEGER NOT NULL, `totalMarks` INTEGER NOT NULL, `positiveMark` REAL NOT NULL, `negativeMark` REAL NOT NULL, `marksObtained` REAL NOT NULL, `correctCount` INTEGER NOT NULL, `wrongCount` INTEGER NOT NULL, `unattemptedCount` INTEGER NOT NULL, `actualDurationSeconds` INTEGER NOT NULL, `percentile` REAL NOT NULL, `rank` INTEGER NOT NULL, `totalCandidates` INTEGER NOT NULL, `attemptedAt` INTEGER NOT NULL, `description` TEXT NOT NULL, `tags` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_tests_category` ON `mock_tests` (`category`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_tests_attemptedAt` ON `mock_tests` (`attemptedAt`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_tests_subjectName` ON `mock_tests` (`subjectName`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `mock_questions` (`id` TEXT NOT NULL, `mockTestId` TEXT NOT NULL, `questionNumber` INTEGER NOT NULL, `subjectName` TEXT NOT NULL, `topicName` TEXT NOT NULL, `errorCategory` TEXT NOT NULL, `isCorrect` INTEGER NOT NULL, `timeSpentSeconds` INTEGER NOT NULL, `notes` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`mockTestId`) REFERENCES `mock_tests`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_questions_mockTestId` ON `mock_questions` (`mockTestId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_questions_subjectName` ON `mock_questions` (`subjectName`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_questions_topicName` ON `mock_questions` (`topicName`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_questions_errorCategory` ON `mock_questions` (`errorCategory`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '779845937980675be354045000c8088a')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `subjects`")
        connection.execSQL("DROP TABLE IF EXISTS `topics`")
        connection.execSQL("DROP TABLE IF EXISTS `subtopics`")
        connection.execSQL("DROP TABLE IF EXISTS `revisions`")
        connection.execSQL("DROP TABLE IF EXISTS `focus_sessions`")
        connection.execSQL("DROP TABLE IF EXISTS `exams`")
        connection.execSQL("DROP TABLE IF EXISTS `sync_queue`")
        connection.execSQL("DROP TABLE IF EXISTS `daily_plans`")
        connection.execSQL("DROP TABLE IF EXISTS `mock_tests`")
        connection.execSQL("DROP TABLE IF EXISTS `mock_questions`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsSubjects: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSubjects.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSubjects.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSubjects.put("icon", TableInfo.Column("icon", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSubjects.put("color", TableInfo.Column("color", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSubjects: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSubjects: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSubjects: TableInfo = TableInfo("subjects", _columnsSubjects, _foreignKeysSubjects,
            _indicesSubjects)
        val _existingSubjects: TableInfo = read(connection, "subjects")
        if (!_infoSubjects.equals(_existingSubjects)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |subjects(com.example.data.SubjectEntity).
              | Expected:
              |""".trimMargin() + _infoSubjects + """
              |
              | Found:
              |""".trimMargin() + _existingSubjects)
        }
        val _columnsTopics: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTopics.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTopics.put("subjectId", TableInfo.Column("subjectId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTopics.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTopics.put("isPriority", TableInfo.Column("isPriority", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTopics.put("isWeak", TableInfo.Column("isWeak", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTopics.put("estimatedMinutes", TableInfo.Column("estimatedMinutes", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTopics.put("isCompleted", TableInfo.Column("isCompleted", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTopics: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysTopics.add(TableInfo.ForeignKey("subjects", "CASCADE", "NO ACTION",
            listOf("subjectId"), listOf("id")))
        val _indicesTopics: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesTopics.add(TableInfo.Index("index_topics_subjectId", false, listOf("subjectId"),
            listOf("ASC")))
        val _infoTopics: TableInfo = TableInfo("topics", _columnsTopics, _foreignKeysTopics,
            _indicesTopics)
        val _existingTopics: TableInfo = read(connection, "topics")
        if (!_infoTopics.equals(_existingTopics)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |topics(com.example.data.TopicEntity).
              | Expected:
              |""".trimMargin() + _infoTopics + """
              |
              | Found:
              |""".trimMargin() + _existingTopics)
        }
        val _columnsSubtopics: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSubtopics.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSubtopics.put("topicId", TableInfo.Column("topicId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSubtopics.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSubtopics.put("isCompleted", TableInfo.Column("isCompleted", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSubtopics: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysSubtopics.add(TableInfo.ForeignKey("topics", "CASCADE", "NO ACTION",
            listOf("topicId"), listOf("id")))
        val _indicesSubtopics: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSubtopics.add(TableInfo.Index("index_subtopics_topicId", false, listOf("topicId"),
            listOf("ASC")))
        val _infoSubtopics: TableInfo = TableInfo("subtopics", _columnsSubtopics,
            _foreignKeysSubtopics, _indicesSubtopics)
        val _existingSubtopics: TableInfo = read(connection, "subtopics")
        if (!_infoSubtopics.equals(_existingSubtopics)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |subtopics(com.example.data.SubtopicEntity).
              | Expected:
              |""".trimMargin() + _infoSubtopics + """
              |
              | Found:
              |""".trimMargin() + _existingSubtopics)
        }
        val _columnsRevisions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsRevisions.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRevisions.put("relatedId", TableInfo.Column("relatedId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRevisions.put("subjectName", TableInfo.Column("subjectName", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRevisions.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRevisions.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRevisions.put("priority", TableInfo.Column("priority", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRevisions.put("estimatedMinutes", TableInfo.Column("estimatedMinutes", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRevisions.put("scheduledDateMillis", TableInfo.Column("scheduledDateMillis",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRevisions.put("isCompleted", TableInfo.Column("isCompleted", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRevisions.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRevisions.put("confidence", TableInfo.Column("confidence", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsRevisions.put("repetitionLevel", TableInfo.Column("repetitionLevel", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysRevisions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesRevisions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesRevisions.add(TableInfo.Index("index_revisions_relatedId", false,
            listOf("relatedId"), listOf("ASC")))
        _indicesRevisions.add(TableInfo.Index("index_revisions_scheduledDateMillis", false,
            listOf("scheduledDateMillis"), listOf("ASC")))
        val _infoRevisions: TableInfo = TableInfo("revisions", _columnsRevisions,
            _foreignKeysRevisions, _indicesRevisions)
        val _existingRevisions: TableInfo = read(connection, "revisions")
        if (!_infoRevisions.equals(_existingRevisions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |revisions(com.example.data.RevisionEntity).
              | Expected:
              |""".trimMargin() + _infoRevisions + """
              |
              | Found:
              |""".trimMargin() + _existingRevisions)
        }
        val _columnsFocusSessions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFocusSessions.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("subjectId", TableInfo.Column("subjectId", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("topicId", TableInfo.Column("topicId", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("subtopicId", TableInfo.Column("subtopicId", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("subjectName", TableInfo.Column("subjectName", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("topicName", TableInfo.Column("topicName", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("durationMinutes", TableInfo.Column("durationMinutes", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("actualDurationSeconds", TableInfo.Column("actualDurationSeconds",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("interruptions", TableInfo.Column("interruptions", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("sessionType", TableInfo.Column("sessionType", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("isDeepFocus", TableInfo.Column("isDeepFocus", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFocusSessions.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFocusSessions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFocusSessions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesFocusSessions.add(TableInfo.Index("index_focus_sessions_timestamp", false,
            listOf("timestamp"), listOf("ASC")))
        val _infoFocusSessions: TableInfo = TableInfo("focus_sessions", _columnsFocusSessions,
            _foreignKeysFocusSessions, _indicesFocusSessions)
        val _existingFocusSessions: TableInfo = read(connection, "focus_sessions")
        if (!_infoFocusSessions.equals(_existingFocusSessions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |focus_sessions(com.example.data.FocusSessionEntity).
              | Expected:
              |""".trimMargin() + _infoFocusSessions + """
              |
              | Found:
              |""".trimMargin() + _existingFocusSessions)
        }
        val _columnsExams: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsExams.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExams.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExams.put("dateMillis", TableInfo.Column("dateMillis", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysExams: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesExams: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoExams: TableInfo = TableInfo("exams", _columnsExams, _foreignKeysExams,
            _indicesExams)
        val _existingExams: TableInfo = read(connection, "exams")
        if (!_infoExams.equals(_existingExams)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |exams(com.example.data.ExamEntity).
              | Expected:
              |""".trimMargin() + _infoExams + """
              |
              | Found:
              |""".trimMargin() + _existingExams)
        }
        val _columnsSyncQueue: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSyncQueue.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncQueue.put("operationType", TableInfo.Column("operationType", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncQueue.put("entityType", TableInfo.Column("entityType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncQueue.put("entityId", TableInfo.Column("entityId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncQueue.put("syncStatus", TableInfo.Column("syncStatus", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncQueue.put("retryCount", TableInfo.Column("retryCount", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSyncQueue.put("queuedAt", TableInfo.Column("queuedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSyncQueue: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSyncQueue: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSyncQueue.add(TableInfo.Index("index_sync_queue_syncStatus_queuedAt", false,
            listOf("syncStatus", "queuedAt"), listOf("ASC", "ASC")))
        val _infoSyncQueue: TableInfo = TableInfo("sync_queue", _columnsSyncQueue,
            _foreignKeysSyncQueue, _indicesSyncQueue)
        val _existingSyncQueue: TableInfo = read(connection, "sync_queue")
        if (!_infoSyncQueue.equals(_existingSyncQueue)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |sync_queue(com.example.data.SyncQueueEntity).
              | Expected:
              |""".trimMargin() + _infoSyncQueue + """
              |
              | Found:
              |""".trimMargin() + _existingSyncQueue)
        }
        val _columnsDailyPlans: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDailyPlans.put("dateString", TableInfo.Column("dateString", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyPlans.put("dateMillis", TableInfo.Column("dateMillis", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyPlans.put("plannedTopicIds", TableInfo.Column("plannedTopicIds", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyPlans.put("plannedSubtopicIds", TableInfo.Column("plannedSubtopicIds", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyPlans.put("plannedRevisionIds", TableInfo.Column("plannedRevisionIds", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyPlans.put("isCompleted", TableInfo.Column("isCompleted", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDailyPlans: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDailyPlans: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDailyPlans: TableInfo = TableInfo("daily_plans", _columnsDailyPlans,
            _foreignKeysDailyPlans, _indicesDailyPlans)
        val _existingDailyPlans: TableInfo = read(connection, "daily_plans")
        if (!_infoDailyPlans.equals(_existingDailyPlans)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |daily_plans(com.example.data.DailyPlanEntity).
              | Expected:
              |""".trimMargin() + _infoDailyPlans + """
              |
              | Found:
              |""".trimMargin() + _existingDailyPlans)
        }
        val _columnsMockTests: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMockTests.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("category", TableInfo.Column("category", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("subjectId", TableInfo.Column("subjectId", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("subjectName", TableInfo.Column("subjectName", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("totalQuestions", TableInfo.Column("totalQuestions", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("durationMinutes", TableInfo.Column("durationMinutes", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("totalMarks", TableInfo.Column("totalMarks", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("positiveMark", TableInfo.Column("positiveMark", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("negativeMark", TableInfo.Column("negativeMark", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("marksObtained", TableInfo.Column("marksObtained", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("correctCount", TableInfo.Column("correctCount", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("wrongCount", TableInfo.Column("wrongCount", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("unattemptedCount", TableInfo.Column("unattemptedCount", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("actualDurationSeconds", TableInfo.Column("actualDurationSeconds",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("percentile", TableInfo.Column("percentile", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("rank", TableInfo.Column("rank", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("totalCandidates", TableInfo.Column("totalCandidates", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("attemptedAt", TableInfo.Column("attemptedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("description", TableInfo.Column("description", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMockTests.put("tags", TableInfo.Column("tags", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMockTests: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMockTests: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesMockTests.add(TableInfo.Index("index_mock_tests_category", false,
            listOf("category"), listOf("ASC")))
        _indicesMockTests.add(TableInfo.Index("index_mock_tests_attemptedAt", false,
            listOf("attemptedAt"), listOf("ASC")))
        _indicesMockTests.add(TableInfo.Index("index_mock_tests_subjectName", false,
            listOf("subjectName"), listOf("ASC")))
        val _infoMockTests: TableInfo = TableInfo("mock_tests", _columnsMockTests,
            _foreignKeysMockTests, _indicesMockTests)
        val _existingMockTests: TableInfo = read(connection, "mock_tests")
        if (!_infoMockTests.equals(_existingMockTests)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |mock_tests(com.example.data.MockTestEntity).
              | Expected:
              |""".trimMargin() + _infoMockTests + """
              |
              | Found:
              |""".trimMargin() + _existingMockTests)
        }
        val _columnsMockQuestions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMockQuestions.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMockQuestions.put("mockTestId", TableInfo.Column("mockTestId", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMockQuestions.put("questionNumber", TableInfo.Column("questionNumber", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMockQuestions.put("subjectName", TableInfo.Column("subjectName", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMockQuestions.put("topicName", TableInfo.Column("topicName", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMockQuestions.put("errorCategory", TableInfo.Column("errorCategory", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMockQuestions.put("isCorrect", TableInfo.Column("isCorrect", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMockQuestions.put("timeSpentSeconds", TableInfo.Column("timeSpentSeconds",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMockQuestions.put("notes", TableInfo.Column("notes", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMockQuestions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysMockQuestions.add(TableInfo.ForeignKey("mock_tests", "CASCADE", "NO ACTION",
            listOf("mockTestId"), listOf("id")))
        val _indicesMockQuestions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesMockQuestions.add(TableInfo.Index("index_mock_questions_mockTestId", false,
            listOf("mockTestId"), listOf("ASC")))
        _indicesMockQuestions.add(TableInfo.Index("index_mock_questions_subjectName", false,
            listOf("subjectName"), listOf("ASC")))
        _indicesMockQuestions.add(TableInfo.Index("index_mock_questions_topicName", false,
            listOf("topicName"), listOf("ASC")))
        _indicesMockQuestions.add(TableInfo.Index("index_mock_questions_errorCategory", false,
            listOf("errorCategory"), listOf("ASC")))
        val _infoMockQuestions: TableInfo = TableInfo("mock_questions", _columnsMockQuestions,
            _foreignKeysMockQuestions, _indicesMockQuestions)
        val _existingMockQuestions: TableInfo = read(connection, "mock_questions")
        if (!_infoMockQuestions.equals(_existingMockQuestions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |mock_questions(com.example.data.MockQuestionLogEntity).
              | Expected:
              |""".trimMargin() + _infoMockQuestions + """
              |
              | Found:
              |""".trimMargin() + _existingMockQuestions)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "subjects", "topics",
        "subtopics", "revisions", "focus_sessions", "exams", "sync_queue", "daily_plans",
        "mock_tests", "mock_questions")
  }

  public override fun clearAllTables() {
    super.performClear(true, "subjects", "topics", "subtopics", "revisions", "focus_sessions",
        "exams", "sync_queue", "daily_plans", "mock_tests", "mock_questions")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(SyllabusDao::class, SyllabusDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(RevisionDao::class, RevisionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(FocusDao::class, FocusDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ExamDao::class, ExamDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SyncDao::class, SyncDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(PlannerDao::class, PlannerDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(MockDao::class, MockDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun syllabusDao(): SyllabusDao = _syllabusDao.value

  public override fun revisionDao(): RevisionDao = _revisionDao.value

  public override fun focusDao(): FocusDao = _focusDao.value

  public override fun examDao(): ExamDao = _examDao.value

  public override fun syncDao(): SyncDao = _syncDao.value

  public override fun plannerDao(): PlannerDao = _plannerDao.value

  public override fun mockDao(): MockDao = _mockDao.value
}
