package com.example.ui.history;

import com.example.data.FocusDao;
import com.example.data.RevisionDao;
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
public final class StudyHistoryViewModel_Factory implements Factory<StudyHistoryViewModel> {
  private final Provider<FocusDao> focusDaoProvider;

  private final Provider<SyllabusDao> syllabusDaoProvider;

  private final Provider<RevisionDao> revisionDaoProvider;

  private final Provider<SyncDao> syncDaoProvider;

  public StudyHistoryViewModel_Factory(Provider<FocusDao> focusDaoProvider,
      Provider<SyllabusDao> syllabusDaoProvider, Provider<RevisionDao> revisionDaoProvider,
      Provider<SyncDao> syncDaoProvider) {
    this.focusDaoProvider = focusDaoProvider;
    this.syllabusDaoProvider = syllabusDaoProvider;
    this.revisionDaoProvider = revisionDaoProvider;
    this.syncDaoProvider = syncDaoProvider;
  }

  @Override
  public StudyHistoryViewModel get() {
    return newInstance(focusDaoProvider.get(), syllabusDaoProvider.get(), revisionDaoProvider.get(), syncDaoProvider.get());
  }

  public static StudyHistoryViewModel_Factory create(Provider<FocusDao> focusDaoProvider,
      Provider<SyllabusDao> syllabusDaoProvider, Provider<RevisionDao> revisionDaoProvider,
      Provider<SyncDao> syncDaoProvider) {
    return new StudyHistoryViewModel_Factory(focusDaoProvider, syllabusDaoProvider, revisionDaoProvider, syncDaoProvider);
  }

  public static StudyHistoryViewModel newInstance(FocusDao focusDao, SyllabusDao syllabusDao,
      RevisionDao revisionDao, SyncDao syncDao) {
    return new StudyHistoryViewModel(focusDao, syllabusDao, revisionDao, syncDao);
  }
}
