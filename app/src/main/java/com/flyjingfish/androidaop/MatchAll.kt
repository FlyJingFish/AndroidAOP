package com.flyjingfish.androidaop

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType

@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.androidaop.*",
    methodName = ["*"],
    type = MatchType.SELF
)
class MatchAll : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchAll", "methodName======${joinPoint.targetClass}--${joinPoint.targetMethod.name}--${joinPoint.targetMethod.parameterTypes.toList()}");
        return joinPoint.proceed()
    }
}