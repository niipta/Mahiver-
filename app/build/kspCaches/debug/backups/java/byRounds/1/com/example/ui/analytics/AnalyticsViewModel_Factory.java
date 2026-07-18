package com.example.ui.analytics;

import com.example.data.FocusDao;
import com.example.data.RevisionDao;
import com.example.data.SettingsRepository;
import com.example.data.SyllabusDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class AnalyticsViewModel_Factory implements Factory<AnalyticsViewModel> {
  private final Provider<SyllabusDao> syllabusDaoProvider;

  private final Provider<RevisionDao> revisionDaoProvider;

  private final Provider<FocusDao> focusDaoProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public AnalyticsViewModel_Factory(Provider<SyllabusDao> syllabusDaoProvider,
      Provider<RevisionDao> revisionDaoProvider, Provider<FocusDao> focusDaoProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.syllabusDaoProvider = syllabusDaoProvider;
    this.revisionDaoProvider = revisionDaoProvider;
    this.focusDaoProvider = focusDaoProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public AnalyticsViewModel get() {
    return newInstance(syllabusDaoProvider.get(), revisionDaoProvider.get(), focusDaoProvider.get(), settingsRepositoryProvider.get());
  }

  public static AnalyticsViewModel_Factory create(Provider<SyllabusDao> syllabusDaoProvider,
      Provider<RevisionDao> revisionDaoProvider, Provider<FocusDao> focusDaoProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new AnalyticsViewModel_Factory(syllabusDaoProvider, revisionDaoProvider, focusDaoProvider, settingsRepositoryProvider);
  }

  public static AnalyticsViewModel newInstance(SyllabusDao syllabusDao, RevisionDao revisionDao,
      FocusDao focusDao, SettingsRepository settingsRepository) {
    return new AnalyticsViewModel(syllabusDao, revisionDao, focusDao, settingsRepository);
  }
}
