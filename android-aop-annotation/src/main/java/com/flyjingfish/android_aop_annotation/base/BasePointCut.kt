package com.flyjingfish.android_aop_annotation.base

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint


interface BasePointCut<T : Annotation> {
    fun invoke(joinPoint: ProceedJoinPoint, anno: T): Any?
}