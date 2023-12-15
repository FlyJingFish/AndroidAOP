package com.flyjingfish.android_aop_core.annotations

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut
import com.flyjingfish.android_aop_core.cut.DoubleClickCut

/**
 * 双击注解，加入此注解，可使你的方法双击时才可进入
 */
@AndroidAopPointCut(DoubleClickCut::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER
)
annotation class DoubleClick(
    /**
     * 两次点击的最大用时（ms），默认是300ms
     */
    val value: Long = DEFAULT_INTERVAL_MILLIS
) {
    companion object {
        const val DEFAULT_INTERVAL_MILLIS: Long = 300
    }
}