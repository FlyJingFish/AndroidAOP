package com.flyjingfish.androidaop

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType

@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.test_lib.BaseActivity",
    methodName = ["onResume","baseMultParam"],
    type = MatchType.EXTENDS,
    overrideMethod = true
)
class MatchActivity2 : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchActivity", "---->${joinPoint.targetClass}--${joinPoint.targetMethod.name}--${joinPoint.targetMethod.parameterTypes.toList()}");
        return joinPoint.proceed()
    }
}