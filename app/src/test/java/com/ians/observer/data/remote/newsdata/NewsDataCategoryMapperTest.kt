package com.ians.observer.data.remote.newsdata

import com.ians.observer.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NewsDataCategoryMapperTest {

    @Test
    fun `maps every supported category to NewsData code`() {
        val expectedCodes = mapOf(
            Category.GENERAL to "top",
            Category.BREAKING to "breaking",
            Category.BUSINESS to "business",
            Category.CRIME to "crime",
            Category.DOMESTIC to "domestic",
            Category.EDUCATION to "education",
            Category.ENTERTAINMENT to "entertainment",
            Category.ENVIRONMENT to "environment",
            Category.FOOD to "food",
            Category.HEALTH to "health",
            Category.LIFESTYLE to "lifestyle",
            Category.OTHER to "other",
            Category.POLITICS to "politics",
            Category.SCIENCE to "science",
            Category.SPORTS to "sports",
            Category.TECHNOLOGY to "technology",
            Category.TOURISM to "tourism",
            Category.WORLD to "world",
        )

        expectedCodes.forEach { (category, expectedCode) ->
            assertEquals(expectedCode, category.toNewsDataCategory())
        }
    }

    @Test
    fun `does not map synthetic all category`() {
        assertNull(Category.ALL.toNewsDataCategory())
    }
}
