package com.ians.observer.domain.usecase.settings

import com.ians.observer.domain.background.DailyNewsScheduler
import com.ians.observer.domain.repository.SettingsRepository
import javax.inject.Inject

class SetNotificationEnabledUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dailyNewsScheduler: DailyNewsScheduler,
) {
    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.setNotificationPreference(enabled)

        if (enabled) {
            dailyNewsScheduler.schedule()
        } else {
            dailyNewsScheduler.cancel()
        }
    }
}