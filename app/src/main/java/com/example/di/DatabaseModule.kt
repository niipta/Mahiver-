package com.example.di

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.ExamDao
import com.example.data.FocusDao
import com.example.data.MockDao
import com.example.data.PlannerDao
import com.example.data.RevisionDao
import com.example.data.SyllabusDao
import com.example.data.sync.SyncDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideSyllabusDao(db: AppDatabase): SyllabusDao = db.syllabusDao()

    @Provides
    fun provideRevisionDao(db: AppDatabase): RevisionDao = db.revisionDao()

    @Provides
    fun provideFocusDao(db: AppDatabase): FocusDao = db.focusDao()

    @Provides
    fun provideExamDao(db: AppDatabase): ExamDao = db.examDao()

    @Provides
    fun provideSyncDao(db: AppDatabase): SyncDao = db.syncDao()

    @Provides
    fun providePlannerDao(db: AppDatabase): PlannerDao = db.plannerDao()

    @Provides
    fun provideMockDao(db: AppDatabase): MockDao = db.mockDao()
}