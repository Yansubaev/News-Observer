package com.ians.observer.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.repository.NewsProvidersRepository
import com.ians.observer.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    newsProvidersRepository: NewsProvidersRepository,
) : ViewModel() {

    val feedProviderIds = newsProvidersRepository.getTopHeadlinesCapableProviderIds()
    val searchProviderIds = newsProvidersRepository.getSearchCapableProviderIds()
    val availableProviderIds = newsProvidersRepository.getAvailableProviderIds()

    val selectedRegionState = settingsRepository.observeCountryPreference()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NewsCountry.US
        )

    val selectedLanguageState = settingsRepository.observeLanguagePreference()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NewsLanguage.EN
        )

    val selectedFeedProvider = settingsRepository.observeFeedProviderPreference()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProviderId.NEWS_DATA
        )

    val enabledSearchProviderIds = settingsRepository.observeEnabledSearchProviderIds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = searchProviderIds
        )

    fun selectCountry(region: NewsCountry) = viewModelScope.launch {
        settingsRepository.setCountryPreference(region)
    }

    fun selectLanguage(language: NewsLanguage) = viewModelScope.launch {
        settingsRepository.setLanguagePreference(language)
    }

    fun selectFeedProvider(providerId: ProviderId) = viewModelScope.launch {
        settingsRepository.setFeedProviderPreference(providerId)
    }

    fun setSearchProviderEnabled(providerId: ProviderId, enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setSearchProviderEnabled(providerId, enabled)
    }

}
