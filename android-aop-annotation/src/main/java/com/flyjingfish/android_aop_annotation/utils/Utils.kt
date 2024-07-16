package com.flyjingfish.android_aop_annotation.utils

import com.flyjingfish.android_aop_annotation.ProceedReturn
import com.flyjingfish.android_aop_annotation.base.OnBaseSuspendReturnListener
import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener
import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener2
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils.isIgnoreOther
import java.lang.reflect.Method

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
}