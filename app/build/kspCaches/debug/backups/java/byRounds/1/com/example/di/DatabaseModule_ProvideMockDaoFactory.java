package com.example.di;

import com.example.data.AppDatabase;
import com.example.data.MockDao;
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
public final class DatabaseModule_ProvideMockDaoFactory implements Factory<MockDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideMockDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MockDao get() {
    return provideMockDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideMockDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideMockDaoFactory(dbProvider);
  }

  public static MockDao provideMockDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideMockDao(db));
  }
}
