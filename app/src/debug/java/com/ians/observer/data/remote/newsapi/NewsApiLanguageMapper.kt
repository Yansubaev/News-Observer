package com.ians.observer.data.remote.newsapi

import com.ians.observer.domain.model.NewsLanguage

internal fun NewsLanguage.toNewsApiLanguage(): String = when (this) {
    NewsLanguage.AR -> "ar"
    NewsLanguage.DE -> "de"
    NewsLanguage.EN -> "en"
    NewsLanguage.ES -> "es"
    NewsLanguage.FR -> "fr"
    NewsLanguage.HE -> "he"
    NewsLanguage.IT -> "it"
    NewsLanguage.NL -> "nl"
    NewsLanguage.NO -> "no"
    NewsLanguage.PT -> "pt"
    NewsLanguage.RU -> "ru"
    NewsLanguage.SV -> "sv"
    NewsLanguage.UD -> "ud"
    NewsLanguage.ZH -> "zh"
}
