package com.ians.observer.domain.exception

import com.ians.observer.domain.model.ProviderId

sealed class NewsException(
    val providerId: ProviderId,
    cause: Throwable? = null,
) : Exception(cause) {

    class NoConnection(providerId: ProviderId, cause: Throwable) : NewsException(providerId, cause)

    class RateLimited(providerId: ProviderId) : NewsException(providerId)

    class ServerError(providerId: ProviderId, val httpCode: Int) : NewsException(providerId)

    class RequestRejected(providerId: ProviderId, val httpCode: Int) : NewsException(providerId)

    class InvalidResponse(providerId: ProviderId, cause: Throwable? = null) : NewsException(providerId, cause)
}

val NewsException.isTransient: Boolean
    get() = this is NewsException.NoConnection ||
            this is NewsException.RateLimited ||
            this is NewsException.ServerError
