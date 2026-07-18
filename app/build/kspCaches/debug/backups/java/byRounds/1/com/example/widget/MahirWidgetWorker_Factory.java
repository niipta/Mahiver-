package com.example.widget;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class MahirWidgetWorker_Factory {
  public MahirWidgetWorker_Factory() {
  }

  public MahirWidgetWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams);
  }

  public static MahirWidgetWorker_Factory create() {
    return new MahirWidgetWorker_Factory();
  }

  public static MahirWidgetWorker newInstance(Context appContext, WorkerParameters workerParams) {
    return new MahirWidgetWorker(appContext, workerParams);
  }
}
