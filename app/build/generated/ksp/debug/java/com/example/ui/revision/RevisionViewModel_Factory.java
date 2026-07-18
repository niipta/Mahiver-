package com.example.ui.revision;

import com.example.data.RevisionRepository;
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
public final class RevisionViewModel_Factory implements Factory<RevisionViewModel> {
  private final Provider<RevisionRepository> repositoryProvider;

  public RevisionViewModel_Factory(Provider<RevisionRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public RevisionViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static RevisionViewModel_Factory create(Provider<RevisionRepository> repositoryProvider) {
    return new RevisionViewModel_Factory(repositoryProvider);
  }

  public static RevisionViewModel newInstance(RevisionRepository repository) {
    return new RevisionViewModel(repository);
  }
}
