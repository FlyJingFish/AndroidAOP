package com.flyjingfish.android_aop_annotation.impl

import com.flyjingfish.android_aop_annotation.AopMethod
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.utils.InvokeMethod
import java.lang.reflect.Method

internal object JoinPoint {
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

    fun setOnInvokeListener(proceedJoinPoint: ProceedJoinPoint, onInvokeListener: OnInvokeListener) {
        if (proceedJoinPoint is ProceedJoinPointImpl){
            proceedJoinPoint.setOnInvokeListener(onInvokeListener)
        }
    }

    fun setHasNext(proceedJoinPoint: ProceedJoinPoint,hasNext: Boolean) {
        if (proceedJoinPoint is ProceedJoinPointImpl){
            proceedJoinPoint.setHasNext(hasNext)
        }
    }
}