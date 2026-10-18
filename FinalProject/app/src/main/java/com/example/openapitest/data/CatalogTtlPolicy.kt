package com.example.openapitest.data

class CatalogTtlPolicy(private val clock: () -> Long = System::currentTimeMillis) {

    fun isStale(lastCachedAt: Long?, ttlHours: Long?, now: Long = clock()): Boolean {
        if (ttlHours == null) return false
        if (lastCachedAt == null) return true
        return now - lastCachedAt >= ttlHours * MILLIS_PER_HOUR
    }

    companion object {
        const val MILLIS_PER_HOUR = 3_600_000L
    }
}
