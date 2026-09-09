package com.ians.observer.domain.usecase.settings

import com.ians.observer.domain.usecase.fake.FakeDailyNewsScheduler
import org.junit.Assert.assertEquals
import org.junit.Test

class SendTestNotificationUseCaseTest {

    @Test
    fun `enqueues one time daily news work`() {
        val scheduler = FakeDailyNewsScheduler()
        val useCase = SendTestNotificationUseCase(scheduler)

        useCase()

        assertEquals(1, scheduler.enqueueOneTimeCallCount)
        assertEquals(0, scheduler.scheduleCallCount)
        assertEquals(0, scheduler.cancelCallCount)
    }
}
