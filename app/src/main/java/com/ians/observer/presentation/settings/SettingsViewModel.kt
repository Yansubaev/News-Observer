package com.ians.observer.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingRepository,
) : ViewModel() {

    val selectedRegionState: StateFlow<NewsCountry> =
        settingsRepository.observeCountryPreference()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NewsCountry.US
            )

    val selectedLanguageState: StateFlow<NewsLanguage> =
        settingsRepository.observeLanguagePreference()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NewsLanguage.EN
            )

    fun selectRegion(region: NewsCountry) {
        viewModelScope.launch {
            settingsRepository.setCountryPreference(region.code)
        }
    }

    fun selectLanguage(language: NewsLanguage) {
        viewModelScope.launch {
            settingsRepository.setLanguagePreference(language.code)
        }
    }
}