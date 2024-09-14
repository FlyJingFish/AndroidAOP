package com.flyjingfish.android_aop_core.annotations

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut
import com.flyjingfish.android_aop_core.cut.TryCatchCut

/**
 * 加入此注解可为您的方法包裹一层 try catch 代码
 */
@AndroidAopPointCut(TryCatchCut::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER
)
annotation class TryCatch(
    /**
     * flag 捕获异常的标志
     */
    val value: String = ""
)
