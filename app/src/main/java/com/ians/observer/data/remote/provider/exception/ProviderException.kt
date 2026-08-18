package com.ians.observer.data.remote.provider.exception

import com.ians.observer.domain.model.ProviderId

class ProviderException(
    val providerId: ProviderId,
    message: String
) : Exception(message)