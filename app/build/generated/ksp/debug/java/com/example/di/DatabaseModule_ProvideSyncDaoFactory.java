package com.example.di;

import com.example.data.AppDatabase;
import com.example.data.sync.SyncDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideSyncDaoFactory implements Factory<SyncDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideSyncDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SyncDao get() {
    return provideSyncDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideSyncDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideSyncDaoFactory(dbProvider);
  }

  public static SyncDao provideSyncDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSyncDao(db));
  }
}
