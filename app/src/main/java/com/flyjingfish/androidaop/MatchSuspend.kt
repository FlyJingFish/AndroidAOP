package com.flyjingfish.androidaop

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType
import com.flyjingfish.test_lib.ToastUtils

@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.androidaop.MainActivity",
    methodName = ["suspend getData(int)"],
    type = MatchType.SELF
)
class MatchSuspend : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchSuspend", "======$methodName");
//        ToastUtils.makeText(ToastUtils.app,"MatchSuspend======$methodName")
        return joinPoint.proceed()
    }
}