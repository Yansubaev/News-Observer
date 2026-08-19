package com.ians.observer.domain.repository

import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import kotlinx.coroutines.flow.Flow

interface SettingRepository {
    suspend fun getSyncInterval(): Long
    suspend fun setCountryPreference(countryCode: String)
    suspend fun setLanguagePreference(languageCode: String)

    fun getSearchHistory(): Flow<List<String>>
    suspend fun saveSearchQuery(query: String)
    suspend fun deleteSearchQuery(query: String)

    fun observeCountryPreference(): Flow<NewsCountry>
    fun observeLanguagePreference(): Flow<NewsLanguage>
}