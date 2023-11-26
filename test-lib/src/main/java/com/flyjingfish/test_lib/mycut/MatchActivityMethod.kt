package com.flyjingfish.test_lib.mycut

import android.app.Activity
import android.content.Context
import android.util.Log
import com.flyjingfish.android_aop_annotation.MatchClassMethod
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.test_lib.ToastUtils.makeText

@AndroidAopMatchClassMethod(
    targetClassName = "androidx.appcompat.app.AppCompatActivity",
    methodName = ["startActivity"]
)
class MatchActivityMethod : MatchClassMethod {
    override operator fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("MatchActivityMethod", "=====invoke=====$methodName")
        if (joinPoint.target is Activity) {
            makeText((joinPoint.target as Context), "进入匹配类切面")
        }
        return joinPoint.proceed()
    }
}