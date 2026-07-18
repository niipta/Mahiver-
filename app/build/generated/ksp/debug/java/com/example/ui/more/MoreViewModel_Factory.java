package com.example.ui.more;

import com.example.data.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class MoreViewModel_Factory implements Factory<MoreViewModel> {
  private final Provider<SettingsRepository> settingsProvider;

  public MoreViewModel_Factory(Provider<SettingsRepository> settingsProvider) {
    this.settingsProvider = settingsProvider;
  }

  @Override
  public MoreViewModel get() {
    return newInstance(settingsProvider.get());
  }

  public static MoreViewModel_Factory create(Provider<SettingsRepository> settingsProvider) {
    return new MoreViewModel_Factory(settingsProvider);
  }

  public static MoreViewModel newInstance(SettingsRepository settings) {
    return new MoreViewModel(settings);
  }
}
