package com.ians.observer.data.remote.provider

import com.google.gson.JsonParseException
import com.ians.observer.domain.exception.NewsException
import com.ians.observer.domain.exception.isTransient
import com.ians.observer.domain.model.ProviderId
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ProviderCallTest {

    private val providerId = ProviderId.NEWS_DATA

    private fun failWith(throwable: Throwable): NewsException =
        try {
            providerCall(providerId) { throw throwable }
        } catch (e: NewsException) {
            e
        }

    private fun http(code: Int) = HttpException(Response.error<Unit>(code, "".toResponseBody()))

    @Test
    fun `maps failures to domain exceptions`() {
        assertTrue(failWith(IOException()) is NewsException.NoConnection)
        assertTrue(failWith(http(429)) is NewsException.RateLimited)
        assertTrue(failWith(http(408)) is NewsException.ServerError)
        assertTrue(failWith(http(503)) is NewsException.ServerError)
        assertTrue(failWith(http(401)) is NewsException.RequestRejected)
        assertTrue(failWith(JsonParseException("bad")) is NewsException.InvalidResponse)
        assertEquals(providerId, failWith(IOException()).providerId)
    }

    @Test
    fun `only network, rate limit and server errors are transient`() {
        assertTrue(failWith(IOException()).isTransient)
        assertTrue(failWith(http(429)).isTransient)
        assertTrue(failWith(http(500)).isTransient)
        assertFalse(failWith(http(422)).isTransient)
        assertFalse(failWith(JsonParseException("bad")).isTransient)
    }

    @Test
    fun `returns block result on success`() {
        assertEquals(42, providerCall(providerId) { 42 })
    }
}
