package com.ians.observer.domain.usecase.settings

import com.ians.observer.domain.background.DailyNewsScheduler
import javax.inject.Inject

class SendTestNotificationUseCase @Inject constructor(
    private val dailyNewsScheduler: DailyNewsScheduler,
) {
    operator fun invoke() {
        dailyNewsScheduler.enqueueOneTime()
    }
}
