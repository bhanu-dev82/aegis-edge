package com.bhanu.aegis.core.data.repository;

import androidx.datastore.core.DataStore;
import com.bhanu.aegis.core.data.datastore.ChakuliSettings;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class SettingsRepository_Factory implements Factory<SettingsRepository> {
  private final Provider<DataStore<ChakuliSettings>> dataStoreProvider;

  private SettingsRepository_Factory(Provider<DataStore<ChakuliSettings>> dataStoreProvider) {
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public SettingsRepository get() {
    return newInstance(dataStoreProvider.get());
  }

  public static SettingsRepository_Factory create(
      Provider<DataStore<ChakuliSettings>> dataStoreProvider) {
    return new SettingsRepository_Factory(dataStoreProvider);
  }

  public static SettingsRepository newInstance(DataStore<ChakuliSettings> dataStore) {
    return new SettingsRepository(dataStore);
  }
}
