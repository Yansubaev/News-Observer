package com.ians.observer.domain.model

import androidx.annotation.StringRes
import com.ians.observer.R

enum class Category(
    val value: String,
    @get:StringRes val stringRes: Int
) {
    GENERAL(
        "general",
        R.string.cat_general
    ),
    BUSINESS(
        "business",
        R.string.cat_business
    ),
    ENTERTAINMENT(
        "entertainment",
        R.string.cat_entertainment
    ),
    HEALTH(
        "health",
        R.string.cat_health
    ),
    SCIENCE(
        "science",
        R.string.cat_science
    ),
    SPORTS(
        "sports",
        R.string.cat_sports
    ),
    TECHNOLOGY(
        "technology",
        R.string.cat_technology
    ),
    ALL(
        "all",
        R.string.cat_all
    );

    override fun toString(): String {
        return value
    }
}