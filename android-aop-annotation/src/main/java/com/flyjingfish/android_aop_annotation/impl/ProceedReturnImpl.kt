package com.flyjingfish.android_aop_annotation.impl

import com.flyjingfish.android_aop_annotation.AopMethod
import com.flyjingfish.android_aop_annotation.Conversions
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedReturn
import com.flyjingfish.android_aop_annotation.ProceedReturn2
import com.flyjingfish.android_aop_annotation.utils.HandlerUtils
import com.flyjingfish.android_aop_annotation.utils.InvokeMethod
import com.flyjingfish.android_aop_annotation.utils.Utils
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

internal class ProceedReturnImpl(targetClass: Class<*>, args: Array<Any?>?, target: Any?):
    ProceedReturn2, ProceedReturn {
    private val args: Array<Any?>?
    private val target: Any?
    private val targetClass: Class<*>
    private var targetMethod: Method? = null
    private var targetInvokeMethod: InvokeMethod? = null
    private var originalMethod: Method? = null
    private var onInvokeListener: OnInvokeListener? = null
    private var hasNext = false
    private var returnType: Class<*>? = null
    private var staticMethod: Method? = null

    init {
        this.targetClass = targetClass
        this.args = args
        this.target = target
    }
    override fun proceed(): Any? {
        return realProceed()
    }
    /**
     * 继续执行 suspend 函数的返回值代码块
     *
     * @return 返回 suspend 函数的返回值代码块的结果
     */
    private fun realProceed(): Any? {
        return proceed(*args!!)
    }

    private fun proceed(vararg args: Any?): Any? {
        return runCatching {
            var returnValue: Any? = null
            if (!hasNext) {
                returnValue = if (targetInvokeMethod != null) {
                    targetInvokeMethod!!.invoke(target, args)
                } else if (staticMethod != null) {
                    staticMethod!!.invoke(null, target, args)
                } else {
                    targetMethod!!.invoke(target, *args)
                }
            } else if (onInvokeListener != null) {
                returnValue = onInvokeListener!!.onInvoke()
            }
            returnValue
        }.getOrElse {
            HandlerUtils.post {
                throw Utils.getRealRuntimeException(it)
            }
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

    internal fun setOnInvokeListener(onInvokeListener: OnInvokeListener?) {
        this.onInvokeListener = onInvokeListener
    }

    internal fun setHasNext(hasNext: Boolean) {
        this.hasNext = hasNext
    }

    internal fun setReturnType(clazz: Class<*>?) {
        returnType = clazz
    }



    /**
     *
     * @return suspend 函数的返回值类型,有可能拿不到实际类型，拿不到时返回 Object 类型,请使用 [ProceedJoinPoint.getTargetMethod]并通过[AopMethod.returnType]获取
     */
    @Deprecated("这个方法即将删除，请使用 ProceedJoinPoint 的 targetMethod.returnType", ReplaceWith("joinPoint.targetMethod.returnType"))
    override fun getReturnType(): Class<*>? {
        if (returnType != null){
            return returnType
        }
        val className = getReturnTypeClassName()
        if (!className.isNullOrEmpty()){
            returnType = Conversions.getClass_(className)
            return returnType
        }
        returnType = targetMethod!!.returnType
        return returnType
    }

    /**
     *
     * @return suspend 函数的返回值类型
     */
    private fun getReturnTypeClassName(): String? {
        return runCatching {
            var className = ""
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
                                    className = type.toString().replace("\\? super ".toRegex(), "")
                                    break
                                }
                            }
                        }
                    }
                }
            }
            className
        }.getOrNull()
    }
    internal fun setStaticMethod(staticMethod: Method?) {
        this.staticMethod = staticMethod
    }
}