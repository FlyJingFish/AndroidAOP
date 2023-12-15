package com.flyjingfish.android_aop_core.annotations


import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut
import com.flyjingfish.android_aop_core.cut.DelayCut
import com.flyjingfish.android_aop_core.utils.AndroidAop


@AndroidAopPointCut(DelayCut::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER
)
annotation class Delay(
    /**
     *
     * 任务 key ,可通过调用 [AndroidAop.shutdown]
     * 或 [AndroidAop.shutdownNow]停止任务,注意不要和其他 id 重合，包括 [Scheduled.id]
     */
    val id: String = "",
    /**
     *
     * 延迟多久开始执行方法，单位是毫秒
     */
    val delay: Long,
    /**
     *
     * 是否在主线程执行，默认主线程
     */
    val isOnMainThread: Boolean = true
)