package com.ians.observer.domain.model

enum class ProviderId(
    val value: String
) {
    NEWS_API("news-api"),
    NEWS_DATA("news-data");

    companion object {
        fun fromValue(value: String): ProviderId =
            ProviderId.entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown ProviderId code: $value")

    }
}
