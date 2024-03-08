package com.flyjingfish.androidaop

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType

@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.androidaop.SecondActivity",
    methodName = ["*"],
    type = MatchType.SELF
)
class MatchAllMethod : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchMainAllMethod", "AllMethod======" + methodName);
        return joinPoint.proceed()
    }
}