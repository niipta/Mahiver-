package com.example;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.hilt.work.WorkerAssistedFactory;
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.example.data.AppDatabase;
import com.example.data.BackupRepository;
import com.example.data.ExamDao;
import com.example.data.FocusDao;
import com.example.data.FocusRepository;
import com.example.data.MockRepository;
import com.example.data.PlannerDao;
import com.example.data.PlannerRepository;
import com.example.data.RevisionDao;
import com.example.data.RevisionRepository;
import com.example.data.SettingsRepository;
import com.example.data.SyllabusDao;
import com.example.data.SyllabusRepository;
import com.example.data.sync.SyncDao;
import com.example.data.sync.SyncWorker;
import com.example.data.sync.SyncWorker_AssistedFactory;
import com.example.di.DatabaseModule_ProvideAppDatabaseFactory;
import com.example.di.DatabaseModule_ProvideExamDaoFactory;
import com.example.di.DatabaseModule_ProvideFocusDaoFactory;
import com.example.di.DatabaseModule_ProvidePlannerDaoFactory;
import com.example.di.DatabaseModule_ProvideRevisionDaoFactory;
import com.example.di.DatabaseModule_ProvideSyllabusDaoFactory;
import com.example.di.DatabaseModule_ProvideSyncDaoFactory;
import com.example.di.RepositoryModule_ProvideBackupRepositoryFactory;
import com.example.di.RepositoryModule_ProvideFocusRepositoryFactory;
import com.example.di.RepositoryModule_ProvideMockRepositoryFactory;
import com.example.di.RepositoryModule_ProvidePlannerRepositoryFactory;
import com.example.di.RepositoryModule_ProvideRevisionRepositoryFactory;
import com.example.di.RepositoryModule_ProvideSettingsRepositoryFactory;
import com.example.di.RepositoryModule_ProvideSyllabusRepositoryFactory;
import com.example.service.AutoBackupWorker;
import com.example.service.AutoBackupWorker_AssistedFactory;
import com.example.service.SmartNotificationWorker;
import com.example.service.SmartNotificationWorker_AssistedFactory;
import com.example.service.StreakWorker;
import com.example.service.StreakWorker_AssistedFactory;
import com.example.ui.analytics.AnalyticsViewModel;
import com.example.ui.analytics.AnalyticsViewModel_HiltModules;
import com.example.ui.backup.BackupRestoreViewModel;
import com.example.ui.backup.BackupRestoreViewModel_HiltModules;
import com.example.ui.focus.FocusViewModel;
import com.example.ui.focus.FocusViewModel_HiltModules;
import com.example.ui.history.StudyHistoryViewModel;
import com.example.ui.history.StudyHistoryViewModel_HiltModules;
import com.example.ui.home.HomeViewModel;
import com.example.ui.home.HomeViewModel_HiltModules;
import com.example.ui.mocks.MocksViewModel;
import com.example.ui.mocks.MocksViewModel_HiltModules;
import com.example.ui.more.MoreViewModel;
import com.example.ui.more.MoreViewModel_HiltModules;
import com.example.ui.planner.PlannerViewModel;
import com.example.ui.planner.PlannerViewModel_HiltModules;
import com.example.ui.revision.RevisionViewModel;
import com.example.ui.revision.RevisionViewModel_HiltModules;
import com.example.ui.syllabus.SyllabusViewModel;
import com.example.ui.syllabus.SyllabusViewModel_HiltModules;
import com.example.widget.MahirWidgetWorker;
import com.example.widget.MahirWidgetWorker_AssistedFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SingleCheck;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerMahirVerseApplication_HiltComponents_SingletonC {
  private DaggerMahirVerseApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public MahirVerseApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements MahirVerseApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public MahirVerseApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements MahirVerseApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public MahirVerseApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements MahirVerseApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public MahirVerseApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements MahirVerseApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public MahirVerseApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements MahirVerseApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public MahirVerseApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements MahirVerseApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public MahirVerseApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements MahirVerseApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public MahirVerseApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends MahirVerseApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends MahirVerseApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends MahirVerseApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends MahirVerseApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(ImmutableMap.<String, Boolean>builderWithExpectedSize(10).put(LazyClassKeyProvider.com_example_ui_analytics_AnalyticsViewModel, AnalyticsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_ui_backup_BackupRestoreViewModel, BackupRestoreViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_ui_focus_FocusViewModel, FocusViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_ui_home_HomeViewModel, HomeViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_ui_mocks_MocksViewModel, MocksViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_ui_more_MoreViewModel, MoreViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_ui_planner_PlannerViewModel, PlannerViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_ui_revision_RevisionViewModel, RevisionViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_ui_history_StudyHistoryViewModel, StudyHistoryViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_ui_syllabus_SyllabusViewModel, SyllabusViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_example_ui_history_StudyHistoryViewModel = "com.example.ui.history.StudyHistoryViewModel";

      static String com_example_ui_backup_BackupRestoreViewModel = "com.example.ui.backup.BackupRestoreViewModel";

      static String com_example_ui_home_HomeViewModel = "com.example.ui.home.HomeViewModel";

      static String com_example_ui_analytics_AnalyticsViewModel = "com.example.ui.analytics.AnalyticsViewModel";

      static String com_example_ui_planner_PlannerViewModel = "com.example.ui.planner.PlannerViewModel";

      static String com_example_ui_syllabus_SyllabusViewModel = "com.example.ui.syllabus.SyllabusViewModel";

      static String com_example_ui_focus_FocusViewModel = "com.example.ui.focus.FocusViewModel";

      static String com_example_ui_more_MoreViewModel = "com.example.ui.more.MoreViewModel";

      static String com_example_ui_revision_RevisionViewModel = "com.example.ui.revision.RevisionViewModel";

      static String com_example_ui_mocks_MocksViewModel = "com.example.ui.mocks.MocksViewModel";

      @KeepFieldType
      StudyHistoryViewModel com_example_ui_history_StudyHistoryViewModel2;

      @KeepFieldType
      BackupRestoreViewModel com_example_ui_backup_BackupRestoreViewModel2;

      @KeepFieldType
      HomeViewModel com_example_ui_home_HomeViewModel2;

      @KeepFieldType
      AnalyticsViewModel com_example_ui_analytics_AnalyticsViewModel2;

      @KeepFieldType
      PlannerViewModel com_example_ui_planner_PlannerViewModel2;

      @KeepFieldType
      SyllabusViewModel com_example_ui_syllabus_SyllabusViewModel2;

      @KeepFieldType
      FocusViewModel com_example_ui_focus_FocusViewModel2;

      @KeepFieldType
      MoreViewModel com_example_ui_more_MoreViewModel2;

      @KeepFieldType
      RevisionViewModel com_example_ui_revision_RevisionViewModel2;

      @KeepFieldType
      MocksViewModel com_example_ui_mocks_MocksViewModel2;
    }
  }

  private static final class ViewModelCImpl extends MahirVerseApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AnalyticsViewModel> analyticsViewModelProvider;

    private Provider<BackupRestoreViewModel> backupRestoreViewModelProvider;

    private Provider<FocusViewModel> focusViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<MocksViewModel> mocksViewModelProvider;

    private Provider<MoreViewModel> moreViewModelProvider;

    private Provider<PlannerViewModel> plannerViewModelProvider;

    private Provider<RevisionViewModel> revisionViewModelProvider;

    private Provider<StudyHistoryViewModel> studyHistoryViewModelProvider;

    private Provider<SyllabusViewModel> syllabusViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.analyticsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.backupRestoreViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.focusViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.mocksViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.moreViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.plannerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.revisionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.studyHistoryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.syllabusViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(ImmutableMap.<String, javax.inject.Provider<ViewModel>>builderWithExpectedSize(10).put(LazyClassKeyProvider.com_example_ui_analytics_AnalyticsViewModel, ((Provider) analyticsViewModelProvider)).put(LazyClassKeyProvider.com_example_ui_backup_BackupRestoreViewModel, ((Provider) backupRestoreViewModelProvider)).put(LazyClassKeyProvider.com_example_ui_focus_FocusViewModel, ((Provider) focusViewModelProvider)).put(LazyClassKeyProvider.com_example_ui_home_HomeViewModel, ((Provider) homeViewModelProvider)).put(LazyClassKeyProvider.com_example_ui_mocks_MocksViewModel, ((Provider) mocksViewModelProvider)).put(LazyClassKeyProvider.com_example_ui_more_MoreViewModel, ((Provider) moreViewModelProvider)).put(LazyClassKeyProvider.com_example_ui_planner_PlannerViewModel, ((Provider) plannerViewModelProvider)).put(LazyClassKeyProvider.com_example_ui_revision_RevisionViewModel, ((Provider) revisionViewModelProvider)).put(LazyClassKeyProvider.com_example_ui_history_StudyHistoryViewModel, ((Provider) studyHistoryViewModelProvider)).put(LazyClassKeyProvider.com_example_ui_syllabus_SyllabusViewModel, ((Provider) syllabusViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return ImmutableMap.<Class<?>, Object>of();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_example_ui_more_MoreViewModel = "com.example.ui.more.MoreViewModel";

      static String com_example_ui_analytics_AnalyticsViewModel = "com.example.ui.analytics.AnalyticsViewModel";

      static String com_example_ui_focus_FocusViewModel = "com.example.ui.focus.FocusViewModel";

      static String com_example_ui_planner_PlannerViewModel = "com.example.ui.planner.PlannerViewModel";

      static String com_example_ui_history_StudyHistoryViewModel = "com.example.ui.history.StudyHistoryViewModel";

      static String com_example_ui_backup_BackupRestoreViewModel = "com.example.ui.backup.BackupRestoreViewModel";

      static String com_example_ui_mocks_MocksViewModel = "com.example.ui.mocks.MocksViewModel";

      static String com_example_ui_syllabus_SyllabusViewModel = "com.example.ui.syllabus.SyllabusViewModel";

      static String com_example_ui_home_HomeViewModel = "com.example.ui.home.HomeViewModel";

      static String com_example_ui_revision_RevisionViewModel = "com.example.ui.revision.RevisionViewModel";

      @KeepFieldType
      MoreViewModel com_example_ui_more_MoreViewModel2;

      @KeepFieldType
      AnalyticsViewModel com_example_ui_analytics_AnalyticsViewModel2;

      @KeepFieldType
      FocusViewModel com_example_ui_focus_FocusViewModel2;

      @KeepFieldType
      PlannerViewModel com_example_ui_planner_PlannerViewModel2;

      @KeepFieldType
      StudyHistoryViewModel com_example_ui_history_StudyHistoryViewModel2;

      @KeepFieldType
      BackupRestoreViewModel com_example_ui_backup_BackupRestoreViewModel2;

      @KeepFieldType
      MocksViewModel com_example_ui_mocks_MocksViewModel2;

      @KeepFieldType
      SyllabusViewModel com_example_ui_syllabus_SyllabusViewModel2;

      @KeepFieldType
      HomeViewModel com_example_ui_home_HomeViewModel2;

      @KeepFieldType
      RevisionViewModel com_example_ui_revision_RevisionViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.example.ui.analytics.AnalyticsViewModel 
          return (T) new AnalyticsViewModel(singletonCImpl.syllabusDao(), singletonCImpl.revisionDao(), singletonCImpl.focusDao(), singletonCImpl.provideSettingsRepositoryProvider.get());

          case 1: // com.example.ui.backup.BackupRestoreViewModel 
          return (T) new BackupRestoreViewModel(singletonCImpl.provideBackupRepositoryProvider.get());

          case 2: // com.example.ui.focus.FocusViewModel 
          return (T) new FocusViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideFocusRepositoryProvider.get());

          case 3: // com.example.ui.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.syllabusDao(), singletonCImpl.revisionDao(), singletonCImpl.focusDao(), singletonCImpl.examDao(), singletonCImpl.plannerDao(), singletonCImpl.syncDao(), singletonCImpl.provideSettingsRepositoryProvider.get());

          case 4: // com.example.ui.mocks.MocksViewModel 
          return (T) new MocksViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideMockRepositoryProvider.get());

          case 5: // com.example.ui.more.MoreViewModel 
          return (T) new MoreViewModel(singletonCImpl.provideSettingsRepositoryProvider.get());

          case 6: // com.example.ui.planner.PlannerViewModel 
          return (T) new PlannerViewModel(singletonCImpl.syllabusDao(), singletonCImpl.revisionDao(), singletonCImpl.plannerDao(), singletonCImpl.syncDao(), singletonCImpl.providePlannerRepositoryProvider.get());

          case 7: // com.example.ui.revision.RevisionViewModel 
          return (T) new RevisionViewModel(singletonCImpl.provideRevisionRepositoryProvider.get());

          case 8: // com.example.ui.history.StudyHistoryViewModel 
          return (T) new StudyHistoryViewModel(singletonCImpl.focusDao(), singletonCImpl.syllabusDao(), singletonCImpl.revisionDao(), singletonCImpl.syncDao());

          case 9: // com.example.ui.syllabus.SyllabusViewModel 
          return (T) new SyllabusViewModel(singletonCImpl.provideSyllabusRepositoryProvider.get(), singletonCImpl.syllabusDao(), singletonCImpl.revisionDao(), singletonCImpl.provideRevisionRepositoryProvider.get(), singletonCImpl.examDao(), singletonCImpl.syncDao());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends MahirVerseApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends MahirVerseApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends MahirVerseApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<AppDatabase> provideAppDatabaseProvider;

    private Provider<BackupRepository> provideBackupRepositoryProvider;

    private Provider<SettingsRepository> provideSettingsRepositoryProvider;

    private Provider<AutoBackupWorker_AssistedFactory> autoBackupWorker_AssistedFactoryProvider;

    private Provider<MahirWidgetWorker_AssistedFactory> mahirWidgetWorker_AssistedFactoryProvider;

    private Provider<SmartNotificationWorker_AssistedFactory> smartNotificationWorker_AssistedFactoryProvider;

    private Provider<StreakWorker_AssistedFactory> streakWorker_AssistedFactoryProvider;

    private Provider<SyncWorker_AssistedFactory> syncWorker_AssistedFactoryProvider;

    private Provider<FocusRepository> provideFocusRepositoryProvider;

    private Provider<MockRepository> provideMockRepositoryProvider;

    private Provider<PlannerRepository> providePlannerRepositoryProvider;

    private Provider<RevisionRepository> provideRevisionRepositoryProvider;

    private Provider<SyllabusRepository> provideSyllabusRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private FocusDao focusDao() {
      return DatabaseModule_ProvideFocusDaoFactory.provideFocusDao(provideAppDatabaseProvider.get());
    }

    private SyncDao syncDao() {
      return DatabaseModule_ProvideSyncDaoFactory.provideSyncDao(provideAppDatabaseProvider.get());
    }

    private Map<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>> mapOfStringAndProviderOfWorkerAssistedFactoryOf(
        ) {
      return ImmutableMap.<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>>of("com.example.service.AutoBackupWorker", ((Provider) autoBackupWorker_AssistedFactoryProvider), "com.example.widget.MahirWidgetWorker", ((Provider) mahirWidgetWorker_AssistedFactoryProvider), "com.example.service.SmartNotificationWorker", ((Provider) smartNotificationWorker_AssistedFactoryProvider), "com.example.service.StreakWorker", ((Provider) streakWorker_AssistedFactoryProvider), "com.example.data.sync.SyncWorker", ((Provider) syncWorker_AssistedFactoryProvider));
    }

    private HiltWorkerFactory hiltWorkerFactory() {
      return WorkerFactoryModule_ProvideFactoryFactory.provideFactory(mapOfStringAndProviderOfWorkerAssistedFactoryOf());
    }

    private SyllabusDao syllabusDao() {
      return DatabaseModule_ProvideSyllabusDaoFactory.provideSyllabusDao(provideAppDatabaseProvider.get());
    }

    private RevisionDao revisionDao() {
      return DatabaseModule_ProvideRevisionDaoFactory.provideRevisionDao(provideAppDatabaseProvider.get());
    }

    private ExamDao examDao() {
      return DatabaseModule_ProvideExamDaoFactory.provideExamDao(provideAppDatabaseProvider.get());
    }

    private PlannerDao plannerDao() {
      return DatabaseModule_ProvidePlannerDaoFactory.providePlannerDao(provideAppDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideAppDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 2));
      this.provideBackupRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<BackupRepository>(singletonCImpl, 1));
      this.provideSettingsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SettingsRepository>(singletonCImpl, 3));
      this.autoBackupWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<AutoBackupWorker_AssistedFactory>(singletonCImpl, 0));
      this.mahirWidgetWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<MahirWidgetWorker_AssistedFactory>(singletonCImpl, 4));
      this.smartNotificationWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<SmartNotificationWorker_AssistedFactory>(singletonCImpl, 5));
      this.streakWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<StreakWorker_AssistedFactory>(singletonCImpl, 6));
      this.syncWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<SyncWorker_AssistedFactory>(singletonCImpl, 7));
      this.provideFocusRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<FocusRepository>(singletonCImpl, 8));
      this.provideMockRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<MockRepository>(singletonCImpl, 9));
      this.providePlannerRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<PlannerRepository>(singletonCImpl, 10));
      this.provideRevisionRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<RevisionRepository>(singletonCImpl, 11));
      this.provideSyllabusRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SyllabusRepository>(singletonCImpl, 12));
    }

    @Override
    public void injectMahirVerseApplication(MahirVerseApplication mahirVerseApplication) {
      injectMahirVerseApplication2(mahirVerseApplication);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private MahirVerseApplication injectMahirVerseApplication2(MahirVerseApplication instance) {
      MahirVerseApplication_MembersInjector.injectWorkerFactory(instance, hiltWorkerFactory());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.example.service.AutoBackupWorker_AssistedFactory 
          return (T) new AutoBackupWorker_AssistedFactory() {
            @Override
            public AutoBackupWorker create(Context appContext, WorkerParameters workerParams) {
              return new AutoBackupWorker(appContext, workerParams, singletonCImpl.provideBackupRepositoryProvider.get(), singletonCImpl.provideSettingsRepositoryProvider.get());
            }
          };

          case 1: // com.example.data.BackupRepository 
          return (T) RepositoryModule_ProvideBackupRepositoryFactory.provideBackupRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideAppDatabaseProvider.get());

          case 2: // com.example.data.AppDatabase 
          return (T) DatabaseModule_ProvideAppDatabaseFactory.provideAppDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.example.data.SettingsRepository 
          return (T) RepositoryModule_ProvideSettingsRepositoryFactory.provideSettingsRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.example.widget.MahirWidgetWorker_AssistedFactory 
          return (T) new MahirWidgetWorker_AssistedFactory() {
            @Override
            public MahirWidgetWorker create(Context appContext2, WorkerParameters workerParams2) {
              return new MahirWidgetWorker(appContext2, workerParams2);
            }
          };

          case 5: // com.example.service.SmartNotificationWorker_AssistedFactory 
          return (T) new SmartNotificationWorker_AssistedFactory() {
            @Override
            public SmartNotificationWorker create(Context context, WorkerParameters params) {
              return new SmartNotificationWorker(context, params, singletonCImpl.provideAppDatabaseProvider.get(), singletonCImpl.provideSettingsRepositoryProvider.get());
            }
          };

          case 6: // com.example.service.StreakWorker_AssistedFactory 
          return (T) new StreakWorker_AssistedFactory() {
            @Override
            public StreakWorker create(Context context2, WorkerParameters params2) {
              return new StreakWorker(context2, params2, singletonCImpl.focusDao(), singletonCImpl.provideSettingsRepositoryProvider.get());
            }
          };

          case 7: // com.example.data.sync.SyncWorker_AssistedFactory 
          return (T) new SyncWorker_AssistedFactory() {
            @Override
            public SyncWorker create(Context appContext3, WorkerParameters workerParams3) {
              return new SyncWorker(appContext3, workerParams3, singletonCImpl.provideAppDatabaseProvider.get(), singletonCImpl.syncDao());
            }
          };

          case 8: // com.example.data.FocusRepository 
          return (T) RepositoryModule_ProvideFocusRepositoryFactory.provideFocusRepository(singletonCImpl.provideAppDatabaseProvider.get(), singletonCImpl.syncDao());

          case 9: // com.example.data.MockRepository 
          return (T) RepositoryModule_ProvideMockRepositoryFactory.provideMockRepository(singletonCImpl.provideAppDatabaseProvider.get(), singletonCImpl.syncDao());

          case 10: // com.example.data.PlannerRepository 
          return (T) RepositoryModule_ProvidePlannerRepositoryFactory.providePlannerRepository(singletonCImpl.provideAppDatabaseProvider.get(), singletonCImpl.syncDao());

          case 11: // com.example.data.RevisionRepository 
          return (T) RepositoryModule_ProvideRevisionRepositoryFactory.provideRevisionRepository(singletonCImpl.provideAppDatabaseProvider.get(), singletonCImpl.syncDao());

          case 12: // com.example.data.SyllabusRepository 
          return (T) RepositoryModule_ProvideSyllabusRepositoryFactory.provideSyllabusRepository(singletonCImpl.provideAppDatabaseProvider.get(), singletonCImpl.syncDao());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
