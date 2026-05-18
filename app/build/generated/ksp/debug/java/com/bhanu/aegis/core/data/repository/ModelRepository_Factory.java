package com.bhanu.aegis.core.data.repository;

import android.content.Context;
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
public final class ModelRepository_Factory implements Factory<ModelRepository> {
  private final Provider<Context> contextProvider;

  private ModelRepository_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ModelRepository get() {
    return newInstance(contextProvider.get());
  }

  public static ModelRepository_Factory create(Provider<Context> contextProvider) {
    return new ModelRepository_Factory(contextProvider);
  }

  public static ModelRepository newInstance(Context context) {
    return new ModelRepository(context);
  }
}
