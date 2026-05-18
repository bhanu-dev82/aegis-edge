package com.bhanu.aegis.feature.modelmanager;

import android.content.Context;
import com.bhanu.aegis.core.data.repository.ModelRepository;
import com.bhanu.aegis.core.data.repository.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class ModelManagerViewModel_Factory implements Factory<ModelManagerViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<ModelRepository> modelRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private ModelManagerViewModel_Factory(Provider<Context> contextProvider,
      Provider<ModelRepository> modelRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.modelRepositoryProvider = modelRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public ModelManagerViewModel get() {
    return newInstance(contextProvider.get(), modelRepositoryProvider.get(), settingsRepositoryProvider.get());
  }

  public static ModelManagerViewModel_Factory create(Provider<Context> contextProvider,
      Provider<ModelRepository> modelRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new ModelManagerViewModel_Factory(contextProvider, modelRepositoryProvider, settingsRepositoryProvider);
  }

  public static ModelManagerViewModel newInstance(Context context, ModelRepository modelRepository,
      SettingsRepository settingsRepository) {
    return new ModelManagerViewModel(context, modelRepository, settingsRepository);
  }
}
