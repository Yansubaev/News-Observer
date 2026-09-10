package com.ians.observer.data.remote.gdelt

import com.ians.observer.domain.model.NewsLanguage

internal fun NewsLanguage.toGdeltLanguage(): String = when (this) {
    NewsLanguage.AR -> "ara"
    NewsLanguage.DE -> "deu"
    NewsLanguage.EN -> "eng"
    NewsLanguage.ES -> "spa"
    NewsLanguage.FR -> "fra"
    NewsLanguage.HE -> "heb"
    NewsLanguage.IT -> "ita"
    NewsLanguage.NL -> "nld"
    NewsLanguage.NO -> "nor"
    NewsLanguage.PT -> "por"
    NewsLanguage.RU -> "rus"
    NewsLanguage.SV -> "swe"
    NewsLanguage.UD -> "urd"
    NewsLanguage.ZH -> "zho"
}
