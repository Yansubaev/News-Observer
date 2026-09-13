package com.ians.observer.data.local

import javax.inject.Qualifier


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SettingsDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SearchHistoryDataStore