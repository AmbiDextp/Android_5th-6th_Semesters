package com.example.openapitest.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogTtlPolicyTest {

    private val policy = CatalogTtlPolicy()
    private val hour = CatalogTtlPolicy.MILLIS_PER_HOUR

    @Test
    fun `отсутствие кэша означает устарелость при включённом TTL`() {
        assertTrue(policy.isStale(lastCachedAt = null, ttlHours = 24))
    }

    @Test
    fun `отсутствие кэша не устарело при выключенном TTL`() {
        assertFalse(policy.isStale(lastCachedAt = null, ttlHours = null))
    }

    @Test
    fun `свежий кэш не устарел`() {
        assertFalse(policy.isStale(lastCachedAt = 0L, ttlHours = 24, now = hour))
    }

    @Test
    fun `кэш старше TTL устарел`() {
        assertTrue(policy.isStale(lastCachedAt = 0L, ttlHours = 1, now = 2 * hour))
    }

    @Test
    fun `граница ровно на TTL считается устаревшей`() {
        assertTrue(policy.isStale(lastCachedAt = 0L, ttlHours = 2, now = 2 * hour))
    }

    @Test
    fun `при выключенном TTL кэш никогда не устаревает`() {
        assertFalse(policy.isStale(lastCachedAt = 0L, ttlHours = null, now = 1000L * hour))
    }
}
