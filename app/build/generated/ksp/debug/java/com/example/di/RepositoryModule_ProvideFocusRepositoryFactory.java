package com.example.di;

import com.example.data.AppDatabase;
import com.example.data.FocusRepository;
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
public final class RepositoryModule_ProvideFocusRepositoryFactory implements Factory<FocusRepository> {
  private final Provider<AppDatabase> dbProvider;

  private final Provider<SyncDao> syncDaoProvider;

  public RepositoryModule_ProvideFocusRepositoryFactory(Provider<AppDatabase> dbProvider,
      Provider<SyncDao> syncDaoProvider) {
    this.dbProvider = dbProvider;
    this.syncDaoProvider = syncDaoProvider;
  }

  @Override
  public FocusRepository get() {
    return provideFocusRepository(dbProvider.get(), syncDaoProvider.get());
  }

  public static RepositoryModule_ProvideFocusRepositoryFactory create(
      Provider<AppDatabase> dbProvider, Provider<SyncDao> syncDaoProvider) {
    return new RepositoryModule_ProvideFocusRepositoryFactory(dbProvider, syncDaoProvider);
  }

  public static FocusRepository provideFocusRepository(AppDatabase db, SyncDao syncDao) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideFocusRepository(db, syncDao));
  }
}
