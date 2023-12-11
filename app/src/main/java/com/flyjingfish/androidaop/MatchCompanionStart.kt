package com.flyjingfish.androidaop

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType
import com.flyjingfish.test_lib.ToastUtils

@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.androidaop.ThirdActivity.Companion",
    methodName = ["start"],
    type = MatchType.SELF
)
class MatchCompanionStart : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchStart", "======$methodName")
//        ToastUtils.makeText(ToastUtils.app,"======$methodName")
        return joinPoint.proceed()
    }
}