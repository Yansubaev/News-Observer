package com.ians.observer.data.remote.newsapi

import com.ians.observer.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NewsApiCategoryMapperTest {

    @Test
    fun `maps every supported category to News API code`() {
        val expectedCodes = mapOf(
            Category.GENERAL to "general",
            Category.BUSINESS to "business",
            Category.ENTERTAINMENT to "entertainment",
            Category.HEALTH to "health",
            Category.SCIENCE to "science",
            Category.SPORTS to "sports",
            Category.TECHNOLOGY to "technology",
        )

        expectedCodes.forEach { (category, expectedCode) ->
            assertEquals(expectedCode, category.toNewsApiCategory())
        }
    }

    @Test
    fun `does not map categories unsupported by News API`() {
        val unsupportedCategories = Category.entries - setOf(
            Category.GENERAL,
            Category.BUSINESS,
            Category.ENTERTAINMENT,
            Category.HEALTH,
            Category.SCIENCE,
            Category.SPORTS,
            Category.TECHNOLOGY,
        )

        unsupportedCategories.forEach { category ->
            assertNull(category.toNewsApiCategory())
        }
    }
}
