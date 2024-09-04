package com.flyjingfish.android_aop_core.cut

import com.flyjingfish.android_aop_annotation.AopMethod
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint

internal class ProceedJoinPointProxy(private val joinPoint: ProceedJoinPoint) : ProceedJoinPoint {
    override fun getArgs(): Array<Any?>? {
        return joinPoint.args
    }

    override fun proceed(): Any? {
        throw UnsupportedOperationException("PermissionCut ProceedJoinPointSuspend can't call proceed")
    }

    override fun proceed(vararg args: Any?): Any? {
        throw UnsupportedOperationException("PermissionCut ProceedJoinPointSuspend can't call proceed")
    }

    override fun getTargetMethod(): AopMethod {
        return joinPoint.targetMethod
    }

    override fun getTarget(): Any? {
        return joinPoint.target
    }

    override fun getTargetClass(): Class<*> {
        return joinPoint.targetClass
    }

    override fun getOriginalArgs(): Array<Any?>? {
        return joinPoint.originalArgs
    }
}