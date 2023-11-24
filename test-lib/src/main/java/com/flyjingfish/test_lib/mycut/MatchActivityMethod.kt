package com.flyjingfish.test_lib.mycut

import android.util.Log
import com.flyjingfish.android_aop_annotation.MatchClassMethod
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod

@AndroidAopMatchClassMethod(
    targetClassName = "androidx.appcompat.app.AppCompatActivity",
    methodName = ["startActivity"]
)
class MatchActivityMethod : MatchClassMethod {
    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchActivityMethod", "=====invoke=====$methodName")
        return joinPoint.proceed()
    }
}