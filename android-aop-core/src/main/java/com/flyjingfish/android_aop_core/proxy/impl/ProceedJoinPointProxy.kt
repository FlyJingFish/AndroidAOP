package com.flyjingfish.android_aop_core.proxy.impl

import com.flyjingfish.android_aop_annotation.AopMethod
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_core.proxy.ProxyMethod
import com.flyjingfish.android_aop_core.proxy.ProxyType

/**
 *
 * 代理 [ProceedJoinPoint]，并使 [AndroidAopReplaceClass] 使用起来类似于 [AndroidAopMatchClassMethod]
 */
internal open class ProceedJoinPointProxy(private val joinPoint: ProceedJoinPoint): ProceedJoinPoint {
    private val proxyTarget : Any?
    private val proxyTargetClass : Class<*>
    private val proxyAopMethod : ProxyAopMethod
    private val proxyArgs : Array<Any?>
    private val proxyOriginalArgs : Array<Any?>
    private val proxyOldArgs : Array<Any?>
    init {
        val annotation : ProxyMethod? = joinPoint.targetMethod.getAnnotation(ProxyMethod::class.java)
        proxyTargetClass = annotation?.proxyClass?.java ?: joinPoint.targetClass
        proxyAopMethod = ProxyAopMethod(joinPoint.targetMethod,annotation?.type)
        proxyTarget = if (proxyAopMethod.type != ProxyType.STATIC_METHOD){
            joinPoint.args?.get(0)
        }else{
            null
        }

        val args = joinPoint.args

        proxyArgs = if (!args.isNullOrEmpty()){
            proxyOldArgs = args
            if (proxyAopMethod.type != ProxyType.STATIC_METHOD){
                args.copyOfRange(1, args.size)
            }else{
                args.copyOfRange(0, args.size)
            }
        }else {
            proxyOldArgs = arrayOfNulls<Any?>(0)
            arrayOfNulls<Any?>(0)
        }

        proxyOriginalArgs = proxyArgs.clone()

    }

    override fun getArgs(): Array<Any?> {
        return proxyArgs
    }

    override fun proceed(): Any? {
        return this.proceed(*proxyArgs)
    }

    override fun proceed(vararg args: Any?): Any? {
        return if (proxyAopMethod.type == ProxyType.METHOD){
            val oldArgs = proxyOldArgs
            val realArgs = arrayOfNulls<Any?>(oldArgs.size)
            realArgs[0] = oldArgs[0]
            System.arraycopy(args, 0, realArgs, 1, args.size)
            joinPoint.proceed(*realArgs)
        }else{
            joinPoint.proceed(*args)
        }
    }

    override fun getTargetMethod(): AopMethod {
        return proxyAopMethod
    }

    override fun getTarget(): Any? {
        return proxyTarget
    }

    override fun getTargetClass(): Class<*> {
        return proxyTargetClass
    }

    override fun getOriginalArgs(): Array<Any?> {
        return proxyOriginalArgs
    }
}