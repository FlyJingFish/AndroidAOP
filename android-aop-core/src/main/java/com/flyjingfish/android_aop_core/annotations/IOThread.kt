package com.flyjingfish.android_aop_core.annotations

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut
import com.flyjingfish.android_aop_core.cut.IOThreadCut
import com.flyjingfish.android_aop_core.enums.ThreadType

/**
 * 切换到子线程的操作，加入此注解可使你的方法内的代码切换到子线程执行
 */
@AndroidAopPointCut(IOThreadCut::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER
)
annotation class IOThread(
    /**
     * 子线程的类型，默认是多线程池
     */
    val value: ThreadType = ThreadType.MultipleIO
)