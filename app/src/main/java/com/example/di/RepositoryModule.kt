package com.example.di

import android.content.Context
import com.example.data.*
import com.example.data.sync.SyncDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSyllabusRepository(db: AppDatabase, syncDao: SyncDao): SyllabusRepository {
        return SyllabusRepository(db, syncDao)
    }

    @Provides
    @Singleton
    fun provideRevisionRepository(db: AppDatabase, syncDao: SyncDao): RevisionRepository {
        return RevisionRepository(db, syncDao)
    }

    @Provides
    @Singleton
    fun providePlannerRepository(db: AppDatabase, syncDao: SyncDao): PlannerRepository {
        return PlannerRepository(db, syncDao)
    }

    @Provides
    @Singleton
    fun provideBackupRepository(@ApplicationContext context: Context, db: AppDatabase): BackupRepository {
        return BackupRepository(context)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideExamRepository(db: AppDatabase, syncDao: SyncDao): ExamRepository {
        return ExamRepository(db, syncDao)
    }

    @Provides
    @Singleton
    fun provideFocusRepository(db: AppDatabase, syncDao: SyncDao): FocusRepository {
        return FocusRepository(db, syncDao)
    }

    @Provides
    @Singleton
    fun provideMockRepository(db: AppDatabase, syncDao: SyncDao): MockRepository {
        return MockRepository(db, syncDao)
    }
}