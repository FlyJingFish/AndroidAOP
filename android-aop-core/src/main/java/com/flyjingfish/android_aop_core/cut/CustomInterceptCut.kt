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
        if (AndroidAop.getOnCustomInterceptListener() == null){
            return joinPoint.proceed()
        }
        return AndroidAop.getOnCustomInterceptListener()?.invoke(joinPoint, anno)
    }
}