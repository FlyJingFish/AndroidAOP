package com.flyjingfish.android_aop_annotation.utils

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal class AutoCleaningObjectPool<T>(
    private val creator: () -> T,
    private val maxCacheSize: Int = Int.MAX_VALUE,
    private val maxIdleTimeMillis: Long = 30_000,  // 30 秒空闲时间
    private val cleanIntervalMillis: Long = 10_000 // 每 10 秒检查一次
) {
    private data class Wrapper<T>(val obj: T, var lastUsed: Long)

    private val pool = ConcurrentLinkedQueue<Wrapper<T>>()
    private val cachedCount = AtomicInteger(0)

    init {
        Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "ObjectPool-Cleaner").apply { isDaemon = true }
        }.scheduleAtFixedRate({
            val now = System.currentTimeMillis()
            val it = pool.iterator()
            while (it.hasNext()) {
                val wrapper = it.next()
                if (now - wrapper.lastUsed > maxIdleTimeMillis) {
                    it.remove()
                    cachedCount.decrementAndGet()
                }
            }
        }, cleanIntervalMillis, cleanIntervalMillis, TimeUnit.MILLISECONDS)
    }

    fun borrow(): T {
        val wrapper = pool.poll()
        return if (wrapper != null) {
            cachedCount.decrementAndGet()
            wrapper.obj
        } else {
            creator()
        }
    }

    fun release(obj: T) {
        if (cachedCount.get() < maxCacheSize) {
            pool.offer(Wrapper(obj, System.currentTimeMillis()))
            cachedCount.incrementAndGet()
        }
        // 否则不缓存，直接丢弃
    }

    fun availableCount(): Int = cachedCount.get()
}
