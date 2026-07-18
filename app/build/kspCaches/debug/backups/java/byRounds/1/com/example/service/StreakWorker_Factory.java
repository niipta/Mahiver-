package com.example.service;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.example.data.FocusDao;
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
public final class StreakWorker_Factory {
  private final Provider<FocusDao> focusDaoProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public StreakWorker_Factory(Provider<FocusDao> focusDaoProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.focusDaoProvider = focusDaoProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  public StreakWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, focusDaoProvider.get(), settingsRepositoryProvider.get());
  }

  public static StreakWorker_Factory create(Provider<FocusDao> focusDaoProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new StreakWorker_Factory(focusDaoProvider, settingsRepositoryProvider);
  }

  public static StreakWorker newInstance(Context context, WorkerParameters params,
      FocusDao focusDao, SettingsRepository settingsRepository) {
    return new StreakWorker(context, params, focusDao, settingsRepository);
  }
}
