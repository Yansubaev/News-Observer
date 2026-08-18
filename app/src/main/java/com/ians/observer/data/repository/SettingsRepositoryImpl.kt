package com.ians.observer.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ians.observer.domain.repository.SettingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "settings_prefs")

class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SettingRepository {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val COUNTRY = stringPreferencesKey("country")
        val LANGUAGE = stringPreferencesKey("language")
        val SEARCH_HISTORY = stringPreferencesKey("search_history")
    }

    override suspend fun getSyncInterval(): Long {
        return 10 * 60 * 1000
    }

    override suspend fun getCountryPreference(): String {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.COUNTRY] ?: "us"
        }.first()
    }

    override suspend fun setCountryPreference(countryCode: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.COUNTRY] = countryCode
        }
    }

    override suspend fun getLanguagePreference(): String {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.LANGUAGE] ?: "en"
        }.first()
    }

    override suspend fun setLanguagePreference(languageCode: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LANGUAGE] = languageCode
        }
    }

    override fun getSearchHistory(): Flow<List<String>> {
        return dataStore.data.map { prefs ->
            val obj = prefs[PreferencesKeys.SEARCH_HISTORY] ?: ""
            val type = object : TypeToken<List<String>>() {}.type
            val list = Gson().fromJson<List<String>>(obj, type) ?: listOf()

            Log.i("HHH", "History: $obj")

            list
        }
    }

    override suspend fun saveSearchQuery(query: String) {
        dataStore.edit { prefs ->
            val obj = prefs[PreferencesKeys.SEARCH_HISTORY] ?: ""
            val type = object : TypeToken<List<String>>() {}.type
            val history =
                Gson().fromJson<List<String>>(obj, type)?.toMutableList() ?: mutableListOf()

            if (history.contains(query)) {
                history.remove(query)
            }

            history.add(query)

            prefs[PreferencesKeys.SEARCH_HISTORY] = Gson().toJson(history.takeLast(10))
        }
    }

    override suspend fun deleteSearchQuery(query: String) {
        dataStore.edit { prefs ->
            val obj = prefs[PreferencesKeys.SEARCH_HISTORY] ?: ""
            val type = object : TypeToken<List<String>>() {}.type
            val history =
                Gson().fromJson<List<String>>(obj, type)?.toMutableList() ?: mutableListOf()

            if (history.remove(query)) {
                prefs[PreferencesKeys.SEARCH_HISTORY] = Gson().toJson(history.takeLast(10))
            }
        }
    }
}