package com.bhanu.aegis.core.data.repository

import androidx.datastore.core.DataStore
import com.bhanu.aegis.core.data.datastore.ChakuliSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class EngineMode { AUTO, PERFORMANCE, MEMORY_SAVER }

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<ChakuliSettings>,
) {
    val themeMode: Flow<ThemeMode> = dataStore.data
        .catch { if (it is IOException) emit(ChakuliSettings.getDefaultInstance()) else throw it }
        .map { settings ->
            when (settings.themeMode) {
                1    -> ThemeMode.LIGHT
                2    -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.updateData { current ->
            current.toBuilder()
                .setThemeMode(when (mode) {
                    ThemeMode.LIGHT  -> 1
                    ThemeMode.DARK   -> 2
                    ThemeMode.SYSTEM -> 0
                })
                .build()
        }
    }

    val engineMode: Flow<EngineMode> = dataStore.data
        .catch { if (it is IOException) emit(ChakuliSettings.getDefaultInstance()) else throw it }
        .map { settings ->
            when (settings.engineMode) {
                1    -> EngineMode.PERFORMANCE
                2    -> EngineMode.MEMORY_SAVER
                else -> EngineMode.AUTO
            }
        }

    suspend fun setEngineMode(mode: EngineMode) {
        dataStore.updateData { current ->
            current.toBuilder()
                .setEngineMode(when (mode) {
                    EngineMode.PERFORMANCE  -> 1
                    EngineMode.MEMORY_SAVER -> 2
                    EngineMode.AUTO         -> 0
                })
                .build()
        }
    }

    val huggingFaceToken: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(ChakuliSettings.getDefaultInstance()) else throw it }
        .map { it.huggingFaceToken }

    suspend fun setHuggingFaceToken(token: String) {
        dataStore.updateData { current ->
            current.toBuilder().setHuggingFaceToken(token.trim()).build()
        }
    }
}
