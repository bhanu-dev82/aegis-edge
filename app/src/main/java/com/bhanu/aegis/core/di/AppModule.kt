package com.bhanu.aegis.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.bhanu.aegis.core.data.datastore.ChakuliSettings
import com.bhanu.aegis.core.data.datastore.ChakuliSettingsSerializer
import com.bhanu.aegis.core.data.repository.ModelRepository
import com.bhanu.aegis.core.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideChakuliSettingsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<ChakuliSettings> =
        DataStoreFactory.create(
            serializer = ChakuliSettingsSerializer,
            produceFile = { context.dataStoreFile("chakuli_settings.pb") },
        )

    @Provides
    @Singleton
    fun provideSettingsRepository(
        dataStore: DataStore<ChakuliSettings>,
    ): SettingsRepository = SettingsRepository(dataStore)

    @Provides
    @Singleton
    fun provideModelRepository(
        @ApplicationContext context: Context,
    ): ModelRepository = ModelRepository(context)
}
