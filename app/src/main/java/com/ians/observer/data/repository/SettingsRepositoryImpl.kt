package com.ians.observer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ians.observer.data.remote.provider.NewsProviderRegistry
import com.ians.observer.di.SettingsDataStore
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:SettingsDataStore private val dataStore: DataStore<Preferences>,
    private val newsProviderRegistry: NewsProviderRegistry,
) : SettingsRepository {


    private object PreferencesKeys {
        val COUNTRY = stringPreferencesKey("country")
        val LANGUAGE = stringPreferencesKey("language")
        val FEED_PROVIDER = stringPreferencesKey("feed_provider")
        val SEARCH_PROVIDER = stringPreferencesKey("search_provider")
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
                val value = prefs[PreferencesKeys.FEED_PROVIDER] ?: ProviderId.NEWS_DATA.value
                val feedProvider = ProviderId.fromValue(value)
                feedProvider.takeIf { it in newsProviderRegistry.availableIds }
                    ?: ProviderId.NEWS_DATA
            }
            .distinctUntilChanged()

    override suspend fun setSearchProviderPreference(providerId: ProviderId) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SEARCH_PROVIDER] = providerId.value
        }
    }

    override fun observeSearchProviderPreference(): Flow<ProviderId> =
        dataStore.data
            .map { prefs ->
                val value = prefs[PreferencesKeys.SEARCH_PROVIDER] ?: ProviderId.NEWS_DATA.value
                val searchProvider = ProviderId.fromValue(value)
                searchProvider.takeIf { it in newsProviderRegistry.availableIds }
                    ?: ProviderId.NEWS_DATA
            }
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
}