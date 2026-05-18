package com.bhanu.aegis.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.aegis.core.data.repository.EngineMode
import com.bhanu.aegis.core.data.repository.SettingsRepository
import com.bhanu.aegis.core.data.repository.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val themeMode = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    val engineMode = settingsRepository.engineMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EngineMode.AUTO)

    fun setEngineMode(mode: EngineMode) {
        viewModelScope.launch { settingsRepository.setEngineMode(mode) }
    }
}
