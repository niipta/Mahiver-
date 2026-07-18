package com.example.widget;

import androidx.hilt.work.WorkerAssistedFactory;
import androidx.work.ListenableWorker;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.annotation.processing.Generated;

@Generated("androidx.hilt.AndroidXHiltProcessor")
@Module
@InstallIn(SingletonComponent.class)
@OriginatingElement(
    topLevelClass = MahirWidgetWorker.class
)
public interface MahirWidgetWorker_HiltModule {
  @Binds
  @IntoMap
  @StringKey("com.example.widget.MahirWidgetWorker")
  WorkerAssistedFactory<? extends ListenableWorker> bind(MahirWidgetWorker_AssistedFactory factory);
}
