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
     * 使用自定义注解的被 suspend 修饰的方法被调用时回调这个方法,否则还是回调 [invoke]
     *
     * 并且注意最好在实现方法内使用 withContext 等函数包裹您的代码，否则可能有Bug
     *
     * [详细使用说明看这里](https://flyjingfish.github.io/AndroidAOP/zh/Suspend_cut/#2-suspend-basepointcutsuspend-matchclassmethodsuspend)
     * @param joinPoint 切点相关信息
     * @param anno 切点设置的注解
     */
    suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, anno: T)

    /**
     * 如果注解的函数是非suspend函数，那么也应该重写此函数，否则会直接往后执行
     */
    override fun invoke(joinPoint: ProceedJoinPoint, anno: T): Any? {
        return joinPoint.proceed()
    }
}