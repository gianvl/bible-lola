package com.lolabible.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lolabible.app.BibleApplication
import com.lolabible.app.data.Settings
import com.lolabible.app.data.SettingsRepository
import com.lolabible.app.data.Translation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Holds the app-wide settings. `settings` is null until they have been read from disk. */
class AppViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val settings: StateFlow<Settings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    fun setTranslation(translation: Translation) {
        viewModelScope.launch { settingsRepository.setTranslation(translation) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as BibleApplication
                AppViewModel(app.container.settingsRepository)
            }
        }
    }
}
