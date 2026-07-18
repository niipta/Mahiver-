package com.example.data.sync;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.example.data.AppDatabase;
import dagger.internal.DaggerGenerated;
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
public final class SyncWorker_Factory {
  private final Provider<AppDatabase> localDbProvider;

  private final Provider<SyncDao> syncDaoProvider;

  public SyncWorker_Factory(Provider<AppDatabase> localDbProvider,
      Provider<SyncDao> syncDaoProvider) {
    this.localDbProvider = localDbProvider;
    this.syncDaoProvider = syncDaoProvider;
  }

  public SyncWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, localDbProvider.get(), syncDaoProvider.get());
  }

  public static SyncWorker_Factory create(Provider<AppDatabase> localDbProvider,
      Provider<SyncDao> syncDaoProvider) {
    return new SyncWorker_Factory(localDbProvider, syncDaoProvider);
  }

  public static SyncWorker newInstance(Context appContext, WorkerParameters workerParams,
      AppDatabase localDb, SyncDao syncDao) {
    return new SyncWorker(appContext, workerParams, localDb, syncDao);
  }
}
