package com.example.widget;

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
public final class MahirWidgetWorker_AssistedFactory_Impl implements MahirWidgetWorker_AssistedFactory {
  private final MahirWidgetWorker_Factory delegateFactory;

  MahirWidgetWorker_AssistedFactory_Impl(MahirWidgetWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public MahirWidgetWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<MahirWidgetWorker_AssistedFactory> create(
      MahirWidgetWorker_Factory delegateFactory) {
    return InstanceFactory.create(new MahirWidgetWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<MahirWidgetWorker_AssistedFactory> createFactoryProvider(
      MahirWidgetWorker_Factory delegateFactory) {
    return InstanceFactory.create(new MahirWidgetWorker_AssistedFactory_Impl(delegateFactory));
  }
}
