package com.ians.observer.domain.model

enum class Category(
    val value: String
) {
    GENERAL("general"),
    BREAKING("breaking"),
    BUSINESS("business"),
    CRIME("crime"),
    DOMESTIC("domestic"),
    EDUCATION("education"),
    ENTERTAINMENT("entertainment"),
    ENVIRONMENT("environment"),
    FOOD("food"),
    HEALTH("health"),
    LIFESTYLE("lifestyle"),
    OTHER("other"),
    POLITICS("politics"),
    SCIENCE("science"),
    SPORTS("sports"),
    TECHNOLOGY("technology"),
    TOURISM("tourism"),
    WORLD("world"),
    ALL("all");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromValue(value: String): Category =
            Category.entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: GENERAL

    }
}
