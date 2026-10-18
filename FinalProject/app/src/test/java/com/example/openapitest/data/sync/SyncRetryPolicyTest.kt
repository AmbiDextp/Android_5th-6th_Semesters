package com.example.openapitest.data.sync

import com.example.openapitest.data.RefreshResult
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncRetryPolicyTest {

    private val error = RefreshResult.Error(offline = true)

    @Test
    fun `успешная синхронизация не повторяется`() {
        assertFalse(shouldRetry(RefreshResult.Success, runAttemptCount = 0))
    }

    @Test
    fun `ошибка повторяется пока есть попытки`() {
        assertTrue(shouldRetry(error, runAttemptCount = 0))
        assertTrue(shouldRetry(error, runAttemptCount = 1))
    }

    @Test
    fun `после последней попытки воркер завершается неудачей`() {
        assertFalse(shouldRetry(error, runAttemptCount = 2, maxAttempts = 3))
    }
}
