package com.ians.observer.domain.usecase.settings

import com.ians.observer.domain.usecase.fake.FakeDailyNewsScheduler
import com.ians.observer.domain.usecase.fake.FakeSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetNotificationEnabledUseCaseTest {

    @Test
    fun `enabling notifications saves preference and schedules work`() = runTest {
        val settingsRepository = FakeSettingsRepository(notificationsEnabled = false)
        val scheduler = FakeDailyNewsScheduler()
        val useCase = SetNotificationEnabledUseCase(settingsRepository, scheduler)

        useCase(true)

        assertTrue(settingsRepository.observeNotificationPreference().first())
        assertEquals(1, scheduler.scheduleCallCount)
        assertEquals(0, scheduler.cancelCallCount)
    }

    @Test
    fun `disabling notifications saves preference and cancels work`() = runTest {
        val settingsRepository = FakeSettingsRepository(notificationsEnabled = true)
        val scheduler = FakeDailyNewsScheduler()
        val useCase = SetNotificationEnabledUseCase(settingsRepository, scheduler)

        useCase(false)

        assertFalse(settingsRepository.observeNotificationPreference().first())
        assertEquals(0, scheduler.scheduleCallCount)
        assertEquals(1, scheduler.cancelCallCount)
    }
}
