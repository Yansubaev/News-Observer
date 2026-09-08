package com.ians.observer.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.repository.NewsProvidersRepository
import com.ians.observer.domain.repository.SettingsRepository
import com.ians.observer.domain.usecase.settings.ClearArticleCacheUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val clearArticleCacheUseCase: ClearArticleCacheUseCase,

    private val settingsRepository: SettingsRepository,
    private val newsProvidersRepository: NewsProvidersRepository,
    ) : ViewModel() {

    val feedProviderIds = newsProvidersRepository.getTopHeadlinesCapableProviderIds()
    val searchProviderIds = newsProvidersRepository.getSearchCapableProviderIds()

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

    sealed interface SettingsEvent {
        data object CacheCleared : SettingsEvent
        data object CacheClearFailed : SettingsEvent
    }

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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

    fun clearCachedArticles() = viewModelScope.launch {
        clearArticleCacheUseCase(
            onSuccess = {
                _events.send(SettingsEvent.CacheCleared)
            },
            onFailed = {
                _events.send(SettingsEvent.CacheClearFailed)
            }
        )
    }
}