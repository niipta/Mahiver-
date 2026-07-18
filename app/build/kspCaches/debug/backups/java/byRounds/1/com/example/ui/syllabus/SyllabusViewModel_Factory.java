package com.example.ui.syllabus;

import com.example.data.ExamDao;
import com.example.data.RevisionDao;
import com.example.data.RevisionRepository;
import com.example.data.SyllabusDao;
import com.example.data.SyllabusRepository;
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
public final class SyllabusViewModel_Factory implements Factory<SyllabusViewModel> {
  private final Provider<SyllabusRepository> repositoryProvider;

  private final Provider<SyllabusDao> syllabusDaoProvider;

  private final Provider<RevisionDao> revisionDaoProvider;

  private final Provider<RevisionRepository> revisionRepositoryProvider;

  private final Provider<ExamDao> examDaoProvider;

  private final Provider<SyncDao> syncDaoProvider;

  public SyllabusViewModel_Factory(Provider<SyllabusRepository> repositoryProvider,
      Provider<SyllabusDao> syllabusDaoProvider, Provider<RevisionDao> revisionDaoProvider,
      Provider<RevisionRepository> revisionRepositoryProvider, Provider<ExamDao> examDaoProvider,
      Provider<SyncDao> syncDaoProvider) {
    this.repositoryProvider = repositoryProvider;
    this.syllabusDaoProvider = syllabusDaoProvider;
    this.revisionDaoProvider = revisionDaoProvider;
    this.revisionRepositoryProvider = revisionRepositoryProvider;
    this.examDaoProvider = examDaoProvider;
    this.syncDaoProvider = syncDaoProvider;
  }

  @Override
  public SyllabusViewModel get() {
    return newInstance(repositoryProvider.get(), syllabusDaoProvider.get(), revisionDaoProvider.get(), revisionRepositoryProvider.get(), examDaoProvider.get(), syncDaoProvider.get());
  }

  public static SyllabusViewModel_Factory create(Provider<SyllabusRepository> repositoryProvider,
      Provider<SyllabusDao> syllabusDaoProvider, Provider<RevisionDao> revisionDaoProvider,
      Provider<RevisionRepository> revisionRepositoryProvider, Provider<ExamDao> examDaoProvider,
      Provider<SyncDao> syncDaoProvider) {
    return new SyllabusViewModel_Factory(repositoryProvider, syllabusDaoProvider, revisionDaoProvider, revisionRepositoryProvider, examDaoProvider, syncDaoProvider);
  }

  public static SyllabusViewModel newInstance(SyllabusRepository repository,
      SyllabusDao syllabusDao, RevisionDao revisionDao, RevisionRepository revisionRepository,
      ExamDao examDao, SyncDao syncDao) {
    return new SyllabusViewModel(repository, syllabusDao, revisionDao, revisionRepository, examDao, syncDao);
  }
}
