package com.flyjingfish.android_aop_annotation

interface MatchClassMethod {
    fun invoke(joinPoint: ProceedJoinPoint, methodName:String): Any?
}