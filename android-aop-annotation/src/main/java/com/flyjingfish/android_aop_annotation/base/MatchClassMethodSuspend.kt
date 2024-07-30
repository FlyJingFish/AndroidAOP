package com.flyjingfish.android_aop_annotation.base

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod

/**
 * 匹配切面的回调接口与 [AndroidAopMatchClassMethod] 配合使用，与 [MatchClassMethod] 不同的是这个类支持 suspend
 * 要求切点函数必须是 suspend 修饰的才会回调 [invokeSuspend]，否则还是回调 [invoke]
 */
interface MatchClassMethodSuspend : MatchClassMethod {
    /**
     * 匹配到的被 suspend 修饰的方法被调用时将会回调这个方法,不会回调 [invoke]
     *
     * 并且注意最好在实现方法内使用 withContext 等函数包裹您的代码，否则可能有Bug
     *
     * [详细使用说明看这里](https://github.com/FlyJingFish/AndroidAOP/wiki/Suspend-%E5%88%87%E7%82%B9%E5%87%BD%E6%95%B0#2%E6%94%AF%E6%8C%81-suspend-%E7%9A%84-basepointcutsuspend-%E5%92%8C-matchclassmethodsuspend)
     *
     * @param joinPoint 切点相关信息
     * @param methodName 匹配的方法名，如果是 Lambda 表达式，请看 wiki 文档
     */
    suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, methodName: String)

    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        return joinPoint.proceed()
    }
}