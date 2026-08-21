package com.ians.observer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ians.observer.di.SettingsDataStore
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
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
        val SEARCH_HISTORY = stringPreferencesKey("search_history")
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
}