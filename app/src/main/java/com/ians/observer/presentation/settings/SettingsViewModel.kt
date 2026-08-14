package com.ians.observer.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.NewsRegion
import com.ians.observer.domain.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingRepository,
) : ViewModel() {

    private val _selectedRegionState = MutableStateFlow(NewsRegion.US)
    val selectedRegionState: StateFlow<NewsRegion> = _selectedRegionState.asStateFlow()

    private val _selectedLanguageState = MutableStateFlow(NewsLanguage.EN)
    val selectedLanguageState: StateFlow<NewsLanguage> = _selectedLanguageState.asStateFlow()

    init {
        viewModelScope.launch {
            actualizeSelectedRegion()
            actualizeSelectedLanguage()
        }
    }

    fun selectRegion(region: NewsRegion) {
        viewModelScope.launch {
            settingsRepository.setCountryPreference(region.code)
            actualizeSelectedRegion()
        }
    }

    fun selectLanguage(language: NewsLanguage) {
        viewModelScope.launch {
            settingsRepository.setLanguagePreference(language.code)
            actualizeSelectedLanguage()
        }
    }

    private suspend fun actualizeSelectedLanguage() {
        val setting = settingsRepository.getLanguagePreference()
        try {
            _selectedLanguageState.value = NewsLanguage.fromCode(setting)
        } catch (e: IllegalArgumentException) {
            _selectedLanguageState.value = NewsLanguage.EN
            println(e)
        }
    }

    private suspend fun actualizeSelectedRegion() {
        val setting = settingsRepository.getCountryPreference()
        try {
            _selectedRegionState.value = NewsRegion.fromCode(setting)
        } catch (e: IllegalArgumentException) {
            _selectedRegionState.value = NewsRegion.US
            println(e)
        }
    }
}