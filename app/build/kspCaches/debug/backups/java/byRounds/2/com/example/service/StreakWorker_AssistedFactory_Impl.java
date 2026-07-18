package com.example.service;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class StreakWorker_AssistedFactory_Impl implements StreakWorker_AssistedFactory {
  private final StreakWorker_Factory delegateFactory;

  StreakWorker_AssistedFactory_Impl(StreakWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public StreakWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<StreakWorker_AssistedFactory> create(
      StreakWorker_Factory delegateFactory) {
    return InstanceFactory.create(new StreakWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<StreakWorker_AssistedFactory> createFactoryProvider(
      StreakWorker_Factory delegateFactory) {
    return InstanceFactory.create(new StreakWorker_AssistedFactory_Impl(delegateFactory));
  }
}
