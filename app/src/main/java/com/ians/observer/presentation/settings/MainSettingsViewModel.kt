package com.ians.observer.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ians.observer.domain.repository.SettingsRepository
import com.ians.observer.domain.usecase.settings.ClearArticleCacheUseCase
import com.ians.observer.domain.usecase.settings.SendTestNotificationUseCase
import com.ians.observer.domain.usecase.settings.SetNotificationEnabledUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainSettingsViewModel @Inject constructor(
    private val clearArticleCacheUseCase: ClearArticleCacheUseCase,
    private val sendTestNotificationUseCase: SendTestNotificationUseCase,
    private val setNotificationEnabledUseCase: SetNotificationEnabledUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    // Null represents the brief period before DataStore emits the persisted preference.
    val notificationsEnabledState = settingsRepository.observeNotificationPreference()
        .map<Boolean, Boolean?> { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val notificationPermissionRequestedState =
        settingsRepository.observeNotificationPermissionRequested()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    sealed interface SettingsEvent {
        data object CacheCleared : SettingsEvent
        data object CacheClearFailed : SettingsEvent
        data object TestNotificationScheduled : SettingsEvent
    }

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun setNotificationEnabled(enabled: Boolean) = viewModelScope.launch {
        setNotificationEnabledUseCase(enabled)
    }

    fun markNotificationPermissionRequested() = viewModelScope.launch {
        settingsRepository.setNotificationPermissionRequested(true)
    }

    fun sendTestNotification() {
        sendTestNotificationUseCase()
        _events.trySend(SettingsEvent.TestNotificationScheduled)
    }

    fun clearCachedArticles() = viewModelScope.launch {
        val event = if (clearArticleCacheUseCase()) {
            SettingsEvent.CacheCleared
        } else {
            SettingsEvent.CacheClearFailed
        }
        _events.send(event)
    }
}
