package com.flyjingfish.android_aop_annotation.base

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut

/**
 * 注解切面的回调接口与 [AndroidAopPointCut] 配合使用，与 [BasePointCut] 不同的是这个类支持 suspend
 * 要求切点函数必须是 suspend 修饰的才会回调 [invokeSuspend]，否则还是回调 [invoke]
 */
interface BasePointCutSuspend<T : Annotation>:BasePointCut<T> {
    /**
     * 使用自定义注解的被 suspend 修饰的方法被调用时回调这个方法,不会回调 [invoke]
     * @param joinPoint 切点相关信息
     * @param anno 切点设置的注解
     */
    suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, anno: T)

    override fun invoke(joinPoint: ProceedJoinPoint, anno: T): Any? {
        return joinPoint.proceed()
    }
}