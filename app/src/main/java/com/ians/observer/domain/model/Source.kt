package com.ians.observer.domain.model

import androidx.annotation.StringRes
import com.ians.observer.R

enum class Source(
    val value: String,
    @get:StringRes val nameRes: Int,
) {
    NEWS_API(
        "newsapi",
        R.string.source_newsapi
    )
}