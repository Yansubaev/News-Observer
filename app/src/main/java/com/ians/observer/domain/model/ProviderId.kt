package com.ians.observer.domain.model

import androidx.annotation.StringRes
import com.ians.observer.R

enum class ProviderId(
    val value: String,
    @get:StringRes val nameRes: Int,
) {
    NEWS_API(
        "news-api",
        R.string.provider_news_api
    ),

    NEWS_DATA(
        "news-data",
        R.string.provider_news_data
    );

    companion object {
        fun fromValue(value: String): ProviderId =
            ProviderId.entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown ProviderId code: $value")

    }
}