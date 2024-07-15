package com.flyjingfish.android_aop_annotation

import com.flyjingfish.android_aop_annotation.utils.InvokeMethod
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

open class ProceedReturn2 (targetClass: Class<*>, args: Array<Any?>?, target: Any?, isSuspend: Boolean){
    private val args: Array<Any?>?
    private var originalArgs: Array<Any?>?
    private val target: Any?
    private val targetClass: Class<*>
    private var targetMethod: Method? = null
    private var targetInvokeMethod: InvokeMethod? = null
    private var originalMethod: Method? = null
    private var onInvokeListener: OnInvokeListener? = null
    private var hasNext = false
    private var argCount = 0
    private val isSuspend : Boolean
    private var suspendContinuation: Any? = null

    init {
        this.targetClass = targetClass
        val fakeArgs: Array<Any?>?
        if (isSuspend && args != null) {
            fakeArgs = arrayOfNulls(args.size - 1)
            for (i in 0 until args.size - 1) {
                fakeArgs[i] = args[i]
            }
            suspendContinuation = args[args.size - 1]
        } else {
            fakeArgs = args
        }
        this.args = fakeArgs
        this.target = target
        this.isSuspend = isSuspend
        if (fakeArgs != null) {
            originalArgs = fakeArgs.clone()
        } else {
            originalArgs = null
        }
        argCount = fakeArgs?.size ?: 0
    }

    /**
     * 继续执行 suspend 函数的返回值代码块
     *
     * @return 返回 suspend 函数的返回值代码块的结果
     */
    internal fun realProceed(): Any? {
        return proceed(*args!!)
    }

    private fun proceed(vararg args: Any?): Any? {

        val realArgs: Array<out Any?>
        if (isSuspend) {
            realArgs = arrayOfNulls(argCount + 1)
            System.arraycopy(args, 0, realArgs, 0, args.size)
            realArgs[argCount] = suspendContinuation

        } else {
            realArgs = args
        }
        if (this.args != null) {
            System.arraycopy(realArgs, 0, this.args, 0, this.args.size)
        }
        return try {
            var returnValue: Any? = null
            if (!hasNext) {
                returnValue = if (targetInvokeMethod != null) {
                    targetInvokeMethod!!.invoke(target, realArgs)
                } else {
                    targetMethod!!.invoke(target, *realArgs)
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

    internal fun setTargetMethod(targetMethod: Method?) {
        this.targetMethod = targetMethod
    }

    internal fun setTargetMethod(targetMethod: InvokeMethod?) {
        targetInvokeMethod = targetMethod
    }

    internal fun setOriginalMethod(originalMethod: Method?) {
        this.originalMethod = originalMethod
    }

    internal interface OnInvokeListener {
        fun onInvoke(): Any?
    }

    internal fun setOnInvokeListener(onInvokeListener: OnInvokeListener?) {
        this.onInvokeListener = onInvokeListener
    }

    internal fun setHasNext(hasNext: Boolean) {
        this.hasNext = hasNext
    }

    /**
     *
     * @return suspend 函数的返回值类型
     */
    fun getReturnType(): Class<*>? {
        val className = getReturnTypeClassName()
        if (className.isNotEmpty()){
            return Conversions.getClass_(className)
        }
        return targetMethod!!.returnType
    }

    /**
     *
     * @return suspend 函数的返回值类型
     */
    internal fun getReturnTypeClassName(): String {
        try {
            if (target != null) {
                val types = target.javaClass.genericInterfaces
                if (types.isNotEmpty()) {
                    val funtion2Type = types[0]
                    if (funtion2Type is ParameterizedType) {
                        val funtion2ArgumentsTypes = funtion2Type.actualTypeArguments
                        if (funtion2ArgumentsTypes.size >= 2) {
                            val continuationType = funtion2ArgumentsTypes[1]
                            if (continuationType is ParameterizedType) {
                                val continuationTypeArguments = continuationType.actualTypeArguments
                                for (type in continuationTypeArguments) {
                                    return type.toString().replace("\\? super ".toRegex(), "")
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
        }
        return ""
    }
}