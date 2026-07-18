package com.example.di;

import android.content.Context;
import com.example.data.AppDatabase;
import com.example.data.BackupRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class RepositoryModule_ProvideBackupRepositoryFactory implements Factory<BackupRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<AppDatabase> dbProvider;

  public RepositoryModule_ProvideBackupRepositoryFactory(Provider<Context> contextProvider,
      Provider<AppDatabase> dbProvider) {
    this.contextProvider = contextProvider;
    this.dbProvider = dbProvider;
  }

  @Override
  public BackupRepository get() {
    return provideBackupRepository(contextProvider.get(), dbProvider.get());
  }

  public static RepositoryModule_ProvideBackupRepositoryFactory create(
      Provider<Context> contextProvider, Provider<AppDatabase> dbProvider) {
    return new RepositoryModule_ProvideBackupRepositoryFactory(contextProvider, dbProvider);
  }

  public static BackupRepository provideBackupRepository(Context context, AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideBackupRepository(context, db));
  }
}
