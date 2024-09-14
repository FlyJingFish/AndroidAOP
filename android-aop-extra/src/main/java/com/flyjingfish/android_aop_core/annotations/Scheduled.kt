package com.flyjingfish.android_aop_core.annotations

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut
import com.flyjingfish.android_aop_core.cut.ScheduledCut
import com.flyjingfish.android_aop_core.utils.AndroidAop

/**
 * 定时任务
 */
@AndroidAopPointCut(ScheduledCut::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER
)
annotation class Scheduled(
    /**
     *
     * 任务 key ,可通过调用 [AndroidAop.shutdown]
     * 或 [AndroidAop.shutdownNow]停止任务,注意不要和其他 id 重合，包括 [Delay.id]
     */
    val id: String = "",
    /**
     * 延迟多久开始,单位毫秒
     */
    val initialDelay: Long = 0L,
    /**
     *
     * 时间间隔,单位毫秒
     */
    val interval: Long,
    /**
     * 执行次数,默认 [Scheduled.INFINITE]
     */
    val repeatCount: Int = INFINITE,
    /**
     *
     * 是否在主线程执行，默认主线程
     */
    val isOnMainThread: Boolean = true
) {
    companion object {
        /**
         * [Scheduled.repeatCount] 不限次数
         */
        const val INFINITE = -1
    }
}
