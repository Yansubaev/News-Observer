package com.ians.observer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ians.observer.di.SettingsDataStore
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.repository.SettingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:SettingsDataStore private val dataStore: DataStore<Preferences>
) : SettingRepository {


    private object PreferencesKeys {
        val COUNTRY = stringPreferencesKey("country")
        val LANGUAGE = stringPreferencesKey("language")
        val FEED_PROVIDER = stringPreferencesKey("feed_provider")
        val SEARCH_PROVIDER = stringPreferencesKey("search_provider")
    }

    override suspend fun getSyncInterval(): Long {
        return 10 * 60 * 1000
    }

    override suspend fun setCountryPreference(country: NewsCountry) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.COUNTRY] = country.code
        }
    }

    override suspend fun setLanguagePreference(language: NewsLanguage) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LANGUAGE] = language.code
        }
    }

    override fun observeCountryPreference(): Flow<NewsCountry> =
        dataStore.data
            .map { prefs ->
                val code = prefs[PreferencesKeys.COUNTRY] ?: "us"
                NewsCountry.fromCode(code)
            }
            .distinctUntilChanged()

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

    override suspend fun setSearchProviderPreference(providerId: ProviderId) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SEARCH_PROVIDER] = providerId.value
        }
    }

    override fun observeFeedProviderPreference(): Flow<ProviderId> =
        dataStore.data
            .map { prefs ->
                val value = prefs[PreferencesKeys.FEED_PROVIDER] ?: ProviderId.NEWS_DATA.value
                ProviderId.fromValue(value)
            }
            .distinctUntilChanged()

    override fun observeSearchProviderPreference(): Flow<ProviderId> =
        dataStore.data
            .map { prefs ->
                val value = prefs[PreferencesKeys.SEARCH_PROVIDER] ?: ProviderId.NEWS_DATA.value
                ProviderId.fromValue(value)
            }
            .distinctUntilChanged()


}