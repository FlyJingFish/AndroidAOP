package com.flyjingfish.androidaop

import android.util.Log
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.flyjingfish.android_aop_annotation.ProceedReturn
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.base.MatchClassMethodSuspend
import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener
import com.flyjingfish.android_aop_annotation.enums.MatchType
import com.flyjingfish.android_aop_annotation.proxy.MatchClassMethodProxy
import com.flyjingfish.android_aop_annotation.proxy.MatchClassMethodSuspendProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//@AndroidAopMatchClassMethod(
//    targetClassName = "com.flyjingfish.androidaop.ReplaceGetData3",
//    type = MatchType.SELF,
//    methodName = ["*"]
//)
class ReplaceGetData3Proxy : MatchClassMethodSuspendProxy() {
    override fun invokeProxy(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        Log.e("ReplaceGetData3Proxy","1methodName=$methodName," +
                "parameterNames=${joinPoint.targetMethod.parameterNames.toList()}," +
                "parameterTypes=${joinPoint.targetMethod.parameterTypes.toList()}," +
                "returnType=${joinPoint.targetMethod.returnType}," +
                "args=${joinPoint.args?.toList()},target=${joinPoint.target},targetClass=${joinPoint.targetClass},"
        )

        return joinPoint.proceed()
    }

    override suspend fun invokeSuspendProxy(joinPoint: ProceedJoinPointSuspend, methodName: String) {
        withContext(Dispatchers.IO) {
            Log.e("ReplaceGetData3Proxy","2methodName=$methodName," +
                    "parameterNames=${joinPoint.targetMethod.parameterNames.toList()}," +
                    "parameterTypes=${joinPoint.targetMethod.parameterTypes.toList()}," +
                    "returnType=${joinPoint.targetMethod.returnType}," +
                    "args=${joinPoint.args?.toList()},target=${joinPoint.target},targetClass=${joinPoint.targetClass},"
            )

            joinPoint.proceed { proceedReturn -> (proceedReturn.proceed() as Int) + 100 }
        }

    }
}