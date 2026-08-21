package com.ians.observer.domain.repository

import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import kotlinx.coroutines.flow.Flow

interface SettingRepository {
    suspend fun getSyncInterval(): Long
    suspend fun setCountryPreference(country: NewsCountry)
    suspend fun setLanguagePreference(language: NewsLanguage)


    fun observeCountryPreference(): Flow<NewsCountry>
    fun observeLanguagePreference(): Flow<NewsLanguage>
}