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
public final class SmartNotificationWorker_AssistedFactory_Impl implements SmartNotificationWorker_AssistedFactory {
  private final SmartNotificationWorker_Factory delegateFactory;

  SmartNotificationWorker_AssistedFactory_Impl(SmartNotificationWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public SmartNotificationWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<SmartNotificationWorker_AssistedFactory> create(
      SmartNotificationWorker_Factory delegateFactory) {
    return InstanceFactory.create(new SmartNotificationWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<SmartNotificationWorker_AssistedFactory> createFactoryProvider(
      SmartNotificationWorker_Factory delegateFactory) {
    return InstanceFactory.create(new SmartNotificationWorker_AssistedFactory_Impl(delegateFactory));
  }
}
