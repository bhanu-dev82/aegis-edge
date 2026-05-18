package com.bhanu.aegis.feature.triage;

import android.content.Context;
import androidx.lifecycle.SavedStateHandle;
import com.bhanu.aegis.core.data.repository.ModelRepository;
import com.bhanu.aegis.core.llm.ModelRouter;
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
public final class TriageViewModel_Factory implements Factory<TriageViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<Context> contextProvider;

  private final Provider<ModelRepository> modelRepositoryProvider;

  private final Provider<ModelRouter> modelRouterProvider;

  private TriageViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<Context> contextProvider, Provider<ModelRepository> modelRepositoryProvider,
      Provider<ModelRouter> modelRouterProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.contextProvider = contextProvider;
    this.modelRepositoryProvider = modelRepositoryProvider;
    this.modelRouterProvider = modelRouterProvider;
  }

  @Override
  public TriageViewModel get() {
    return newInstance(savedStateHandleProvider.get(), contextProvider.get(), modelRepositoryProvider.get(), modelRouterProvider.get());
  }

  public static TriageViewModel_Factory create(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<Context> contextProvider, Provider<ModelRepository> modelRepositoryProvider,
      Provider<ModelRouter> modelRouterProvider) {
    return new TriageViewModel_Factory(savedStateHandleProvider, contextProvider, modelRepositoryProvider, modelRouterProvider);
  }

  public static TriageViewModel newInstance(SavedStateHandle savedStateHandle, Context context,
      ModelRepository modelRepository, ModelRouter modelRouter) {
    return new TriageViewModel(savedStateHandle, context, modelRepository, modelRouter);
  }
}
