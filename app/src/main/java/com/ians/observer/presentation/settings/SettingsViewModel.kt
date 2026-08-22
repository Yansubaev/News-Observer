package com.ians.observer.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ians.observer.data.remote.provider.NewsProviderRegistry
import com.ians.observer.data.remote.provider.model.ProviderCapabilities
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingRepository,
    private val providerRegistry: NewsProviderRegistry
) : ViewModel() {

    val feedProviderIds = providerRegistry.idsSupporting(ProviderCapabilities.TOP_HEADLINES)
    val searchProviderIds = providerRegistry.idsSupporting(ProviderCapabilities.SEARCH)

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

    val selectedSearchProvider = settingsRepository.observeSearchProviderPreference()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProviderId.NEWS_DATA
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

    fun selectSearchProvider(providerId: ProviderId) = viewModelScope.launch {
        settingsRepository.setSearchProviderPreference(providerId)
    }

}