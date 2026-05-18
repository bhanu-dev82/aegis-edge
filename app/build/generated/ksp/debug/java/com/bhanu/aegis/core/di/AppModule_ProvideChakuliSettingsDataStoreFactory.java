package com.bhanu.aegis.core.di;

import android.content.Context;
import androidx.datastore.core.DataStore;
import com.bhanu.aegis.core.data.datastore.ChakuliSettings;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideChakuliSettingsDataStoreFactory implements Factory<DataStore<ChakuliSettings>> {
  private final Provider<Context> contextProvider;

  private AppModule_ProvideChakuliSettingsDataStoreFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DataStore<ChakuliSettings> get() {
    return provideChakuliSettingsDataStore(contextProvider.get());
  }

  public static AppModule_ProvideChakuliSettingsDataStoreFactory create(
      Provider<Context> contextProvider) {
    return new AppModule_ProvideChakuliSettingsDataStoreFactory(contextProvider);
  }

  public static DataStore<ChakuliSettings> provideChakuliSettingsDataStore(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideChakuliSettingsDataStore(context));
  }
}
