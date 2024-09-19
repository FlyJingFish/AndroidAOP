package com.flyjingfish.androidaop.test

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType

@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.androidaop.test.TestOverride",
    methodName = ["test"],
    type = MatchType.EXTENDS,
    overrideMethod = true
)
class MatchTestOverride : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchTestOverride", "---->${joinPoint}")
        return joinPoint.proceed()
    }
}