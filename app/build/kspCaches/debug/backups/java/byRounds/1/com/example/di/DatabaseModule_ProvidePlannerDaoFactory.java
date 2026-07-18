package com.example.di;

import com.example.data.AppDatabase;
import com.example.data.PlannerDao;
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
public final class DatabaseModule_ProvidePlannerDaoFactory implements Factory<PlannerDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvidePlannerDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PlannerDao get() {
    return providePlannerDao(dbProvider.get());
  }

  public static DatabaseModule_ProvidePlannerDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvidePlannerDaoFactory(dbProvider);
  }

  public static PlannerDao providePlannerDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePlannerDao(db));
  }
}
