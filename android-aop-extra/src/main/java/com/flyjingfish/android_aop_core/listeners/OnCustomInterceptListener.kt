package com.flyjingfish.android_aop_core.listeners

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_core.annotations.CustomIntercept

fun interface OnCustomInterceptListener {
    fun invoke(
        joinPoint: ProceedJoinPoint,
        customIntercept: CustomIntercept
    ): Any?

}