package com.ians.observer.domain.repository

interface SettingRepository {
    suspend fun getLastSyncTime(): Long
    suspend fun saveLastSyncTime(time: Long)
    suspend fun getSyncInterval(): Long
    suspend fun getCountryPreference(): String
}