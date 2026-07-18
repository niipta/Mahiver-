package com.example.ui.backup;

import com.example.data.BackupRepository;
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
public final class BackupRestoreViewModel_Factory implements Factory<BackupRestoreViewModel> {
  private final Provider<BackupRepository> repositoryProvider;

  public BackupRestoreViewModel_Factory(Provider<BackupRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public BackupRestoreViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static BackupRestoreViewModel_Factory create(
      Provider<BackupRepository> repositoryProvider) {
    return new BackupRestoreViewModel_Factory(repositoryProvider);
  }

  public static BackupRestoreViewModel newInstance(BackupRepository repository) {
    return new BackupRestoreViewModel(repository);
  }
}
