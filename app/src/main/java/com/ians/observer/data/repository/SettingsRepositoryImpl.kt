package com.ians.observer.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ians.observer.domain.repository.SettingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "settings_prefs")

class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SettingRepository {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        val COUNTRY = stringPreferencesKey("country")
    }

    override suspend fun getLastSyncTime(): Long {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.LAST_SYNC_TIME] ?: 0L
        }.first()
    }

    override suspend fun saveLastSyncTime(time: Long) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LAST_SYNC_TIME] = time
        }
    }

    override suspend fun getSyncInterval(): Long {
        return 10 * 60 * 1000
    }

    override suspend fun getCountryPreference(): String {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.COUNTRY] ?: "us"
        }.first()
    }
}