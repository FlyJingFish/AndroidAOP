package com.flyjingfish.android_aop_core.annotations

import androidx.lifecycle.Lifecycle
import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut
import com.flyjingfish.android_aop_core.cut.OnLifecycleCut

/**
 * 监听生命周期的操作，加入此注解可使你的方法内的代码在对应生命周期内才去执行
 */
@AndroidAopPointCut(OnLifecycleCut::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER
)
annotation class OnLifecycle(val value: Lifecycle.Event)
