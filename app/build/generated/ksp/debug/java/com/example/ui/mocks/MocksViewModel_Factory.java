package com.example.ui.mocks;

import android.content.Context;
import com.example.data.MockRepository;
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
public final class MocksViewModel_Factory implements Factory<MocksViewModel> {
  private final Provider<Context> applicationProvider;

  private final Provider<MockRepository> repositoryProvider;

  public MocksViewModel_Factory(Provider<Context> applicationProvider,
      Provider<MockRepository> repositoryProvider) {
    this.applicationProvider = applicationProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public MocksViewModel get() {
    return newInstance(applicationProvider.get(), repositoryProvider.get());
  }

  public static MocksViewModel_Factory create(Provider<Context> applicationProvider,
      Provider<MockRepository> repositoryProvider) {
    return new MocksViewModel_Factory(applicationProvider, repositoryProvider);
  }

  public static MocksViewModel newInstance(Context application, MockRepository repository) {
    return new MocksViewModel(application, repository);
  }
}
