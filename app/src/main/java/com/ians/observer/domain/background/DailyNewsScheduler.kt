package com.ians.observer.domain.background

interface DailyNewsScheduler {
    fun schedule()
    fun cancel()
    fun enqueueOneTime()
}
