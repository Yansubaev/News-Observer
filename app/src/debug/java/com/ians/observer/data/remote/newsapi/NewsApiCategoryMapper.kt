package com.ians.observer.data.remote.newsapi

import com.ians.observer.domain.model.Category

internal fun Category.toNewsApiCategory(): String? = when (this) {
    Category.GENERAL -> "general"
    Category.BUSINESS -> "business"
    Category.ENTERTAINMENT -> "entertainment"
    Category.HEALTH -> "health"
    Category.SCIENCE -> "science"
    Category.SPORTS -> "sports"
    Category.TECHNOLOGY -> "technology"
    Category.ALL,
    Category.BREAKING,
    Category.CRIME,
    Category.DOMESTIC,
    Category.EDUCATION,
    Category.ENVIRONMENT,
    Category.FOOD,
    Category.LIFESTYLE,
    Category.OTHER,
    Category.POLITICS,
    Category.TOURISM,
    Category.WORLD -> null
}
