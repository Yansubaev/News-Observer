package com.ians.observer.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingRepository {
    suspend fun getLastSyncTime(): Long
    suspend fun saveLastSyncTime(time: Long)
    suspend fun getSyncInterval(): Long
    suspend fun getCountryPreference(): String

    fun getSearchHistory(): Flow<List<String>>
    suspend fun saveSearchQuery(query: String)
    suspend fun deleteSearchQuery(query: String)
}