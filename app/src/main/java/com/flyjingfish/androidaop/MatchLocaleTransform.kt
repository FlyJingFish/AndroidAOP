package com.flyjingfish.androidaop

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType

@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.test_lib.LocaleTransform",
    methodName = ["getLanguage"],
    type = MatchType.SELF
)
class MatchLocaleTransform : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchLocaleTransform", "======" + methodName);
        return joinPoint.proceed()
    }
}