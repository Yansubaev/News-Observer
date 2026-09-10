package com.ians.observer.data.remote.gdelt

import android.os.SystemClock
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GdeltThrottleInterceptor @Inject constructor() : Interceptor {

    private val lock = Any()

    private var lastRequestAt = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        synchronized(lock) {
            val elapsed = SystemClock.elapsedRealtime() - lastRequestAt
            if (lastRequestAt != 0L && elapsed < MIN_REQUEST_INTERVAL_MILLIS) {
                Thread.sleep(MIN_REQUEST_INTERVAL_MILLIS - elapsed)
            }

            lastRequestAt = SystemClock.elapsedRealtime()
        }

        return chain.proceed(chain.request())
    }

    private companion object {
        const val MIN_REQUEST_INTERVAL_MILLIS = 5_000L
    }
}
