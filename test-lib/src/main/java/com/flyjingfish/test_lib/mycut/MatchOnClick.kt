package com.flyjingfish.test_lib.mycut

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType

@AndroidAopMatchClassMethod(
    targetClassName = "android.view.View.OnClickListener",
    methodName = ["onClick"],
    type = MatchType.EXTENDS
)
class MatchOnClick : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchOnClick", "=====invoke=====$methodName")
        return joinPoint.proceed()
    }
}