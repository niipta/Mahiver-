package com.example.service;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.example.data.AppDatabase;
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
public final class SmartNotificationWorker_Factory {
  private final Provider<AppDatabase> dbProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public SmartNotificationWorker_Factory(Provider<AppDatabase> dbProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.dbProvider = dbProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  public SmartNotificationWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, dbProvider.get(), settingsRepositoryProvider.get());
  }

  public static SmartNotificationWorker_Factory create(Provider<AppDatabase> dbProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new SmartNotificationWorker_Factory(dbProvider, settingsRepositoryProvider);
  }

  public static SmartNotificationWorker newInstance(Context context, WorkerParameters params,
      AppDatabase db, SettingsRepository settingsRepository) {
    return new SmartNotificationWorker(context, params, db, settingsRepository);
  }
}
