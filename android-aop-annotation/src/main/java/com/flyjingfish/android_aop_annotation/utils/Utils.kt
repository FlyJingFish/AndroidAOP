package com.flyjingfish.android_aop_annotation.utils

import com.flyjingfish.android_aop_annotation.ProceedReturn
import com.flyjingfish.android_aop_annotation.base.OnBaseSuspendReturnListener
import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener
import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener2
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils.isIgnoreOther
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.regex.Matcher
import java.util.regex.Pattern

internal object Utils {
    fun getStartSuspendObj(target:Any): Any? {
        return runCatching {
            val method: Method = target.javaClass.getMethod("getCompletion")
            method.isAccessible = true
            val returnValue = method.invoke(target)
            val field = returnValue.javaClass.getField("uCont")
            field.isAccessible = true
            val uCont = field[returnValue]
            val completionMethod = uCont.javaClass.getMethod("getCompletion")
            completionMethod.isAccessible = true
            completionMethod.invoke(uCont)
        }.getOrNull()
    }

    fun invokeReturn(proceedReturn :ProceedReturn, listener:OnBaseSuspendReturnListener):Any?{
        return runCatching {
            if (isIgnoreOther(listener) && listener is OnSuspendReturnListener2) {
                listener.onReturn(proceedReturn)
            } else if (listener is OnSuspendReturnListener) {
                listener.onReturn(proceedReturn)
            }else{
                null
            }
        }.getOrElse {
            HandlerUtils.post {
                throw it
            }
        }
    }

    fun getRealRuntimeException(e :Throwable):RuntimeException{
        val throwable: Throwable = if (e is InvocationTargetException) {
            e.targetException
        } else {
            e
        }
        val stackTrace = throwable.stackTrace
        stackTrace?.let {
            for (stackTraceElement in it) {
                val realMethodName = getRealMethodName(stackTraceElement.methodName)
                if (realMethodName != stackTraceElement.methodName) {
                    try {
                        val field = StackTraceElement::class.java.getDeclaredField("methodName")
                        field.isAccessible = true
                        field[stackTraceElement] = realMethodName
                    } catch (_: Throwable) {
                    }
                }
            }
        }

        return if (throwable is RuntimeException) {
            throwable
        } else {
            RuntimeException(throwable)
        }
    }
    private val AOPMethodPattern: Pattern = Pattern.compile("\\$\\$.{32}\\$\\\$AndroidAOP$")
    private fun getRealMethodName(methodName:String?):String?{
        if (methodName == null){
            return null
        }
        val matcher: Matcher = AOPMethodPattern.matcher(methodName)
        return if (matcher.find()) {
            matcher.replaceAll("")
        }else{
            methodName;
        }
    }
}