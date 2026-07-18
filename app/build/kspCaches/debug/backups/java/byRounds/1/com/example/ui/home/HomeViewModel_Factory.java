package com.example.ui.home;

import com.example.data.ExamDao;
import com.example.data.FocusDao;
import com.example.data.PlannerDao;
import com.example.data.RevisionDao;
import com.example.data.SettingsRepository;
import com.example.data.SyllabusDao;
import com.example.data.sync.SyncDao;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<SyllabusDao> syllabusDaoProvider;

  private final Provider<RevisionDao> revisionDaoProvider;

  private final Provider<FocusDao> focusDaoProvider;

  private final Provider<ExamDao> examDaoProvider;

  private final Provider<PlannerDao> plannerDaoProvider;

  private final Provider<SyncDao> syncDaoProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public HomeViewModel_Factory(Provider<SyllabusDao> syllabusDaoProvider,
      Provider<RevisionDao> revisionDaoProvider, Provider<FocusDao> focusDaoProvider,
      Provider<ExamDao> examDaoProvider, Provider<PlannerDao> plannerDaoProvider,
      Provider<SyncDao> syncDaoProvider, Provider<SettingsRepository> settingsRepositoryProvider) {
    this.syllabusDaoProvider = syllabusDaoProvider;
    this.revisionDaoProvider = revisionDaoProvider;
    this.focusDaoProvider = focusDaoProvider;
    this.examDaoProvider = examDaoProvider;
    this.plannerDaoProvider = plannerDaoProvider;
    this.syncDaoProvider = syncDaoProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(syllabusDaoProvider.get(), revisionDaoProvider.get(), focusDaoProvider.get(), examDaoProvider.get(), plannerDaoProvider.get(), syncDaoProvider.get(), settingsRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<SyllabusDao> syllabusDaoProvider,
      Provider<RevisionDao> revisionDaoProvider, Provider<FocusDao> focusDaoProvider,
      Provider<ExamDao> examDaoProvider, Provider<PlannerDao> plannerDaoProvider,
      Provider<SyncDao> syncDaoProvider, Provider<SettingsRepository> settingsRepositoryProvider) {
    return new HomeViewModel_Factory(syllabusDaoProvider, revisionDaoProvider, focusDaoProvider, examDaoProvider, plannerDaoProvider, syncDaoProvider, settingsRepositoryProvider);
  }

  public static HomeViewModel newInstance(SyllabusDao syllabusDao, RevisionDao revisionDao,
      FocusDao focusDao, ExamDao examDao, PlannerDao plannerDao, SyncDao syncDao,
      SettingsRepository settingsRepository) {
    return new HomeViewModel(syllabusDao, revisionDao, focusDao, examDao, plannerDao, syncDao, settingsRepository);
  }
}
