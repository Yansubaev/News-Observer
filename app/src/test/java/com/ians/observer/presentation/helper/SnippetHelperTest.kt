package com.ians.observer.presentation.helper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SnippetHelperTest {

    @Test
    fun `keeps only the first sentence`() {
        val text = "The central bank raised interest rates again today. Analysts expect more hikes."

        assertEquals("The central bank raised interest rates again today.", shortenSnippet(text))
    }

    @Test
    fun `does not treat abbreviations as sentence end`() {
        val text = "U.S. officials confirmed the talks will continue next week. More details later."

        assertEquals("U.S. officials confirmed the talks will continue next week.", shortenSnippet(text))
    }

    @Test
    fun `short text without sentence end is unchanged`() {
        assertEquals("Markets rally", shortenSnippet("  Markets rally  "))
    }

    @Test
    fun `long text is cut at word boundary with ellipsis`() {
        val text = "word ".repeat(100)

        val result = shortenSnippet(text, maxLength = 22)

        assertEquals("word word word word…", result)
    }

    @Test
    fun `too long first sentence is cut`() {
        val text = "a".repeat(30) + " " + "b".repeat(300) + ". Second."

        val result = shortenSnippet(text)

        assertTrue(result.length <= 201)
        assertTrue(result.endsWith("…"))
    }
}
