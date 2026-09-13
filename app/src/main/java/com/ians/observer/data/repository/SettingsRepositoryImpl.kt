package com.ians.observer.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.ians.observer.data.local.SettingsDataStore
import com.ians.observer.data.remote.provider.NewsProviderRegistry
import com.ians.observer.data.remote.provider.model.ProviderCapabilities
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class SettingsRepositoryImpl @Inject constructor(
    @param:SettingsDataStore private val dataStore: DataStore<Preferences>,
    private val newsProviderRegistry: NewsProviderRegistry,
) : SettingsRepository {


    private object PreferencesKeys {
        val COUNTRY = stringPreferencesKey("country")
        val LANGUAGE = stringPreferencesKey("language")
        val FEED_PROVIDER = stringPreferencesKey("feed_provider")
        val SEARCH_PROVIDER = stringPreferencesKey("search_provider")
        val ENABLED_SEARCH_PROVIDERS = stringSetPreferencesKey("enabled_search_providers")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val NOTIFICATIONS_PERMISSION = booleanPreferencesKey("notif_permission")
    }

    override suspend fun getSyncInterval(): Long {
        return 10 * 60 * 1000
    }

    override suspend fun setCountryPreference(country: NewsCountry) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.COUNTRY] = country.code
        }
    }

    override fun observeCountryPreference(): Flow<NewsCountry> =
        dataStore.data
            .map { prefs ->
                val code = prefs[PreferencesKeys.COUNTRY] ?: "us"
                NewsCountry.fromCode(code)
            }
            .distinctUntilChanged()

    override suspend fun setLanguagePreference(language: NewsLanguage) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LANGUAGE] = language.code
        }
    }

    override fun observeLanguagePreference(): Flow<NewsLanguage> =
        dataStore.data
            .map { prefs ->
                val code = prefs[PreferencesKeys.LANGUAGE] ?: "en"
                NewsLanguage.fromCode(code)
            }
            .distinctUntilChanged()

    override suspend fun setFeedProviderPreference(providerId: ProviderId) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.FEED_PROVIDER] = providerId.value
        }
    }

    override fun observeFeedProviderPreference(): Flow<ProviderId> =
        dataStore.data
            .map { prefs ->
                prefs[PreferencesKeys.FEED_PROVIDER]
                    ?.let(::providerIdOrNull)
                    ?.takeIf { it in newsProviderRegistry.availableIds }
                    ?: ProviderId.NEWS_DATA
            }
            .distinctUntilChanged()

    override suspend fun setSearchProviderPreference(providerId: ProviderId) {
        dataStore.edit { prefs ->
            if (providerId in enabledSearchProviderIds(prefs)) {
                prefs[PreferencesKeys.SEARCH_PROVIDER] = providerId.value
            }
        }
    }

    override fun observeSearchProviderPreference(): Flow<ProviderId> =
        dataStore.data
            .map { prefs ->
                val enabledProviderIds = enabledSearchProviderIds(prefs)
                val selectedProvider = prefs[PreferencesKeys.SEARCH_PROVIDER]
                    ?.let(::providerIdOrNull)

                selectedProvider?.takeIf { it in enabledProviderIds }
                    ?: enabledProviderIds.first()
            }
            .distinctUntilChanged()

    override suspend fun setSearchProviderEnabled(providerId: ProviderId, enabled: Boolean) {
        dataStore.edit { prefs ->
            val enabledProviderIds = enabledSearchProviderIds(prefs).toMutableSet()

            if (enabled) {
                enabledProviderIds += providerId
            } else if (enabledProviderIds.size > 1) {
                enabledProviderIds -= providerId
            }

            prefs[PreferencesKeys.ENABLED_SEARCH_PROVIDERS] =
                enabledProviderIds.mapTo(linkedSetOf()) { it.value }

            val selectedProvider = prefs[PreferencesKeys.SEARCH_PROVIDER]
                ?.let(::providerIdOrNull)
            if (selectedProvider !in enabledProviderIds) {
                prefs[PreferencesKeys.SEARCH_PROVIDER] = enabledProviderIds.first().value
            }
        }
    }

    override fun observeEnabledSearchProviderIds(): Flow<Set<ProviderId>> =
        dataStore.data
            .map(::enabledSearchProviderIds)
            .distinctUntilChanged()

    override suspend fun setNotificationPreference(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.NOTIFICATIONS] = enabled
        }
    }

    override fun observeNotificationPreference(): Flow<Boolean> =
        dataStore.data
            .map { prefs ->
                prefs[PreferencesKeys.NOTIFICATIONS] ?: false
            }
            .distinctUntilChanged()

    override suspend fun setNotificationPermissionRequested(requested: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.NOTIFICATIONS_PERMISSION] = requested
        }
    }

    override fun observeNotificationPermissionRequested(): Flow<Boolean> =
        dataStore.data
            .map { prefs ->
                prefs[PreferencesKeys.NOTIFICATIONS_PERMISSION] ?: false
            }
            .distinctUntilChanged()

    private fun enabledSearchProviderIds(prefs: Preferences): Set<ProviderId> {
        val supportedProviderIds = supportedSearchProviderIds
        val storedProviderIds = prefs[PreferencesKeys.ENABLED_SEARCH_PROVIDERS]
            ?.mapNotNull(::providerIdOrNull)
            ?.filter { it in supportedProviderIds }
            ?.toSet()

        return storedProviderIds?.takeIf { it.isNotEmpty() } ?: supportedProviderIds
    }

    private val supportedSearchProviderIds: Set<ProviderId>
        get() = ProviderId.entries.filterTo(linkedSetOf()) { providerId ->
            providerId in newsProviderRegistry.availableIds &&
                ProviderCapabilities.SEARCH in newsProviderRegistry.require(providerId).capabilities
        }

    private fun providerIdOrNull(value: String): ProviderId? =
        ProviderId.entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
}
