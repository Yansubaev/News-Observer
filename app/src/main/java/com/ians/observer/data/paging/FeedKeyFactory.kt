package com.ians.observer.data.paging

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import com.ians.observer.data.paging.model.PagingSourceSpec


object FeedKeyFactory {
    fun create(spec: PagingSourceSpec): String =
        when (spec) {
            is PagingSourceSpec.Feed ->
                "feed:v1|country=${spec.country.code}" +
                        "|language=${spec.language.code}" +
                        "|category=${spec.category?.value ?: "all"}"

            is PagingSourceSpec.Search ->
                "search:v1|query=${spec.query.trim().toLowerCase(Locale.current)}" +
                        "|country=${spec.country?.code ?: "any"}" +
                        "|language=${spec.language?.code ?: "all"}"
        }
}