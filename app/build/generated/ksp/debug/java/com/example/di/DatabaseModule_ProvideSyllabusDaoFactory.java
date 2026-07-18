package com.example.di;

import com.example.data.AppDatabase;
import com.example.data.SyllabusDao;
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
public final class DatabaseModule_ProvideSyllabusDaoFactory implements Factory<SyllabusDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideSyllabusDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SyllabusDao get() {
    return provideSyllabusDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideSyllabusDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideSyllabusDaoFactory(dbProvider);
  }

  public static SyllabusDao provideSyllabusDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSyllabusDao(db));
  }
}
