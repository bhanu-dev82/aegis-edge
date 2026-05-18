package com.bhanu.aegis.core.di;

import androidx.datastore.core.DataStore;
import com.bhanu.aegis.core.data.datastore.ChakuliSettings;
import com.bhanu.aegis.core.data.repository.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class AppModule_ProvideSettingsRepositoryFactory implements Factory<SettingsRepository> {
  private final Provider<DataStore<ChakuliSettings>> dataStoreProvider;

  private AppModule_ProvideSettingsRepositoryFactory(
      Provider<DataStore<ChakuliSettings>> dataStoreProvider) {
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public SettingsRepository get() {
    return provideSettingsRepository(dataStoreProvider.get());
  }

  public static AppModule_ProvideSettingsRepositoryFactory create(
      Provider<DataStore<ChakuliSettings>> dataStoreProvider) {
    return new AppModule_ProvideSettingsRepositoryFactory(dataStoreProvider);
  }

  public static SettingsRepository provideSettingsRepository(DataStore<ChakuliSettings> dataStore) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSettingsRepository(dataStore));
  }
}
