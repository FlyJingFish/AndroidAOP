package com.flyjingfish.androidaop

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType
import com.flyjingfish.test_lib.ToastUtils

@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.androidaop.test.TestBean",
    methodName = ["setName"],
    type = MatchType.SELF
)
class MatchTestBean : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchTestBean", "======$methodName");
        ToastUtils.makeText(ToastUtils.app,"MatchTestBean======$methodName")
        return joinPoint.proceed()
    }
}