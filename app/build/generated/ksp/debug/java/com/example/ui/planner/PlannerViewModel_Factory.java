package com.example.ui.planner;

import com.example.data.PlannerDao;
import com.example.data.PlannerRepository;
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
public final class PlannerViewModel_Factory implements Factory<PlannerViewModel> {
  private final Provider<SyllabusDao> syllabusDaoProvider;

  private final Provider<RevisionDao> revisionDaoProvider;

  private final Provider<PlannerDao> plannerDaoProvider;

  private final Provider<SyncDao> syncDaoProvider;

  private final Provider<PlannerRepository> repositoryProvider;

  public PlannerViewModel_Factory(Provider<SyllabusDao> syllabusDaoProvider,
      Provider<RevisionDao> revisionDaoProvider, Provider<PlannerDao> plannerDaoProvider,
      Provider<SyncDao> syncDaoProvider, Provider<PlannerRepository> repositoryProvider) {
    this.syllabusDaoProvider = syllabusDaoProvider;
    this.revisionDaoProvider = revisionDaoProvider;
    this.plannerDaoProvider = plannerDaoProvider;
    this.syncDaoProvider = syncDaoProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public PlannerViewModel get() {
    return newInstance(syllabusDaoProvider.get(), revisionDaoProvider.get(), plannerDaoProvider.get(), syncDaoProvider.get(), repositoryProvider.get());
  }

  public static PlannerViewModel_Factory create(Provider<SyllabusDao> syllabusDaoProvider,
      Provider<RevisionDao> revisionDaoProvider, Provider<PlannerDao> plannerDaoProvider,
      Provider<SyncDao> syncDaoProvider, Provider<PlannerRepository> repositoryProvider) {
    return new PlannerViewModel_Factory(syllabusDaoProvider, revisionDaoProvider, plannerDaoProvider, syncDaoProvider, repositoryProvider);
  }

  public static PlannerViewModel newInstance(SyllabusDao syllabusDao, RevisionDao revisionDao,
      PlannerDao plannerDao, SyncDao syncDao, PlannerRepository repository) {
    return new PlannerViewModel(syllabusDao, revisionDao, plannerDao, syncDao, repository);
  }
}
