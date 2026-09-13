package com.ians.observer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ians.observer.data.local.SearchHistoryDataStore
import com.ians.observer.domain.repository.SearchHistoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchHistoryRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:SearchHistoryDataStore private val dataStore: DataStore<Preferences>
) : SearchHistoryRepository {

    private object PreferencesKeys {
        val SEARCH_HISTORY = stringPreferencesKey("search_history")
    }

    override fun getSearchHistory(): Flow<List<String>> {
        return dataStore.data.map { prefs ->
            val obj = prefs[PreferencesKeys.SEARCH_HISTORY] ?: ""
            val type = object : TypeToken<List<String>>() {}.type
            val list = Gson().fromJson<List<String>>(obj, type) ?: listOf()
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