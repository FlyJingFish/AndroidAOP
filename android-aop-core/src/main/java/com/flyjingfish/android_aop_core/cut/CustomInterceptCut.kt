package com.flyjingfish.android_aop_core.cut

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.base.BasePointCut
import com.flyjingfish.android_aop_core.annotations.CustomIntercept
import com.flyjingfish.android_aop_core.utils.AndroidAop

internal class CustomInterceptCut : BasePointCut<CustomIntercept> {
    override fun invoke(
        joinPoint: ProceedJoinPoint,
        anno: CustomIntercept
    ): Any? {
        return when (AndroidAop.getOnCustomInterceptListener()) {
            null -> {
                joinPoint.proceed()
            }
            else -> {
                AndroidAop.getOnCustomInterceptListener()?.invoke(joinPoint, anno)
            }
        }
    }
}