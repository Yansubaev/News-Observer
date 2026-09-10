package com.ians.observer.domain.repository

import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.ProviderId
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getSyncInterval(): Long

    suspend fun setCountryPreference(country: NewsCountry)
    fun observeCountryPreference(): Flow<NewsCountry>

    suspend fun setLanguagePreference(language: NewsLanguage)
    fun observeLanguagePreference(): Flow<NewsLanguage>

    suspend fun setFeedProviderPreference(providerId: ProviderId)
    fun observeFeedProviderPreference(): Flow<ProviderId>

    suspend fun setSearchProviderPreference(providerId: ProviderId)
    fun observeSearchProviderPreference(): Flow<ProviderId>

    suspend fun setSearchProviderEnabled(providerId: ProviderId, enabled: Boolean)
    fun observeEnabledSearchProviderIds(): Flow<Set<ProviderId>>

    suspend fun setNotificationPreference(enabled: Boolean)
    fun observeNotificationPreference(): Flow<Boolean>

    suspend fun setNotificationPermissionRequested(requested: Boolean)
    fun observeNotificationPermissionRequested(): Flow<Boolean>
}
