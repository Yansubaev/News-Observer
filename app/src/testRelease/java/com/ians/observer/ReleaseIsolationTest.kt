package com.ians.observer

import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * News API's free plan forbids production use, and third-party images are not licensed.
 * Runs only in `testReleaseUnitTest`, against the release variant's classpath.
 */
class ReleaseIsolationTest {

    @Test(expected = ClassNotFoundException::class)
    fun `news api provider is not packaged in release`() {
        Class.forName("com.ians.observer.data.remote.newsapi.NewsApiProvider")
    }

    @Test
    fun `news api key is not defined in release build config`() {
        val fieldNames = BuildConfig::class.java.fields.map { it.name }

        assertFalse("NEWS_API_KEY" in fieldNames)
    }

    @Test
    fun `images are disabled in release`() {
        assertFalse(BuildConfig.IMAGES_ENABLED)
    }
}
