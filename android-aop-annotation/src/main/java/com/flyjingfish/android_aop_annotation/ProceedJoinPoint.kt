package com.flyjingfish.android_aop_annotation

import com.flyjingfish.android_aop_annotation.utils.InvokeMethod
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class ProceedJoinPoint(targetClass: Class<*>, args: Array<Any?>?,targetMethod: AopMethod,target: Any?) {
    /**
     * [wiki 文档使用说明](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#args)
     */
    @JvmField
    var args: Array<out Any?>?

    @JvmField
    val target: Any?

    @JvmField
    val targetClass: Class<*>

    @JvmField
    val targetMethod: AopMethod

    private var originalArgs: Array<Any?>? = null
    private lateinit var targetJavaMethod: Method
    private var targetInvokeMethod: InvokeMethod? = null
    private var originalMethod: Method? = null
    private var onInvokeListener: OnInvokeListener? = null
    private var hasNext = false
    private var argCount = 0

    init {
        this.targetClass = targetClass
        this.args = args
        if (args != null) {
            originalArgs = args.clone()
        }
        argCount = args?.size ?: 0
        this.targetMethod = targetMethod
        this.target = target
    }

    /**
     * 调用切点方法内代码
     * @return 返回切点方法返回值 [wiki 文档使用说明](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#proceed)
     */
    fun proceed(): Any? {
        val localArgs = args
        return if (localArgs == null) {
            proceed(null)
        } else {
            proceed(*args!!)
        }

    }

    /**
     * 调用切点方法内代码
     * @param args 切点方法参数数组
     * @return 返回切点方法返回值 [wiki 文档使用说明](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#proceed)
     */
    fun proceed(vararg args: Any?): Any? {
        this.args = args
        if (argCount > 0) {
            require(args.size == argCount) { "proceed 所参数个数不对" }
        }
        return try {
            var returnValue: Any? = null
            if (!hasNext) {
                returnValue = if (targetInvokeMethod != null) {
                    targetInvokeMethod!!.invoke(target, args)
                } else {
                    targetJavaMethod.invoke(target, *args)
                }
            } else if (onInvokeListener != null) {
                returnValue = onInvokeListener!!.onInvoke()
            }
            returnValue
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e.targetException)
        }
    }

    suspend fun proceedSuspend(): Any? {
        val localArgs = args
        return if (localArgs == null) {
            proceedSuspend(null)
        } else {
            proceedSuspend(*args!!)
        }

    }

    suspend fun proceedSuspend(vararg args: Any?): Any? {
        this.args = args
        if (argCount > 0) {
            require(args.size == argCount) { "proceed 所参数个数不对" }
        }
        return try {
            var returnValue: Any? = null
            if (!hasNext) {
                returnValue = if (targetInvokeMethod != null) {
                    targetInvokeMethod!!.invoke(target, args)
                } else {
                    targetJavaMethod.invoke(target, *args)
                }
            } else if (onInvokeListener != null) {
                returnValue = onInvokeListener!!.onInvokeSuspend()
            }
            returnValue
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e.targetException)
        }
    }

    /**
     *
     * @return 切点方法相关信息
     */
    fun getTargetMethod(): AopMethod {
        return targetMethod!!
    }

    /**
     *
     * @return 切点方法所在对象，如果方法为静态的，此值为null
     */
    fun getTarget(): Any? {
        return target
    }

    /**
     *
     * @return 切点方法所在类 Class
     */
    fun getTargetClass(): Class<*> {
        return targetClass
    }

    internal fun setTargetJavaMethod(targetMethod: Method) {
        targetJavaMethod = targetMethod
    }

    internal fun setTargetMethod(targetMethod: InvokeMethod?) {
        targetInvokeMethod = targetMethod
    }

    internal fun setOriginalMethod(originalMethod: Method?) {
        this.originalMethod = originalMethod
    }


    internal interface OnInvokeListener {
        fun onInvoke(): Any?
        suspend fun onInvokeSuspend(): Any?
    }

    internal fun setOnInvokeListener(onInvokeListener: OnInvokeListener?) {
        this.onInvokeListener = onInvokeListener
    }

    internal fun setHasNext(hasNext: Boolean) {
        this.hasNext = hasNext
    }

    /**
     * 和 [ProceedJoinPoint.args] 相比，返回的引用地址不同，但数组里边的对象一致
     * @return 最开始进入方法时的参数  [wiki 文档使用说明](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#args)
     */
    fun getOriginalArgs(): Array<out Any?>? {
        return originalArgs
    }

    fun getArgs(): Array<out Any?>? {
        return args
    }
}