package com.ians.observer.data.remote.provider

import com.google.gson.JsonParseException
import com.ians.observer.domain.exception.NewsException
import com.ians.observer.domain.model.ProviderId
import retrofit2.HttpException
import java.io.IOException

private const val REQUEST_TIMEOUT_CODE = 408
private const val RATE_LIMIT_CODE = 429

inline fun <T> providerCall(providerId: ProviderId, block: () -> T): T =
    try {
        block()
    } catch (e: IOException) {
        throw NewsException.NoConnection(providerId, e)
    } catch (e: HttpException) {
        throw e.toNewsException(providerId)
    } catch (e: JsonParseException) {
        throw NewsException.InvalidResponse(providerId, e)
    }

fun HttpException.toNewsException(providerId: ProviderId): NewsException =
    when (val code = code()) {
        RATE_LIMIT_CODE -> NewsException.RateLimited(providerId)
        REQUEST_TIMEOUT_CODE, in 500..599 -> NewsException.ServerError(providerId, code)
        else -> NewsException.RequestRejected(providerId, code)
    }
