package com.example.di;

import com.example.data.AppDatabase;
import com.example.data.ExamRepository;
import com.example.data.sync.SyncDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class RepositoryModule_ProvideExamRepositoryFactory implements Factory<ExamRepository> {
  private final Provider<AppDatabase> dbProvider;

  private final Provider<SyncDao> syncDaoProvider;

  public RepositoryModule_ProvideExamRepositoryFactory(Provider<AppDatabase> dbProvider,
      Provider<SyncDao> syncDaoProvider) {
    this.dbProvider = dbProvider;
    this.syncDaoProvider = syncDaoProvider;
  }

  @Override
  public ExamRepository get() {
    return provideExamRepository(dbProvider.get(), syncDaoProvider.get());
  }

  public static RepositoryModule_ProvideExamRepositoryFactory create(
      Provider<AppDatabase> dbProvider, Provider<SyncDao> syncDaoProvider) {
    return new RepositoryModule_ProvideExamRepositoryFactory(dbProvider, syncDaoProvider);
  }

  public static ExamRepository provideExamRepository(AppDatabase db, SyncDao syncDao) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideExamRepository(db, syncDao));
  }
}
