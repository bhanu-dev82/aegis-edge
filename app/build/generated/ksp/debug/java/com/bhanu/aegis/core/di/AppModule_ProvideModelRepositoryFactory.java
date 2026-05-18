package com.bhanu.aegis.core.di;

import android.content.Context;
import com.bhanu.aegis.core.data.repository.ModelRepository;
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
public final class AppModule_ProvideModelRepositoryFactory implements Factory<ModelRepository> {
  private final Provider<Context> contextProvider;

  private AppModule_ProvideModelRepositoryFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ModelRepository get() {
    return provideModelRepository(contextProvider.get());
  }

  public static AppModule_ProvideModelRepositoryFactory create(Provider<Context> contextProvider) {
    return new AppModule_ProvideModelRepositoryFactory(contextProvider);
  }

  public static ModelRepository provideModelRepository(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideModelRepository(context));
  }
}
