package com.bhanu.aegis.core.llm;

import com.bhanu.aegis.core.data.repository.ModelRepository;
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
public final class ModelRouter_Factory implements Factory<ModelRouter> {
  private final Provider<ModelRepository> modelRepoProvider;

  private ModelRouter_Factory(Provider<ModelRepository> modelRepoProvider) {
    this.modelRepoProvider = modelRepoProvider;
  }

  @Override
  public ModelRouter get() {
    return newInstance(modelRepoProvider.get());
  }

  public static ModelRouter_Factory create(Provider<ModelRepository> modelRepoProvider) {
    return new ModelRouter_Factory(modelRepoProvider);
  }

  public static ModelRouter newInstance(ModelRepository modelRepo) {
    return new ModelRouter(modelRepo);
  }
}
