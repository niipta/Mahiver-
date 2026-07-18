package com.example.ui.focus;

import android.content.Context;
import com.example.data.FocusRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class FocusViewModel_Factory implements Factory<FocusViewModel> {
  private final Provider<Context> applicationProvider;

  private final Provider<FocusRepository> repositoryProvider;

  public FocusViewModel_Factory(Provider<Context> applicationProvider,
      Provider<FocusRepository> repositoryProvider) {
    this.applicationProvider = applicationProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public FocusViewModel get() {
    return newInstance(applicationProvider.get(), repositoryProvider.get());
  }

  public static FocusViewModel_Factory create(Provider<Context> applicationProvider,
      Provider<FocusRepository> repositoryProvider) {
    return new FocusViewModel_Factory(applicationProvider, repositoryProvider);
  }

  public static FocusViewModel newInstance(Context application, FocusRepository repository) {
    return new FocusViewModel(application, repository);
  }
}
