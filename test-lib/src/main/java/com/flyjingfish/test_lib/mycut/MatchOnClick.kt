package com.flyjingfish.test_lib.mycut

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod

@AndroidAopMatchClassMethod(
    targetClassName = "android.view.View.OnClickListener",
    methodName = ["onClick"]
)
class MatchOnClick : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchOnClick", "=====invoke=====$methodName")
        return joinPoint.proceed()
    }
}