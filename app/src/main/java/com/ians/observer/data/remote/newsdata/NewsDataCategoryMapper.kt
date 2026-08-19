package com.ians.observer.data.remote.newsdata

import com.ians.observer.domain.model.Category

internal fun Category.toNewsDataCategory(): String? = when (this) {
    Category.GENERAL -> "top"
    Category.BREAKING -> "breaking"
    Category.BUSINESS -> "business"
    Category.CRIME -> "crime"
    Category.DOMESTIC -> "domestic"
    Category.EDUCATION -> "education"
    Category.ENTERTAINMENT -> "entertainment"
    Category.ENVIRONMENT -> "environment"
    Category.FOOD -> "food"
    Category.HEALTH -> "health"
    Category.LIFESTYLE -> "lifestyle"
    Category.OTHER -> "other"
    Category.POLITICS -> "politics"
    Category.SCIENCE -> "science"
    Category.SPORTS -> "sports"
    Category.TECHNOLOGY -> "technology"
    Category.TOURISM -> "tourism"
    Category.WORLD -> "world"
    Category.ALL -> null
}
