package com.bhanu.aegis.core.di

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.WorkerFactory
import com.bhanu.aegis.feature.modelmanager.ModelDownloadWorker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

// WorkManager Hilt integration — required for @HiltWorker or standard workers injected via Hilt
@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    // No @HiltWorker used for now (ModelDownloadWorker is a standard CoroutineWorker).
    // This placeholder module ensures the hilt-work dependency is in scope.
    // If @HiltWorker is added later, bind HiltWorkerFactory here.
}
