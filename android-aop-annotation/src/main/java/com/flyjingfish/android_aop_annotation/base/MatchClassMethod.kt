package com.flyjingfish.android_aop_annotation.base

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint

interface MatchClassMethod {
    fun invoke(joinPoint: ProceedJoinPoint, methodName:String): Any?
}