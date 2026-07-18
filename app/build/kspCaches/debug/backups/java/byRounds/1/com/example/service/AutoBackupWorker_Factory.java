package com.example.service;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.example.data.BackupRepository;
import com.example.data.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class AutoBackupWorker_Factory {
  private final Provider<BackupRepository> backupRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public AutoBackupWorker_Factory(Provider<BackupRepository> backupRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.backupRepositoryProvider = backupRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  public AutoBackupWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, backupRepositoryProvider.get(), settingsRepositoryProvider.get());
  }

  public static AutoBackupWorker_Factory create(Provider<BackupRepository> backupRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new AutoBackupWorker_Factory(backupRepositoryProvider, settingsRepositoryProvider);
  }

  public static AutoBackupWorker newInstance(Context appContext, WorkerParameters workerParams,
      BackupRepository backupRepository, SettingsRepository settingsRepository) {
    return new AutoBackupWorker(appContext, workerParams, backupRepository, settingsRepository);
  }
}
