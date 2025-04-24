package com.flyjingfish.android_aop_annotation.impl

import com.flyjingfish.android_aop_annotation.AopMethod
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.utils.InvokeMethod
import java.lang.reflect.Method

internal object JoinPoint {
    @JvmStatic
    fun getJoinPoint(
        targetClass: Class<*>,
        args: Array<Any?>?,
        target: Any?,
        isSuspend: Boolean,
        targetMethod: Method,
        invokeMethod: InvokeMethod?,
        aopMethod: AopMethod
    ): ProceedJoinPoint {
        return ProceedJoinPointImpl(
            targetClass,
            args,
            target,
            isSuspend,
            targetMethod,
            invokeMethod,
            aopMethod
        )
    }
    @JvmStatic
    fun getJoinPointSuspend(
        targetClass: Class<*>,
        args: Array<Any?>?,
        target: Any?,
        isSuspend: Boolean,
        targetMethod: Method,
        invokeMethod: InvokeMethod?,
        aopMethod: AopMethod
    ): ProceedJoinPoint {
        return ProceedJoinPointSuspendImpl(
            targetClass,
            args,
            target,
            isSuspend,
            targetMethod,
            invokeMethod,
            aopMethod
        )
    }
    @JvmStatic
    fun setOnInvokeListener(proceedJoinPoint: ProceedJoinPoint, onInvokeListener: OnInvokeListener) {
        (proceedJoinPoint as ProceedJoinPointImpl).setOnInvokeListener(onInvokeListener)
    }
    @JvmStatic
    fun setHasNext(proceedJoinPoint: ProceedJoinPoint,hasNext: Boolean) {
        (proceedJoinPoint as ProceedJoinPointImpl).setHasNext(hasNext)
    }
    @JvmStatic
    fun setStaticMethod(proceedJoinPoint: ProceedJoinPoint, staticMethod: Method?) {
        (proceedJoinPoint as ProceedJoinPointImpl).setStaticMethod(staticMethod)
    }
}