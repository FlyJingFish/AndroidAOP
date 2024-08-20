package com.flyjingfish.test_lib.replace

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.enums.MatchType
import com.flyjingfish.android_aop_core.proxy.MatchClassMethodProxy

@AndroidAopMatchClassMethod(
    targetClassName = "com.flyjingfish.test_lib.replace.ReplaceToast",
    type = MatchType.SELF,
    methodName = ["*"]
)
class ReplaceToastProxy : MatchClassMethodProxy() {
    override fun invokeProxy(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("ReplaceToastProxy","methodName=$methodName," +
                "parameterNames=${joinPoint.targetMethod.parameterNames.toList()}," +
                "parameterTypes=${joinPoint.targetMethod.parameterTypes.toList()}," +
                "returnType=${joinPoint.targetMethod.returnType}," +
                "args=${joinPoint.args?.toList()},target=${joinPoint.target},targetClass=${joinPoint.targetClass},"
        )

        return joinPoint.proceed()
    }
}