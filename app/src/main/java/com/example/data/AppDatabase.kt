package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SubjectEntity::class,
        TopicEntity::class,
        SubtopicEntity::class,
        RevisionEntity::class,
        FocusSessionEntity::class,
        ExamEntity::class,
        SyncQueueEntity::class,
        DailyPlanEntity::class,
        MockTestEntity::class,
        MockQuestionLogEntity::class
    ],
    version = 11,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun syllabusDao(): SyllabusDao
    abstract fun revisionDao(): RevisionDao
    abstract fun focusDao(): FocusDao
    abstract fun examDao(): ExamDao
    abstract fun syncDao(): com.example.data.sync.SyncDao
    abstract fun plannerDao(): PlannerDao
    abstract fun mockDao(): MockDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `daily_plans` (`dateString` TEXT NOT NULL, `dateMillis` INTEGER NOT NULL, `plannedTopicIds` TEXT NOT NULL, `plannedSubtopicIds` TEXT NOT NULL, `plannedRevisionIds` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, PRIMARY KEY(`dateString`))")
            }
        }

        private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Empty migration to preserve data
            }
        }

        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sync_queue_syncStatus_queuedAt` ON `sync_queue` (`syncStatus`, `queuedAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_revisions_relatedId` ON `revisions` (`relatedId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_revisions_scheduledDateMillis` ON `revisions` (`scheduledDateMillis`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_focus_sessions_timestamp` ON `focus_sessions` (`timestamp`)")
            }
        }

        private val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `revisions` ADD COLUMN `isActive` INTEGER NOT NULL DEFAULT 1")
            }
        }

        // v9 -> v10: introduce Mock Tests & Mock Attempts (old split schema)
        private val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `mock_tests` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `subjectId` TEXT,
                        `subjectName` TEXT NOT NULL,
                        `totalQuestions` INTEGER NOT NULL,
                        `durationMinutes` INTEGER NOT NULL,
                        `totalMarks` INTEGER NOT NULL,
                        `positiveMark` REAL NOT NULL,
                        `negativeMark` REAL NOT NULL,
                        `description` TEXT NOT NULL,
                        `tags` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )""".trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_tests_subjectId` ON `mock_tests` (`subjectId`)")

                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `mock_attempts` (
                        `id` TEXT NOT NULL,
                        `mockTestId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `subjectName` TEXT NOT NULL,
                        `totalQuestions` INTEGER NOT NULL,
                        `correctCount` INTEGER NOT NULL,
                        `wrongCount` INTEGER NOT NULL,
                        `unattemptedCount` INTEGER NOT NULL,
                        `score` REAL NOT NULL,
                        `totalMarks` REAL NOT NULL,
                        `durationMinutes` INTEGER NOT NULL,
                        `actualDurationSeconds` INTEGER NOT NULL,
                        `attemptedAt` INTEGER NOT NULL,
                        `accuracy` REAL NOT NULL,
                        `notes` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`mockTestId`) REFERENCES `mock_tests`(`id`) ON DELETE CASCADE
                    )""".trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_attempts_mockTestId` ON `mock_attempts` (`mockTestId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_attempts_attemptedAt` ON `mock_attempts` (`attemptedAt`)")
            }
        }

        /**
         * v10 -> v11: Flatten mock_tests + mock_attempts into a single mock_tests table
         * with a richer schema, and add the new mock_questions table for per-question
         * error tracking.
         *
         * This is a destructive migration for mock data — old mock tests/attempts are
         * dropped because the schema change is too large to migrate losslessly. All
         * other tables are untouched.
         */
        private val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // 1. Drop old mock_attempts table (and its indices)
                db.execSQL("DROP INDEX IF EXISTS `index_mock_attempts_mockTestId`")
                db.execSQL("DROP INDEX IF EXISTS `index_mock_attempts_attemptedAt`")
                db.execSQL("DROP TABLE IF EXISTS `mock_attempts`")

                // 2. Drop old mock_tests table (and its index) — we recreate with new schema
                db.execSQL("DROP INDEX IF EXISTS `index_mock_tests_subjectId`")
                db.execSQL("DROP TABLE IF EXISTS `mock_tests`")

                // 3. Recreate mock_tests with the new flat schema
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `mock_tests` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `subjectId` TEXT,
                        `subjectName` TEXT NOT NULL,
                        `totalQuestions` INTEGER NOT NULL,
                        `durationMinutes` INTEGER NOT NULL,
                        `totalMarks` INTEGER NOT NULL,
                        `positiveMark` REAL NOT NULL,
                        `negativeMark` REAL NOT NULL,
                        `marksObtained` REAL NOT NULL,
                        `correctCount` INTEGER NOT NULL,
                        `wrongCount` INTEGER NOT NULL,
                        `unattemptedCount` INTEGER NOT NULL,
                        `actualDurationSeconds` INTEGER NOT NULL,
                        `percentile` REAL NOT NULL,
                        `rank` INTEGER NOT NULL,
                        `totalCandidates` INTEGER NOT NULL,
                        `attemptedAt` INTEGER NOT NULL,
                        `description` TEXT NOT NULL,
                        `tags` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )""".trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_tests_category` ON `mock_tests` (`category`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_tests_attemptedAt` ON `mock_tests` (`attemptedAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_tests_subjectName` ON `mock_tests` (`subjectName`)")

                // 4. Create the new mock_questions table
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `mock_questions` (
                        `id` TEXT NOT NULL,
                        `mockTestId` TEXT NOT NULL,
                        `questionNumber` INTEGER NOT NULL,
                        `subjectName` TEXT NOT NULL,
                        `topicName` TEXT NOT NULL,
                        `errorCategory` TEXT NOT NULL,
                        `isCorrect` INTEGER NOT NULL,
                        `timeSpentSeconds` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`mockTestId`) REFERENCES `mock_tests`(`id`) ON DELETE CASCADE
                    )""".trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_questions_mockTestId` ON `mock_questions` (`mockTestId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_questions_subjectName` ON `mock_questions` (`subjectName`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_questions_topicName` ON `mock_questions` (`topicName`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_mock_questions_errorCategory` ON `mock_questions` (`errorCategory`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mahirverse_database"
                )
                .addMigrations(
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                    MIGRATION_10_11
                )
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
